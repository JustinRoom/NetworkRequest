package jsc.org.lib.netreq.http;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class HttpRequester {

    private static HttpRequester instance = null;
    private final Object lock = new Object();
    private final List<Call> calls = new ArrayList<>();
    private Context mContext = null;
    private OkHttpClient mClient = null;

    private HttpRequester() {
    }

    public static HttpRequester getInstance() {
        if (instance == null) {
            instance = new HttpRequester();
        }
        return instance;
    }

    public void register(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public void bindClient(OkHttpClient mClient) {
        this.mClient = mClient;
    }

    public void unregister() {
        cancelAll();
        mClient = null;
        mContext = null;
    }

    public <C extends NetCallback> void request(@NonNull Request request, @NonNull C callback) {
        if (mClient == null) {
            return;
        }
        Call call = mClient.newCall(request);
        addCall(call);
        callback.bindCall(call);
        callback.onStart(callback.getArguments());
        //enqueue()方法内部本身就是用ThreadPool来维护的，所以本地不需要再创建一个线程池。
        call.enqueue(callback);
    }

    public static <C extends NetCallback> void request(@NonNull OkHttpClient mClient, @NonNull Request request, @NonNull C callback) {
        Call call = mClient.newCall(request);
        callback.bindCall(call);
        callback.onStart(callback.getArguments());
        //enqueue()方法内部本身就是用ThreadPool来维护的，所以本地不需要再创建一个线程池。
        call.enqueue(callback);
    }

    public void addCall(Call call) {
        synchronized (lock) {
            calls.add(call);
        }
    }

    public void removeCall(Call call) {
        synchronized (lock) {
            calls.remove(call);
        }
    }

    public void cancelAll() {
        synchronized (lock) {
            for (Call call : calls) {
                call.cancel();
            }
            calls.clear();
        }
    }
}
