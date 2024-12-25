package com.robin.core.fileaccess.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
@Slf4j
public class ByteBufferInputStream  extends InputStream {
    private ByteBuffer byteBuffer;
    private int count;
    public ByteBufferInputStream(ByteBuffer byteBuffer,int count){
        this.byteBuffer=byteBuffer;
        byteBuffer.position(0);
        this.count=count;
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

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
}
