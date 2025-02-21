package com.robin.core.fileaccess.util;

import lombok.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;

public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer byteBuffer;
    private int count;
    private static final byte nullByte=(byte) 0xff;
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
        byteBuffer.putLong(value);
    }
    public void writeGap() throws IOException{
        write(0);
    }
    public void writeNullTag() throws IOException{
        //overwrite 0x00 with 0xff
        byteBuffer.position(byteBuffer.position()-1);
        byteBuffer.put(nullByte);
        writeGap();
    }
    public void writeDouble(Double value) throws IOException{
        byteBuffer.putDouble(value);
    }
    public void writeLeft(int length,int input) throws IOException{
        for(int i=0;i<length;i++){
            write(input);
        }
    }
    public void writeInt(int value) throws IOException{
       byteBuffer.putInt(value);
    }
    public void writeShort(short value) throws IOException{
        byteBuffer.putShort(value);
    }
    public void writeDouble(double value) throws IOException{
        byteBuffer.putDouble(value);
    }
    public void writeBytes(byte[] bytes) throws IOException{
        byteBuffer.put(bytes);
    }
    public void writePrimitive(@NonNull Object obj,boolean lastColumn, Charset charset) throws IOException{
        Assert.notNull(obj,"");
        if(Integer.class.isAssignableFrom(obj.getClass())){
            writeInt((Integer)obj);

        }else if(Short.class.isAssignableFrom(obj.getClass())){
            writeShort((Short)obj);
        }else if(Long.class.isAssignableFrom(obj.getClass())){
            writeLong((Long)obj);

        }else if(Double.class.isAssignableFrom(obj.getClass())){
            writeDouble((Double)obj);
        }
        else if(String.class.isAssignableFrom(obj.getClass())){
            byte[] bytes=obj.toString().getBytes(charset);
            writeBytes(bytes);
        }else if(Timestamp.class.isAssignableFrom(obj.getClass())){
            writeLong(((Timestamp)obj).getTime());
        }
        else{
            throw new IOException("no primitive type support");
        }
        if(!lastColumn) {
            writeGap();
        }
    }
    public int getPosition(){
        return byteBuffer.position();
    }
    public void seek(int pos){
        byteBuffer.position(pos);
    }

}
