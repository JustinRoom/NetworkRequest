package com.jsc.netreq.utils;

import com.jsc.netreq.entity.ServerData;

import org.json.JSONException;
import org.json.JSONObject;

public final class ServerDataParser {

    public static ServerData parse(String bodyStr) {
        try {
            ServerData data = new ServerData();
            JSONObject obj = new JSONObject(bodyStr);
            data.ret = obj.optInt("ret");
            data.msg = obj.optString("msg");
            data.body = obj.optJSONObject("body");
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
