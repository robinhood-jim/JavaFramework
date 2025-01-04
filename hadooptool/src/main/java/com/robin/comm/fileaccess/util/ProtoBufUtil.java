package com.robin.comm.fileaccess.util;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.IOException;


public class ProtoBufUtil {
    public static String translateType(DataSetColumnMeta column) {
        String retStr = "";
        if(column.getColumnType().equals(Const.META_TYPE_INTEGER)){
            retStr="int32";
        } else if (column.getColumnType().equals(Const.META_TYPE_BIGINT)) {
            retStr = "int64";
        } else if (column.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
            retStr = "double";
        } else if (column.getColumnType().equals(Const.META_TYPE_NUMERIC)) {
            retStr = "float";
        } else if (column.getColumnType().equals(Const.META_TYPE_TIMESTAMP)) {
            retStr = "int64";
        } else if (column.getColumnType().equals(Const.META_TYPE_BOOLEAN)) {
            retStr = "bool";
        } else if (column.getColumnType().equals(Const.META_TYPE_STRING) ||column.getColumnType().equals(Const.META_TYPE_CLOB)) {
            retStr = "string";
        } else if (column.getColumnType().equals(Const.META_TYPE_BINARY) || column.getColumnType().equals(Const.META_TYPE_BLOB)) {
            retStr = "byte";
        }
        return retStr;
    }
    public static ProtoContainer initSchema(DataCollectionMeta colmeta)  {
        try {
            ProtoContainer container = new ProtoContainer();
            if (!CollectionUtils.isEmpty(colmeta.getColumnList())) {
                DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
                schemaBuilder.setName(colmeta.getClassNamespace() + ".proto");
                MessageDefinition.Builder msgBuilder = MessageDefinition.newBuilder(colmeta.getValueClassName());
                for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                    DataSetColumnMeta column = colmeta.getColumnList().get(i);
                    msgBuilder = msgBuilder.addField(column.isRequired() ? "required" : "optional", ProtoBufUtil.translateType(column), column.getColumnName(), i + 1);
                }
                MessageDefinition definition = msgBuilder.build();
                schemaBuilder.addMessageDefinition(definition);
                DynamicSchema schema = schemaBuilder.build();
                DynamicMessage.Builder mesgBuilder = DynamicMessage.newBuilder(schema.getMessageDescriptor(colmeta.getValueClassName()));
                Descriptors.Descriptor msgDesc = schema.getMessageDescriptor(colmeta.getValueClassName());
                container.setMesgBuilder(mesgBuilder);
                container.setSchema(schema);
                container.setDefinition(definition);
                container.setMsgDesc(msgDesc);
                return container;
            } else {
                throw new OperationNotSupportException("");
            }
        }catch (Exception ex){
            throw new OperationNotSupportException(ex);
        }
    }
    @Data
    public static class ProtoContainer{
        private DynamicSchema schema;
        private MessageDefinition definition;
        private DynamicMessage.Builder mesgBuilder;
        private Descriptors.Descriptor msgDesc;
    }
}
