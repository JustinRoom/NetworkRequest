package jsc.org.lib.netreq.utils;

import okhttp3.HttpUrl;

public final class UrlUtils {

    public static boolean isValidUrl(String originalUrl) {
        HttpUrl url = HttpUrl.parse(originalUrl);
        return url != null;
    }

}
