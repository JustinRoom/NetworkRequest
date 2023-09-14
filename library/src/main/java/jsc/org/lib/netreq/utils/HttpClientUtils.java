package jsc.org.lib.netreq.utils;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import jsc.org.lib.netreq.interceptor.CacheValidDateInterceptor;
import jsc.org.lib.netreq.interceptor.CustomCookieJar;
import jsc.org.lib.netreq.interceptor.RequestRetryInterceptor;
import jsc.org.lib.netreq.ssl.TrustAllCerts;
import jsc.org.lib.netreq.ssl.TrustAllHostnameVerifier;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class HttpClientUtils {

    public static OkHttpClient.Builder createOkHttpClientBuilder(long timeoutSec) {
        return createOkHttpClientBuilder(timeoutSec, timeoutSec, timeoutSec);
    }

    public static OkHttpClient.Builder createOkHttpClientBuilder(long connectTimeoutSec,
                                                                 long readTimeoutSec,
                                                                 long writeTimeoutSec) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        //支持访问所有https
        try {
            String protocol = "TLS";//SSL
            SSLContext sslcontext = SSLContext.getInstance(protocol);
            sslcontext.init(null, new TrustManager[]{new TrustAllCerts()}, null);
            builder.sslSocketFactory(sslcontext.getSocketFactory(), new TrustAllCerts());
            builder.hostnameVerifier(new TrustAllHostnameVerifier());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder;
    }

    /**
     * For WebSocket.
     * @param builder
     * @param time
     * @param unit
     */
    public static void addPingInterval(OkHttpClient.Builder builder, long time, TimeUnit unit) {
        builder.pingInterval(time, unit);
    }

    public static void addHttpLoggingInterceptor(OkHttpClient.Builder builder) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLoggingInterceptor);
    }

    public static void addRequestRetryInterceptor(OkHttpClient.Builder builder, int maxRetryCount) {
        builder.addInterceptor(new RequestRetryInterceptor(maxRetryCount));
    }

    public static void addCacheValidDateInterceptor(OkHttpClient.Builder builder, Context context) {
        //缓存2天
        addCacheValidDateInterceptor(builder, context, 60 * 60 * 24 * 2);
    }

    public static void addCacheValidDateInterceptor(OkHttpClient.Builder builder, Context context, long cacheStaleSec) {
        builder.addInterceptor(new CacheValidDateInterceptor(context, cacheStaleSec));
    }

    public static <I extends Interceptor> void addCustomInterceptor(OkHttpClient.Builder builder, I interceptor) {
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
    }

    public static void addCookieJar(OkHttpClient.Builder builder) {
        builder.cookieJar(new CustomCookieJar());
    }

    public static void addCache(OkHttpClient.Builder builder, Context context) {
        //缓存文件128Mb
        addCache(builder, context, 1024 * 1024 * 128);
    }

    public static void addCache(OkHttpClient.Builder builder, Context context, long maxSize) {
        File cacheFile = new File(context.getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, maxSize);
        builder.cache(cache);
    }
}
