package com.robin.comm.fileaccess.util;

import org.apache.hadoop.fs.FSDataInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MockInputStream extends FSDataInputStream {
    MockFileSystem fs;

    public MockInputStream(MockFileSystem fs, byte[] streamBytes) throws IOException {
        super(new ByteArraySeekableInputStream(streamBytes));
        this.fs = fs;
    }
    public MockInputStream(MockFileSystem fs, ByteBuffer byteBuffer){
        super(new ByteBufferSeekableInputStream(byteBuffer));
        this.fs=fs;
    }

    public MockInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException {
        fs.removeStream(this);
    }
}
