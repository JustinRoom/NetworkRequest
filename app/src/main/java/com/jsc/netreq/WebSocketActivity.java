package com.jsc.netreq;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jsc.netreq.databinding.ActivityWebsocketBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import jsc.org.lib.netreq.utils.HttpClientUtils;
import jsc.org.lib.netreq.websocket.WebSocketIncubator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketActivity extends BaseActivity {

    ActivityWebsocketBinding binding = null;
    WebSocketIncubator socketIncubator = new WebSocketIncubator();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebsocketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        binding.btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(0x10);
            }
        });
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketIncubator != null) {
            socketIncubator.end(1000, "");
        }
    }

    private void connect() {
        OkHttpClient.Builder builder = HttpClientUtils.createOkHttpClientBuilder(20L);
        HttpClientUtils.addHttpLoggingInterceptor(builder);
        HttpClientUtils.addPingInterval(builder, 10, TimeUnit.SECONDS);
        //可根据自己的业务需求调整
//        HttpClientUtils.addRequestRetryInterceptor(builder, 2);
//        HttpClientUtils.addCacheValidDateInterceptor(builder, getApplicationContext());
//        HttpClientUtils.addCookieJar(builder);
//        HttpClientUtils.addCache(builder, getApplicationContext());
//        HttpClientUtils.addCustomInterceptor(builder, null);
        socketIncubator.bindClient(builder.build());
        Request request = new Request.Builder()
                .url("ws://www.xmind.net:54872")
                .header("Connection", "Upgrade")
                .header("Upgrade", "websocket")
                .build();
        socketIncubator.begin(request, new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                try {
                    JSONObject obj = new JSONObject(text);
                    String cmdType = obj.optString("cmdType");
                    final String msg = obj.optString("msg");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    switch (cmdType) {
                        case "auth_back":
                            mHandler.sendEmptyMessage(0x11);
                            break;
                        case "pong":
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                String msg = bytes.string(StandardCharsets.UTF_8);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                int code = response.code();
                String msg = response.message();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg + ":" + code, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x10:
                    sendAuthMsg();
                    break;

                case 0x11:
                    sendHartMsg();
                    sendEmptyMessageDelayed(0x11, 5_000L);
                    break;

            }
        }
    };


    private void sendAuthMsg() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("cmdType", "auth");
            obj.put("serverType", "WEBSOCKET_SERVER");
            obj.put("clientType", "Web");
            JSONObject subObj = new JSONObject();
            subObj.put("token", "111111");
            subObj.put("answerId", "123B20d283db4353bbaa89586c8e5531");
            obj.put("data", subObj);
            if (socketIncubator.sendMessage(obj.toString())) {
                Toast.makeText(getApplicationContext(), "认证请求发送成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "认证请求发送失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendHartMsg() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("cmdType", "ping");
            obj.put("serverType", "WEBSOCKET_SERVER");
            obj.put("clientType", "Web");
            JSONObject subObj = new JSONObject();
            obj.put("data", subObj);
            socketIncubator.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
