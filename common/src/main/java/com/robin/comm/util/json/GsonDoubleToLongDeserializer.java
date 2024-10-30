package com.robin.comm.util.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class GsonDoubleToLongDeserializer implements JsonDeserializer<Map<String,Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return (Map<String, Object>) read(jsonElement);
    }
    public Object read(JsonElement element){
        if(element.isJsonArray()){
            List<Object> list=new ArrayList<>();
            JsonArray array= element.getAsJsonArray();

            for(JsonElement ele:array){
                list.add(read(ele));
            }
            return list;
        }else if(element.isJsonObject()){
            Map<String,Object> resMap=new HashMap<>();
            Iterator<Map.Entry<String,JsonElement>> iter= element.getAsJsonObject().entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,JsonElement> entry=iter.next();
                resMap.put(entry.getKey(),read(entry.getValue()));
            }
            return resMap;
        }else if(element.isJsonPrimitive()){
            JsonPrimitive element1=element.getAsJsonPrimitive();
            if(element1.isBoolean()) {
                return element1.getAsBoolean();
            } else if(element1.isString()) {
                return element1.getAsString();
            } else if(element1.isNumber()){
                Number number=element1.getAsNumber();
                if (Math.ceil(number.doubleValue()) == number.longValue()){
                    Long longval=number.longValue();
                    if(Long.parseLong(String.valueOf(longval.intValue()))==longval && longval.intValue()<Integer.MAX_VALUE){
                        return longval.intValue();
                    }
                    return longval;
                }else{
                    return number.doubleValue();
                }
            }
        }
        return null;
    }
}
