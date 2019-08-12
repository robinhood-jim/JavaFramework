package com.robin.comm.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;


public class GsonUtil {
    public static final Gson getGson(){
        return new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String,Object>>(){}.getType(),
                new GsonDoubleToLongDeserializer()).create();
    }
}
