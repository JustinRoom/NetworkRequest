package com.jsc.netreq;

import android.app.Application;

import jsc.org.lib.netreq.http.HttpRequester;
import jsc.org.lib.netreq.utils.HttpClientUtils;
import okhttp3.OkHttpClient;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initOkHttpClient();
    }

    private void initOkHttpClient() {
        HttpRequester.getInstance().register(getApplicationContext());
        OkHttpClient.Builder builder = HttpClientUtils.createOkHttpClientBuilder(20L);
        HttpClientUtils.addHttpLoggingInterceptor(builder);
        //可根据自己的业务需求调整
        HttpClientUtils.addRequestRetryInterceptor(builder, 2);
        HttpClientUtils.addCacheValidDateInterceptor(builder, getApplicationContext());
        HttpClientUtils.addCookieJar(builder);
        HttpClientUtils.addCache(builder, getApplicationContext());
//        HttpClientUtils.addCustomInterceptor(builder, null);
        HttpRequester.getInstance().bindClient(builder.build());
    }
}
