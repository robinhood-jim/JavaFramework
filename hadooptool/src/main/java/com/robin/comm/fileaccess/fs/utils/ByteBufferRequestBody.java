package com.robin.comm.fileaccess.fs.utils;

import com.robin.core.fileaccess.util.ByteBufferInputStream;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferRequestBody extends RequestBody {
    private ByteBuffer byteBuffer;
    private int count;
    private String contentType;
    public ByteBufferRequestBody(ByteBuffer byteBuffer,String contentType,int count){
        this.byteBuffer=byteBuffer;
        this.count=count;
        this.contentType=contentType;
    }

    @Override
    public long contentLength() throws IOException {
        return count;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        MediaType mediaType = null;
        if (this.contentType != null) {
            mediaType = MediaType.parse(this.contentType);
        }
        return mediaType == null ? MediaType.parse("application/octet-stream") : mediaType;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        sink.write(Okio.source(new ByteBufferInputStream(byteBuffer,count)),count);
    }
}
