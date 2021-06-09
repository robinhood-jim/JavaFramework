package com.robin.tracer.sender;

import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.io.IOException;
import java.util.List;

/**
 * 黑洞，如果不需要输出到zipkin或者kafka
 */
public class BlackHoleSender extends Sender {

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        return 0;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> list) {
        return 0;
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> list) {
        return new Call.Base<Void>() {

            @Override
            protected Void doExecute() throws IOException {
                return null;
            }

            @Override
            protected void doEnqueue(Callback<Void> callback) {

            }

            @Override
            public Call<Void> clone() {
                return null;
            }
        };
    }

}
