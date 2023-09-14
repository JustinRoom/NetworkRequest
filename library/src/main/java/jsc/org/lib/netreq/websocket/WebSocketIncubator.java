package jsc.org.lib.netreq.websocket;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class WebSocketIncubator {

    private OkHttpClient mClient = null;
    private WebSocket mWebSocket = null;

    public void bindClient(OkHttpClient mClient) {
        this.mClient = mClient;
    }

    public void begin(@NonNull Request request, WebSocketListener listener) {
        if (mClient != null) {
            mWebSocket = mClient.newWebSocket(request, listener);
        }
    }

    public boolean sendMessage(String msg) {
        return mWebSocket != null && mWebSocket.send(msg);
    }

    public void sendMessage(ByteString msg) {
        if (mWebSocket != null) {
            mWebSocket.send(msg);
        }
    }

    /**
     * 撤销待发消息
     */
    public void revokeMessage() {
        if (mWebSocket != null) {
            mWebSocket.cancel();
        }
    }

    /**
     * @param code   Endpoints MAY use the following pre-defined status codes when sending
     *               a Close frame.
     *               <url>
     *               <li>1000 indicates a normal closure, meaning that the purpose for
     *               which the connection was established has been fulfilled.</li>
     *               <li>1001 indicates that an endpoint is "going away", such as a server
     *               going down or a browser having navigated away from a page.</li>
     *               <li>1002 indicates that an endpoint is terminating the connection due
     *               to a protocol error.</li>
     *               <li>1003 indicates that an endpoint is terminating the connection
     *               because it has received a type of data it cannot accept (e.g., an
     *               endpoint that understands only text data MAY send this if it
     *               receives a binary message)</li>
     *               </url>
     * @param reason
     */
    public void end(int code, String reason) {
        if (mWebSocket != null) {
            mWebSocket.close(code, reason);
            mWebSocket = null;
        }
    }

    public void unregister() {
        mClient = null;
        mWebSocket = null;
    }
}
