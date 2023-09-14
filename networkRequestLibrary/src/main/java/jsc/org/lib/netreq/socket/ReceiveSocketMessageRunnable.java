package jsc.org.lib.netreq.socket;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 接收socket消息
 *
 */
public class ReceiveSocketMessageRunnable implements Runnable {

    private BufferedReader br = null;
    private Handler handler = null;
    private boolean isRunning = false;

    public ReceiveSocketMessageRunnable(BufferedReader br, Handler handler) {
        this.br = br;
        this.handler = handler;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            if (br == null) {
                stop();
                continue;
            }
            try {
                String msg = br.readLine();
                if (handler != null && !TextUtils.isEmpty(msg)) {
                    Message message = Message.obtain();
                    message.what = 0x10;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
