package jsc.org.lib.netreq.http;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public final class URLParamsBuilder {
    String baseUrl;
    String actionUrl;
    List<String> params = new ArrayList<>();

    public URLParamsBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public URLParamsBuilder actionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
        return this;
    }

    public URLParamsBuilder addParams(String key, String value) {
        params.add(String.format("%s=%s", key, value));
        return this;
    }

    public URLParamsBuilder addURLEncoderParams(String key, String value) {
        try {
            params.add(String.format("%s=%s", key, URLEncoder.encode(value, "utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String build() {
        if (TextUtils.isEmpty(baseUrl))
            throw new IllegalArgumentException("No base url.");
        return String.format("%s%s?%s", baseUrl, actionUrl, params());
    }

    public String params() {
        String[] array = new String[params.size()];
        params.toArray(array);
        return String.join("&", array);
    }
}