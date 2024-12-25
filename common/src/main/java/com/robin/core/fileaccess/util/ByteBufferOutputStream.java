package com.robin.core.fileaccess.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
}
