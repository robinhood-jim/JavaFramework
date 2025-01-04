package org.apache.parquet.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;

public class CustomAvroRecordMaterializer<T> extends RecordMaterializer<T> {
    private AvroRecordConverter<T> root;

    public CustomAvroRecordMaterializer(MessageType requestedSchema, Schema avroSchema, GenericData baseModel) {
        this.root = new AvroRecordConverter(requestedSchema, avroSchema, baseModel);
    }

    public T getCurrentRecord() {
        return this.root.getCurrentRecord();
    }

    public GroupConverter getRootConverter() {
        return this.root;
    }
}
