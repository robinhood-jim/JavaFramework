package com.robin.comm.fileaccess.util;


import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferSeekableInputStream extends InputStream implements  Seekable, PositionedReadable {
    private ByteBuffer byteBuffer;
    public ByteBufferSeekableInputStream(ByteBuffer byteBuffer){
        this.byteBuffer=byteBuffer;
    }
    @Override
    public int read() throws IOException {
        if (byteBuffer.remaining() == 0) {
            return -1;
        }
        return byteBuffer.get() & 0xff;
    }

    @Override
    public int available() throws IOException {
        return byteBuffer.remaining();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (byteBuffer.remaining() == 0) {
            return -1;
        }
        if (len > byteBuffer.remaining()) {
            len = byteBuffer.remaining();
        }
        byteBuffer.get(b, off, len);
        return len;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        long newPos = byteBuffer.position() + n;
        if (newPos > byteBuffer.remaining()) {
            n = byteBuffer.remaining();
        }
        byteBuffer.position(byteBuffer.position() + (int) n);
        return n;
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        long oldPos=getPos();
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
    public void seek(long pos) throws IOException {
        byteBuffer.position((int)pos);
    }

    @Override
    public long getPos() throws IOException {
        return byteBuffer.position();
    }

    @Override
    public boolean seekToNewSource(long l) throws IOException {
        return false;
    }
}
