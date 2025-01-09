package com.robin.comm.fileaccess.util;

import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.PrimitiveConverter;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.Type;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParquetMapGroupConverter extends GroupConverter {
    private Map<String,Object> record;
    private MessageType type;
    private List<Type> types;
    // row base scan can use HasMap reuse,other renew one
    private boolean reuseMap=false;
    public ParquetMapGroupConverter(MessageType type){
        this.type=type;
        types=type.getFields();
    }
    public ParquetMapGroupConverter(ReadSupport.ReadContext readContext, Map<String,Object> recMap,boolean reuseMap){
        this.type=readContext.getRequestedSchema();
        types=type.getFields();
        this.record=recMap;
        this.reuseMap=reuseMap;
    }
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
        if(!reuseMap) {
            record = new HashMap<>();
        }else if(record!=null){
            record.clear();
        }
    }

    @Override
    public void end() {
    }

    public Map<String, Object> getRecord() {
        return record;
    }
}
