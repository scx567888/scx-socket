<p align="center">
    <img src="https://scx.cool/logos/scx-socket-logo.svg" width="300px"  alt="scx-logo"/>
</p>
<p align="center">
    <a target="_blank" href="https://github.com/scx567888/scx-socket/actions/workflows/ci.yml">
        <img src="https://github.com/scx567888/scx-socket/actions/workflows/ci.yml/badge.svg" alt="CI"/>
    </a>
    <a target="_blank" href="https://search.maven.org/artifact/cool.scx/scx-socket">
        <img src="https://img.shields.io/maven-central/v/cool.scx/scx-socket?color=ff69b4" alt="maven-central"/>
    </a>
    <a target="_blank" href="https://github.com/scx567888/scx-socket">
        <img src="https://img.shields.io/github/languages/code-size/scx567888/scx-socket?color=orange" alt="code-size"/>
    </a>
    <a target="_blank" href="https://github.com/scx567888/scx-socket/issues">
        <img src="https://img.shields.io/github/issues/scx567888/scx-socket" alt="issues"/>
    </a>
    <a target="_blank" href="https://github.com/scx567888/scx-socket/blob/master/LICENSE">
        <img src="https://img.shields.io/github/license/scx567888/scx-socket" alt="license"/>
    </a>
</p>
<p align="center">
    <a target="_blank" href="https://github.com/scx567888/scx-common">
        <img src="https://img.shields.io/badge/SCX Common-f44336" alt="SCX Common"/>
    </a>
    <a target="_blank" href="https://github.com/eclipse-vertx/vert.x">
        <img src="https://img.shields.io/badge/Vert.x-ff8000" alt="Vert.x"/>
    </a>
    <a target="_blank" href="https://github.com/FasterXML/jackson">
        <img src="https://img.shields.io/badge/Jackson-d8b125" alt="Jackson"/>
    </a>
    <a target="_blank" href="https://github.com/netty/netty">
        <img src="https://img.shields.io/badge/Netty-44be16" alt="Netty"/>
    </a>
    <a target="_blank" href="https://github.com/scx567888/scx-logging">
        <img src="https://img.shields.io/badge/SCX Logging-29aaf5" alt="SCX Logging"/>
    </a>
    <a target="_blank" href="https://github.com/cbeust/testng">
        <img src="https://img.shields.io/badge/TestNG-9c27b0" alt="TestNG"/>
    </a>
</p>

简体中文 | [English](./README.md)

> 一个 WebSocket 框架

## Maven

``` xml
<dependency>
    <groupId>cool.scx</groupId>
    <artifactId>scx-socket</artifactId>
    <version>{version}</version>
</dependency>
```

## 快速开始

#### 1. 创建一个 ScxSocket 服务端 。

``` java
import cool.scx.socket.ScxSocketServer;
import io.vertx.core.Vertx;

public class YourServer {

    public static void main(String[] args) {

        //1, 创建服务器
        var scxSocketServer = new ScxSocketServer();

        //2, 添加客户端连接事件
        scxSocketServer.onClientConnect(clientContent -> {

            clientContent.send("Hello ScxSocketClient !!!");

            clientContent.onMessage((m) -> {
                System.out.println("onMessage : " + m);
            });

            clientContent.onClose(c -> {
                System.out.println("onClose");
            });

            clientContent.onError(e -> {
                System.out.println("onError");
            });

        });

        //3, 使用 vertx 的 httpServer 进行调用
        Vertx.vertx().createHttpServer()
                .webSocketHandler(scxSocketServer::call)
                .listen(8990);
    }

}
```

#### 2. 创建 ScxSocket 客户端 。

```java
import cool.scx.socket.ScxSocketClient;
import io.vertx.core.Vertx;

public class YourClient {

    public static void main(String[] args) {

        var vertxWebSocketClient = Vertx.vertx().createWebSocketClient();

        //1, 创建客户端
        var scxSocketClient = new ScxSocketClient("ws://127.0.0.1:8990", vertxWebSocketClient);

        //2, 添加事件
        scxSocketClient.onOpen(clientContent -> {
            scxSocketClient.send("Hello ScxSocketServer !!!");
        });

        scxSocketClient.onMessage((m) -> {
            System.out.println("onMessage : " + m);
        });

        scxSocketClient.onClose(c -> {
            System.out.println("onClose");
        });

        scxSocketClient.onError(e -> {
            System.out.println("onError");
        });

        //3, 连接服务端
        scxSocketClient.connect();

    }

}
```

#### 3. 运行 YourServer 和 YourClient , 您应看到如下内容 。

```text
onMessage : Hello ScxSocketServer !!!
onMessage : Hello ScxSocketClient !!!
```

有关更多信息，请参阅 [文档](https://scx.cool/docs/scx-socket/index.html)

备注 : JavaScript 版本的实现, 请看 [SCX-SOCKET-JS](https://github.com/scx567888/scx-socket-js)

## Stats

![Alt](https://repobeats.axiom.co/api/embed/24f847bcb1d91c1a0af19fad1c3d4460a465b2cd.svg "Repobeats analytics image")
