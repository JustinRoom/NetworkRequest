package jsc.org.lib.netreq.http;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class NetCallback implements Callback {

    private Bundle arguments = null;
    protected Call call;
    private boolean responded = false;
    private boolean canceled = false;

    public void bindCall(Call call) {
        this.call = call;
    }

    @Nullable
    public Bundle getArguments() {
        return arguments;
    }

    public NetCallback setArguments(Bundle arguments) {
        this.arguments = arguments;
        return this;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean cancel() {
        if (!responded) {
            canceled = true;
            call.cancel();
            HttpRequester.getInstance().removeCall(call);
            return true;
        }
        return false;
    }

    protected void onStart(@Nullable Bundle arguments) {

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        responded = true;
        HttpRequester.getInstance().removeCall(call);
        log(call.request().url().toString(), 0x999, "请求失败", e.getLocalizedMessage());
        callback(getArguments(), 0x999, "请求失败", e.getLocalizedMessage());
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        responded = true;
        HttpRequester.getInstance().removeCall(call);
        if (!dealOriginalResponse(getArguments(), response)) {
            int code = response.code();
            ResponseBody body = response.body();
            String content = body == null ? "" : body.string();
            if (code >= 200 && code < 300) {
                log(call.request().url().toString(), code, "请求成功", content);
                callback(getArguments(), code, "请求成功", content);
            } else {
                log(call.request().url().toString(), code, "请求失败", content);
                callback(getArguments(), code, "请求失败", content);
            }
        }
    }

    protected boolean dealOriginalResponse(@Nullable Bundle arguments, Response response) {
        return false;
    }

    public abstract void callback(@Nullable Bundle arguments, int code, String tips, String body);

    protected void onFinished() {

    }

    private void log(String url, int code, String tips, String content) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("url", url);
            obj.put("code", code);
            obj.put("tips", tips);
            obj.put("content", content);
            Log.d("NetCallback", obj.toString());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
