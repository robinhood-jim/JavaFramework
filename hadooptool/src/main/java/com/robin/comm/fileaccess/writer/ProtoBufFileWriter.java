package com.robin.comm.fileaccess.writer;


import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import org.springframework.util.CollectionUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ProtoBufFileWriter extends AbstractFileWriter {
    private DynamicSchema schema;
    private DynamicMessage message;
    private ExtensionRegistry registry;
    private DynamicSchema.Builder schemaBuilder;
    private DynamicMessage.Builder mesgBuilder;
    private MessageDefinition definition;
    protected ProtoBufFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
    }


    @Override
    public void beginWrite() throws IOException {
        try {
            if (!CollectionUtils.isEmpty(colmeta.getColumnList())) {
                schemaBuilder = DynamicSchema.newBuilder();
                schemaBuilder.setName(colmeta.getClassNamespace() + ".proto");
                MessageDefinition.Builder msgBuilder = MessageDefinition.newBuilder(colmeta.getValueClassName());
                for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                    DataSetColumnMeta column = colmeta.getColumnList().get(i);
                    msgBuilder = msgBuilder.addField(column.isRequired() ? "required" : "optional", ProtoBufUtil.translateType(column), column.getColumnName(), i + 1);
                }
                definition = msgBuilder.build();
                schemaBuilder.addMessageDefinition(definition);
                schema = schemaBuilder.build();
                mesgBuilder= DynamicMessage.newBuilder(schema.getMessageDescriptor(colmeta.getValueClassName()));
                registry=getExtension(schema,colmeta);
            }else{
                checkAccessUtil(null);
                out = accessUtil.getOutResourceByStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
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
    public void finishWrite() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        DynamicMessage.Builder msgBuilder=schema.newMessageBuilder(colmeta.getClassNamespace());
        Descriptors.Descriptor msgDesc=msgBuilder.getDescriptorForType();
        Iterator<Map.Entry<String,Object>> iter=map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,Object> entry=iter.next();
            Descriptors.FieldDescriptor des=msgDesc.findFieldByName(entry.getKey());
            if(des!=null){
                msgBuilder.setField(des,entry.getValue());
            }
        }
        DynamicMessage message=msgBuilder.build();
        message.writeDelimitedTo(out);
    }
}
