package jsc.org.lib.netreq.websocket;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;

public class MockWebServerIncubator {

    MockWebServer server = null;

    public void mockWebSocket() {
        if (server != null) {
            return;
        }
        server = new MockWebServer();
        MockResponse response = new MockResponse();
        response.addHeader("token", "yunjisoft");
        server.enqueue(response.withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                String requestUrl = webSocket.request().url().toString();
                Log.i("MockWebServer", "socket disconnected: " + requestUrl);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                String requestUrl = webSocket.request().url().toString();
                Log.i("MockWebServer", "socket connected: " + requestUrl);
            }
        }));
        String hostName = server.getHostName();
        int port = server.getPort();
        String url = "wss:" + hostName + ":" + port;
    }
}
