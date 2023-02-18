package com.robin.comm.fileaccess.iterator;


import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.InputFile;

import java.io.IOException;
import java.util.Map;

public class CustomParquetReader<T> extends ParquetReader<T> {
    private DataCollectionMeta colMeta;
    public CustomParquetReader(Configuration conf, Path file, ReadSupport readSupport,DataCollectionMeta colMeta) throws IOException {
        super(conf,file, readSupport);
        this.colMeta=colMeta;
    }

    public static  CustomParquetReader.Builder builder(InputFile file,DataCollectionMeta colMeta) {
        return new CustomParquetReader.Builder(file,colMeta);
    }
    public static class Builder extends org.apache.parquet.hadoop.ParquetReader.Builder<Map> {
        private boolean enableCompatibility;
        private DataCollectionMeta colMeta;
        /** @deprecated */
        @Deprecated
        private Builder(Path path,DataCollectionMeta colMeta) {
            super(path);
            this.colMeta=colMeta;
        }

        private Builder(InputFile file,DataCollectionMeta colMeta) {
            super(file);
            this.colMeta=colMeta;
        }



        public CustomParquetReader.Builder disableCompatibility() {
            return this;
        }

        public CustomParquetReader.Builder withCompatibility(boolean enableCompatibility) {
            this.enableCompatibility = enableCompatibility;
            return this;
        }

        protected ReadSupport<Map> getReadSupport() {
            return new CustomReadSupport(colMeta);
        }
    }
}
