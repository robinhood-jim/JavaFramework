package com.robin.core.fileaccess.util;

import lombok.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;

public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer byteBuffer;
    private int count;
    public ByteBufferOutputStream(ByteBuffer byteBuffer){
        this.byteBuffer=byteBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        if(count<byteBuffer.capacity()){
            byteBuffer.put((byte) b);
        }else{
            throw new IndexOutOfBoundsException();
        }
        count += 1;
    }
    public void reset(){
        byteBuffer.position(0);
    }


    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getCount() {
        return count;
    }
    public void writeLong(long value) throws IOException{
        for(byte bt:GenericByteConvertor.longToBytesLittle(value)){
            write(bt);
        }
    }
    public void writeGap() throws IOException{
        write(0);
    }
    public void writeLeft(int length,int input) throws IOException{
        for(int i=0;i<length;i++){
            write(input);
        }
    }
    public void writeInt(int value) throws IOException{
        for(byte bt:GenericByteConvertor.intToByteLittle(value)){
            write(bt);
        }
    }
    public void writeShort(short value) throws IOException{
        for(byte bt:GenericByteConvertor.shortToByteLittle(value)){
            write(bt);
        }
    }
    public void writeDouble(double value) throws IOException{
        for(byte bt:GenericByteConvertor.double2Bytes(value)){
            write(bt);
        }
    }
    public void writeBytes(byte[] bytes) throws IOException{
        for(byte bt:bytes){
            write(bt);
        }
    }
    public int writePrimitive(@NonNull Object obj,int startPos, Charset charset) throws IOException{
        Assert.notNull(obj,"");
        int nextPos=startPos;
        if(Integer.class.isAssignableFrom(obj.getClass())){
            writeInt((Integer)obj);
            nextPos+=5;
        }else if(Short.class.isAssignableFrom(obj.getClass())){
            writeShort((Short)obj);
            nextPos+=3;
        }else if(Long.class.isAssignableFrom(obj.getClass())){
            writeLong((Long)obj);
            nextPos+=9;
        }else if(String.class.isAssignableFrom(obj.getClass())){
            byte[] bytes=obj.toString().getBytes(charset);
            nextPos+=bytes.length+1;
            writeBytes(bytes);
        }else{
            throw new IOException("no primitive type support");
        }
        writeGap();
        return nextPos;
    }


}
