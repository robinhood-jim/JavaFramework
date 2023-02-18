package com.robin.comm.fileaccess.writer;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;
import java.util.Map;

public class CustomParquetWriter extends ParquetWriter<Map<String,Object>> {
    public CustomParquetWriter(Path path, MessageType schema, boolean enableDictionary, CompressionCodecName codecName) throws IOException {
        super(path,new CustomWriteSupport(schema),codecName,DEFAULT_BLOCK_SIZE, DEFAULT_PAGE_SIZE, enableDictionary, false);
    }
    public static class Builder<T> extends org.apache.parquet.hadoop.ParquetWriter.Builder<T, CustomParquetWriter.Builder<T>> {
        private MessageType schema;


        public Builder(Path file,MessageType schema) {
            super(file);
            this.schema = schema;
        }

        public Builder(OutputFile file,MessageType schema) {
            super(file);
            this.schema = schema;
        }
        public CustomParquetWriter.Builder<T> withSchema(MessageType schema) {
            this.schema = schema;
            return this;
        }
        public CustomParquetWriter.Builder<T> withCompressionCodec(CompressionCodecName compressCode){
            super.withCompressionCodec(compressCode);
            return this;
        }

        protected CustomParquetWriter.Builder<T> self() {
            return this;
        }

        protected WriteSupport<T> getWriteSupport(Configuration conf) {
            return new CustomWriteSupport(this.schema);
        }
    }
}
