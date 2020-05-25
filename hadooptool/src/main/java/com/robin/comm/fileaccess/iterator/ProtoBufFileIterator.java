package com.robin.comm.fileaccess.iterator;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.fileaccess.iterator</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年11月28日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ProtoBufFileIterator extends AbstractFileIterator {
    private DynamicSchema schema;
    private DynamicMessage message;
    private ExtensionRegistry registry;
    private DynamicSchema.Builder schemaBuilder;
    private DynamicMessage.Builder mesgBuilder;

    public ProtoBufFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }

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

    @Override
    public void init()  {
        try {
            if (!colmeta.getColumnList().isEmpty()) {
                schemaBuilder = DynamicSchema.newBuilder();
                schemaBuilder.setName(colmeta.getClassNamespace() + ".proto");
                MessageDefinition.Builder msgBuilder = MessageDefinition.newBuilder(colmeta.getValueClassName());
                for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                    DataSetColumnMeta column = colmeta.getColumnList().get(i);
                    msgBuilder = msgBuilder.addField(column.isRequired() ? "required" : "optional", translateType(column), column.getColumnName(), i + 1);
                }
                MessageDefinition definition = msgBuilder.build();
                schemaBuilder.addMessageDefinition(definition);
                schema = schemaBuilder.build();
                mesgBuilder=DynamicMessage.newBuilder(schema.getMessageDescriptor(colmeta.getValueClassName()));
                registry=getExtension(schema,colmeta);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
    public static  ExtensionRegistry getExtension(DynamicSchema schema, DataCollectionMeta colmeta){
        ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        for(Descriptors.FieldDescriptor descriptor:schema.getMessageDescriptor(colmeta.getValueClassName()).getFields()){
            extensionRegistry.add(descriptor);
        }
        return extensionRegistry;
    }

    @Override
    public boolean hasNext() {
        try {
            if (mesgBuilder.mergeDelimitedFrom(instream)) {
                message=mesgBuilder.build();
                return true;
            }else {
                return false;
            }
        }catch (Exception ex){

        }
        return false;
    }

    @Override
    public Map<String, Object> next() {
        Map<String,Object> tmap=new HashMap<String, Object>();
        for(Descriptors.FieldDescriptor descriptor:schema.getMessageDescriptor(colmeta.getValueClassName()).getFields()){
            tmap.put(descriptor.getName(),message.getField(descriptor));
        }
        return tmap;
    }

    @Override
    public void remove() {
        try {
            if (mesgBuilder.mergeDelimitedFrom(instream, registry)) {
                mesgBuilder.build();
            }
        }catch (Exception ex){

        }
    }
}
