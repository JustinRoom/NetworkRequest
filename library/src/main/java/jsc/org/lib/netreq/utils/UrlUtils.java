package jsc.org.lib.netreq.utils;

import okhttp3.HttpUrl;

public final class UrlUtils {

    public static HttpUrl toHttpUrl(String url) {
        return HttpUrl.parse(url);
    }

    public static boolean isValidUrl(String url) {
        return toHttpUrl(url) != null;
    }

    public static void requireValidUrl(String url) {
        if (toHttpUrl(url) == null)
            throw new IllegalArgumentException("Invalid url.");
    }
}
