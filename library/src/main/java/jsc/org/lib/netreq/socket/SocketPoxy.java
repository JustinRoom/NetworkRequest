package jsc.org.lib.netreq.socket;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * socket封装
 *
 * @author jsc
 */
public class SocketPoxy {

    private final String ip;
    private final int port;
    private Socket socket = null;
    private BufferedReader br = null;
    private OutputStream os = null;

    public SocketPoxy(@NonNull String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public BufferedReader getBufferedReader() {
        return br;
    }

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * 连接
     *
     * @return
     */
    public boolean connect() {
        if (!isConnected()) {
            try {
                socket = new Socket(ip, port);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isConnected();
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        if (isConnected()) {
            try {
                os.close();
                br.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                br = null;
                socket = null;
                os = null;
            }
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMessage(String msg) {
        if (!isConnected()) return;
        String lineMsg = msg + "\r\n";
        try {
            os.write(lineMsg.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
