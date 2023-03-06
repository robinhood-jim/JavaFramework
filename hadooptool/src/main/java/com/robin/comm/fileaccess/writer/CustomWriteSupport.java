package com.robin.comm.fileaccess.writer;


import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.io.ParquetEncodingException;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.Type;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomWriteSupport<T> extends WriteSupport<T> {
    MessageType schema;
    RecordConsumer recordConsumer;
    List<Type> types;
    List<ColumnDescriptor> descriptors;

    public CustomWriteSupport(MessageType schema) {
        this.schema = schema;
        this.types = schema.getFields();
        this.descriptors = schema.getColumns();
    }

    @Override
    public WriteContext init(Configuration configuration) {
        return new WriteContext(schema, new HashMap<>());
    }

    @Override
    public void prepareForWrite(RecordConsumer recordConsumer) {
        this.recordConsumer = recordConsumer;
    }

    @Override
    public void write(T obj) {
        if (Map.class.isAssignableFrom(obj.getClass())) {
            Map<String, Object> map = (Map<String, Object>) obj;
            recordConsumer.startMessage();
            for (int i = 0; i < types.size(); i++) {
                Type type = types.get(i);
                Object tobj = map.get(type.getName());
                recordConsumer.startField(type.getName(),i);

                switch (type.asPrimitiveType().getPrimitiveTypeName()) {
                    case BOOLEAN:
                        recordConsumer.addBoolean(ObjectUtils.isEmpty(tobj) ? false : Boolean.parseBoolean(tobj.toString()));
                        break;
                    case FLOAT:
                        recordConsumer.addFloat(ObjectUtils.isEmpty(tobj) ? Float.valueOf("0.0") : Float.valueOf(tobj.toString()));
                        break;
                    case DOUBLE:
                        recordConsumer.addDouble(ObjectUtils.isEmpty(tobj) ? 0.0 : Double.parseDouble(tobj.toString()));
                        break;
                    case INT32:
                        recordConsumer.addInteger(ObjectUtils.isEmpty(tobj) ? 0 : Integer.valueOf(tobj.toString()));
                        break;
                    case INT64:
                        Long realVal = 0L;
                        if (!ObjectUtils.isEmpty(tobj)) {
                            if (type.getOriginalType() == OriginalType.DATE || type.getOriginalType() == OriginalType.TIME_MILLIS
                                    || type.getOriginalType() == OriginalType.TIMESTAMP_MILLIS) {
                                realVal = ((Timestamp) tobj).getTime();
                            } else {
                                realVal = Long.valueOf(tobj.toString());
                            }
                        }
                        recordConsumer.addLong(realVal);
                        break;
                    case INT96:
                        Long realVal1 = 0L;
                        if (!ObjectUtils.isEmpty(tobj)) {
                            if (type.getOriginalType() == OriginalType.DATE || type.getOriginalType() == OriginalType.TIME_MILLIS
                                    || type.getOriginalType() == OriginalType.TIMESTAMP_MILLIS) {
                                realVal1 = ((Timestamp) tobj).getTime();
                            } else {
                                realVal1 = Long.valueOf(tobj.toString());
                            }
                        }
                        recordConsumer.addLong(realVal1);
                        break;
                    case BINARY:
                        if (!ObjectUtils.isEmpty(tobj)) {
                            if (type.getOriginalType().equals(OriginalType.UTF8)) {
                                recordConsumer.addBinary(stringToBinary(tobj.toString()));
                            } else if (byte[].class.isAssignableFrom(tobj.getClass())) {
                                recordConsumer.addBinary(Binary.fromConstantByteArray((byte[]) tobj));
                            }
                        } else {
                            recordConsumer.addBinary(stringToBinary(""));
                        }
                        break;
                    default:
                        throw new ParquetEncodingException("unsupport column type" + type.asPrimitiveType().getPrimitiveTypeName());

                }
                recordConsumer.endField(type.getName(),i);
            }
            recordConsumer.endMessage();
        }
    }

    private Binary stringToBinary(Object value) {
        return Binary.fromString(value.toString());
    }
}
