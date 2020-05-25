package com.robin.core.web.comm;

import com.robin.core.base.util.MessageUtils;

import java.util.HashMap;

public class RespBody extends HashMap<String, Object> {

    public static RespBody msg(String msg) {
        RespBody r = new RespBody();
        r.put("msg", msg);
        r.put("code", 0);
        r.put("data", null);
        return r;
    }

    public static RespBody ok(Object object, String... msgs) {
        RespBody r = new RespBody();
        if (msgs != null && msgs.length > 0) {
            r.put("msg", msgs[0]);
        } else {
            r.put("msg", null);
        }
        r.put("code", 0);
        r.put("data", object);
        return r;
    }


    public static RespBody error(Exception ex) {
        RespBody r = new RespBody();
        r.put("code", 500);
        r.put("msg", ex.getMessage());
        r.put("data", null);
        return r;
    }

    public static RespBody error(String message) {
        RespBody r = new RespBody();
        r.put("data", null);
        r.put("code", 500);
        r.put("msg", message);
        return r;
    }


    public static RespBody errorWithMsg(Integer code, String message) {
        RespBody r = new RespBody();
        r.put("data", null);
        r.put("msg", message);
        r.put("code", code);
        return r;
    }


    public static RespBody okWithCode(String messageTitle, String... params) {
        RespBody r = new RespBody();
        r.put("data", null);
        r.put("msg", MessageUtils.getMessage(messageTitle, params));
        r.put("code", 0);
        return r;
    }

    public static RespBody error(int code, String... params) {
        RespBody r = new RespBody();
        r.put("data", null);
        r.put("msg", MessageUtils.getMessage(code, params));
        r.put("code", code);
        return r;
    }

    @Override
    public RespBody put(String key, Object value) {
        super.put(key, value);
        return this;
    }

}