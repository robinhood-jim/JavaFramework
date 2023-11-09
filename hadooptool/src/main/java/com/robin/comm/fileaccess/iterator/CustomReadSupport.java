package com.robin.comm.fileaccess.iterator;


import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.api.InitContext;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.api.*;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.Type;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomReadSupport extends ReadSupport<java.util.Map> {
    MessageType type;
    DataCollectionMeta colMeta;
    public CustomReadSupport(DataCollectionMeta colMeta){
        this.colMeta=colMeta;
    }


    @Override
    public ReadContext init(InitContext context) {
        type = context.getFileSchema();
        return new ReadContext(context.getFileSchema());
    }


    @Override
    public RecordMaterializer<Map> prepareForRead(Configuration configuration, Map<String, String> map, MessageType messageType, ReadContext readContext) {

        java.util.Map<String, String> metadata = readContext.getReadSupportMetadata();
        MessageType parquetSchema = readContext.getRequestedSchema();
        List<Type> types=parquetSchema.getFields();

        return new RecordMaterializer<Map>() {
            Map<String, Object> record;

            @Override
            public Map getCurrentRecord() {
                return record;
            }


            @Override
            public GroupConverter getRootConverter() {
                return new GroupConverter() {
                    @Override
                    public Converter getConverter(int i) {
                        return new PrimitiveConverter() {
                            @Override
                            public void addBinary(Binary value) {
                                try {
                                    if (types.get(i).getOriginalType().equals(OriginalType.UTF8)) {
                                        record.put(types.get(i).getName(), new String(value.getBytes(), "UTF-8"));
                                    }else{
                                        record.put(types.get(i).getName(),value.getBytes());
                                    }
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }

                            @Override
                            public void addBoolean(boolean value) {
                                record.put(types.get(i).getName(),value);
                            }

                            @Override
                            public void addDouble(double value) {
                                record.put(types.get(i).getName(),value);
                            }

                            @Override
                            public void addFloat(float value) {
                                record.put(types.get(i).getName(),value);
                            }

                            @Override
                            public void addInt(int value) {
                                record.put(types.get(i).getName(),value);
                            }

                            @Override
                            public void addLong(long value) {
                                if (types.get(i).getOriginalType() == OriginalType.DATE || types.get(i).getOriginalType() == OriginalType.TIME_MILLIS
                                        || types.get(i).getOriginalType() == OriginalType.TIMESTAMP_MILLIS) {
                                    record.put(types.get(i).getName(),new Timestamp(value));
                                } else {
                                    record.put(types.get(i).getName(),value);
                                }
                            }
                        };
                    }

                    @Override
                    public void start() {
                        record=new HashMap<>();
                    }

                    @Override
                    public void end() {
                    }
                };
            }
        };
    }

}
