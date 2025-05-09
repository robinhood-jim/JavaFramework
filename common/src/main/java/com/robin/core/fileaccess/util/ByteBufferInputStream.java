package com.robin.core.fileaccess.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

@Slf4j
public class ByteBufferInputStream  extends InputStream {
    private ByteBuffer byteBuffer;
    private int count;
    public ByteBufferInputStream(ByteBuffer byteBuffer,int count){
        this.byteBuffer=byteBuffer;
        byteBuffer.position(0);
        this.count=count;
    }
    public ByteBufferInputStream(ByteBuffer buffer,byte[] bytes){
        this.byteBuffer=buffer;
        byteBuffer.position(0);
        buffer.put(bytes);
        this.count=bytes.length;
    }
    @Override
    public int read() throws IOException {
        if (byteBuffer.position()>count || byteBuffer.remaining() == 0) {
            return -1;
        }
        return byteBuffer.get() & 0xff;
    }

    @Override
    public int available() throws IOException {
        return count-byteBuffer.position();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (off+byteBuffer.position()>=count || byteBuffer.remaining() == 0) {
            return -1;
        }
        if(byteBuffer.position()+len+off>count){
            len=count-off-byteBuffer.position();
        }else if (len > byteBuffer.remaining()) {
            len = byteBuffer.remaining();
        }
        byteBuffer.get(b, off, len);
        return len;
    }
    public byte[] readString(int len) throws IOException{
        ByteBuffer tmpbuffer=ByteBuffer.allocate(len);
        byte b;
        while((b=byteBuffer.get())!=0x00 && b!=-1){
            tmpbuffer.put(b);
        }
        byteBuffer.position(byteBuffer.position()-1);
        byte[] retByte=new byte[tmpbuffer.position()];
        tmpbuffer.position(0);
        tmpbuffer.get(retByte);
        return retByte;
    }
    public Integer readInt(){
        return byteBuffer.getInt();
    }
    public Long readLong(){
        return byteBuffer.getLong();
    }
    public Short readShort(){
        return byteBuffer.getShort();
    }
    public Double readDouble(){
        return byteBuffer.getDouble();
    }
    public Timestamp readTimeStamp(){
        return new Timestamp(byteBuffer.getLong());
    }
    public Float readFloat(){
        return byteBuffer.getFloat();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public int capacity(){
        return count;
    }
    public void seek(int pos){
        byteBuffer.position(pos);
    }

    @Override
    public void close() throws IOException {
        byteBuffer.clear();
    }
}
