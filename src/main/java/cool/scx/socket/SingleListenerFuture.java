package cool.scx.socket;

import io.vertx.core.Future;

import java.util.function.Consumer;

/**
 * Vertx 中的 Future 可以添加多个回调且无法取消 ,此类用于解决此问题
 */
public final class SingleListenerFuture<T> {

    private final Future<T> vertxFuture;
    //为了解决 Future 无法移除回调 采取的折中方式
    private volatile Consumer<T> _onSuccess;
    private volatile Consumer<Throwable> _onFailure;

    public SingleListenerFuture(Future<T> vertxFuture) {
        this.vertxFuture = vertxFuture;
        this.vertxFuture.onSuccess(this::_callOnSuccess).onFailure(this::_callOnFailure);
    }

    public synchronized boolean isComplete() {
        return vertxFuture.isComplete();
    }

    public synchronized SingleListenerFuture<T> onSuccess(Consumer<T> onSuccess) {
        //如果已经有结果 即刻执行
        if (this.vertxFuture.succeeded()) {
            if (onSuccess != null) {
                onSuccess.accept(this.vertxFuture.result());
            }
        } else {
            this._onSuccess = onSuccess;
        }
        return this;
    }

    public synchronized SingleListenerFuture<T> onFailure(Consumer<Throwable> onFailure) {
        //如果已经有结果 即刻执行
        if (this.vertxFuture.failed()) {
            if (onFailure != null) {
                onFailure.accept(this.vertxFuture.cause());
            }
        } else {
            this._onFailure = onFailure;
        }
        return this;
    }

    //为了解决 Future 无法移除回调 采取的折中方式
    private synchronized void _callOnSuccess(T t) {
        if (_onSuccess != null) {
            _onSuccess.accept(t);
        }
    }

    //为了解决 Future 无法移除回调 采取的折中方式
    private synchronized void _callOnFailure(Throwable throwable) {
        if (_onFailure != null) {
            _onFailure.accept(throwable);
        }
    }

    public Future<T> vertxFuture() {
        return vertxFuture;
    }

}
