package cool.scx.socket.test;

import cool.scx.socket.ScxSocketFrame;
import org.testng.annotations.Test;

import static cool.scx.socket.ScxSocketFrame.Type.MESSAGE;

public class ScxSocketFrameTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var s = new ScxSocketFrame();
        s.seq_id = 100;
        s.type = MESSAGE;
        s.now = System.currentTimeMillis();
        s.payload = "消息 Message😀😀😀 😁😁 😂😂😂!!!";

        for (int i = 0; i < 9999; i++) {
            var json = s.toJson();
            var socketFrame1 = ScxSocketFrame.fromJson(json);
        }

        //粗略测试一下性能
        var l = System.nanoTime();
        for (int i = 0; i < 999999; i++) {
            var json = s.toJson();
            var socketFrame = ScxSocketFrame.fromJson(json);
        }
        System.out.println("JSON 耗时 :" + (System.nanoTime() - l) / 1000_000);

    }

}
