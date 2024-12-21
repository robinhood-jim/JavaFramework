package com.robin.comm.fileaccess.util;

import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;


public class ByteArraySeekableInputStream extends ByteArrayInputStream implements Seekable, PositionedReadable {

    public ByteArraySeekableInputStream(byte[] buf) {
        super(buf);
    }
    public ByteArraySeekableInputStream(ByteBuffer byteBuffer){
        super(byteBuffer.array());
    }


    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        long oldPos = getPos();
        int nread = -1;
        try {
            seek(position);
            nread = read(buffer, offset, length);
        } finally {
            seek(oldPos);
        }
        return nread;
    }


    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        int nread = 0;
        while (nread < length) {
            int nbytes = read(position + nread, buffer, offset + nread, length - nread);
            if (nbytes < 0) {
                throw new EOFException("End of file reached before reading fully.");
            }
            nread += nbytes;
        }
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        readFully(position, buffer, 0, buffer.length);
    }

    @Override
    public void seek(long l) throws IOException {
        try{
            this.reset();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        this.skip(l);
    }

    @Override
    public long getPos() throws IOException {
        return pos;
    }


    @Override
    public boolean seekToNewSource(long l) throws IOException {
        return false;
    }
}
