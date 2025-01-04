package com.robin.comm.fileaccess.util;


import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.api.InitContext;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.api.*;
import org.apache.parquet.schema.MessageType;

import java.util.HashMap;
import java.util.Map;

public class CustomRowReadSupport extends ReadSupport<Map<String,Object>> {
    MessageType type;

    public CustomRowReadSupport(){
    }


    @Override
    public ReadContext init(InitContext context) {
        type = context.getFileSchema();
        return new ReadContext(context.getFileSchema());
    }


    @Override
    public RecordMaterializer<Map<String,Object>> prepareForRead(Configuration configuration, Map<String, String> map, MessageType messageType, ReadContext readContext) {

        return new RecordMaterializer<Map<String,Object>>() {
            Map<String, Object> record=new HashMap<>();

            @Override
            public Map getCurrentRecord() {
                return record;
            }


            @Override
            public GroupConverter getRootConverter() {
                return new ParquetMapGroupConverter(readContext,record,true);
            }
        };
    }

}
