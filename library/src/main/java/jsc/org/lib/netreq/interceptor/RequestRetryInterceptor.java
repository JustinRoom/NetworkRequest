package jsc.org.lib.netreq.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestRetryInterceptor implements Interceptor {

    private int max = 0;
    private int counter = 0;

    public RequestRetryInterceptor(int max) {
        this.max = max;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        while (!response.isSuccessful() && counter < max) {
            counter++;
            response = chain.proceed(request);
        }
        return response;
    }
}