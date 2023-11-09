package com.robin.comm.fileaccess.util;

import org.apache.parquet.io.SeekableInputStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileSeekableInputStream extends SeekableInputStream {
    private final FileChannel channel;
    private long position;

    public FileSeekableInputStream(String filename) throws IOException, URISyntaxException {
        this.channel = FileChannel.open(Paths.get(new URL(filename).toURI()), StandardOpenOption.READ);
        this.position = 0;
    }

    @Override
    public int read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            return -1;
        }
        position += bytesRead;
        return buffer.get(0) & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            return -1;
        }
        position += bytesRead;
        return bytesRead;
    }

    @Override
    public void seek(long newPos) throws IOException {
        channel.position(newPos);
        position = newPos;
    }

    @Override
    public void readFully(byte[] bytes) throws IOException {
        ByteBuffer buffer=ByteBuffer.wrap(bytes);
        channel.read(buffer);
    }

    @Override
    public void readFully(byte[] bytes, int offset, int length) throws IOException {
        ByteBuffer buffer=ByteBuffer.wrap(bytes,offset,length);
        channel.read(buffer);
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        return channel.read(byteBuffer);
    }

    @Override
    public void readFully(ByteBuffer byteBuffer) throws IOException {
        channel.read(byteBuffer);
    }

    @Override
    public long getPos() throws IOException {
        return position;
    }

    @Override
    public void close() throws IOException {
        //channel.close();
    }
    public void closeQuitly() throws IOException{
        channel.close();
    }
}
