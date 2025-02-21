package com.robin.core.fileaccess.util;

import com.robin.core.base.exception.OperationNotSupportException;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;

public class ByteBufferUtils {
    public static void fillInteger(@NonNull ByteBuffer buffer,@NonNull int value){
        buffer.putInt(value);
    }
    public static void fillLong(@NonNull ByteBuffer buffer,@NonNull long value){
        buffer.putLong(value);
    }
    public static void fillShort(@NonNull ByteBuffer buffer, @NonNull short value){
        buffer.putShort(value);
    }
    public static void fillDouble(@NonNull ByteBuffer buffer,@NonNull double value){
        buffer.putDouble(value);
    }
    public static void fillString(@NonNull ByteBuffer buffer, @NonNull String value, @NonNull Charset charset){
        buffer.put(value.getBytes(charset));
    }
    public static void fillTimestamp(@NonNull ByteBuffer buffer,@NonNull Timestamp value){
        buffer.putLong(value.getTime());
    }
    public static void fillGap(@NonNull ByteBuffer buffer){
        buffer.put((byte)0);
    }
    public static void fillPrimitive(@NonNull ByteBuffer buffer,@NonNull Object value,Charset charset){
        if(Integer.class.isAssignableFrom(value.getClass())){
            fillInteger(buffer,(Integer) value);
        }else if(Short.class.isAssignableFrom(value.getClass())){
            fillShort(buffer,(Short)value);
        }else if(Long.class.isAssignableFrom(value.getClass())){
            fillLong(buffer,(Long)value);
        }else if(Double.class.isAssignableFrom(value.getClass())){
            fillDouble(buffer,(Double)value);
        }else if(String.class.isAssignableFrom(value.getClass())){
            fillString(buffer,(String)value,charset);
        }else if(Timestamp.class.isAssignableFrom(value.getClass())){
            fillTimestamp(buffer,(Timestamp)value);
        }else{
            throw new OperationNotSupportException("no primitive type supported");
        }
    }
    public static byte[] getContent(ByteBuffer buffer){
        int count=buffer.position();
        buffer.position(0);
        byte[] bytes=new byte[count];
        buffer.get(bytes);
        return bytes;
    }
}
