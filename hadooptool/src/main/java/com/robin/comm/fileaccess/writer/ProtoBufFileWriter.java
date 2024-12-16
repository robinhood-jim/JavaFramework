package com.robin.comm.fileaccess.writer;



import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.base.util.Const;
import com.robin.core.compress.util.CompressEncoder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public class ProtoBufFileWriter extends AbstractFileWriter {
    private DynamicSchema schema;
    private DynamicSchema.Builder schemaBuilder;
    private DynamicMessage.Builder mesgBuilder;
    private MessageDefinition definition;
    private MessageDefinition.Builder msgBuilder;
    private Descriptors.Descriptor msgDesc;
    private OutputStream wrapOutputStream;
    public ProtoBufFileWriter(){
        this.identifier= Const.FILEFORMATSTR.PROTOBUF.getValue();
    }
    public ProtoBufFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        this.identifier= Const.FILEFORMATSTR.PROTOBUF.getValue();
    }


    @Override
    public void beginWrite() throws IOException {
        try {
            if (!CollectionUtils.isEmpty(colmeta.getColumnList())) {
                schemaBuilder = DynamicSchema.newBuilder();
                schemaBuilder.setName(colmeta.getClassNamespace() + ".proto");
                msgBuilder = MessageDefinition.newBuilder(colmeta.getValueClassName());
                for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                    DataSetColumnMeta column = colmeta.getColumnList().get(i);
                    msgBuilder = msgBuilder.addField(column.isRequired() ? "required" : "optional", ProtoBufUtil.translateType(column), column.getColumnName(), i + 1);
                }
                definition = msgBuilder.build();
                schemaBuilder.addMessageDefinition(definition);
                schema = schemaBuilder.build();
                mesgBuilder= DynamicMessage.newBuilder(schema.getMessageDescriptor(colmeta.getValueClassName()));
                msgDesc=schema.getMessageDescriptor(colmeta.getValueClassName());
            }
            checkAccessUtil(null);
            out=accessUtil.getRawOutputStream(colmeta,ResourceUtil.getProcessPath(colmeta.getPath()));
            wrapOutputStream = CompressEncoder.getOutputStreamByCompressType(ResourceUtil.getProcessPath(colmeta.getPath()),out); //accessUtil.getOutResourceByStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
            getCompressType();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void finishWrite() throws IOException {
        wrapOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        wrapOutputStream.flush();
    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        Iterator<Map.Entry<String,Object>> iter=map.entrySet().iterator();
        mesgBuilder.clear();
        while(iter.hasNext()){
            Map.Entry<String,Object> entry=iter.next();
            Descriptors.FieldDescriptor des=msgDesc.findFieldByName(entry.getKey());
            if(des!=null && !ObjectUtils.isEmpty(entry.getValue())){
                mesgBuilder.setField(des,entry.getValue());
            }
        }
        DynamicMessage message=mesgBuilder.build();
        message.writeDelimitedTo(wrapOutputStream);
    }
}
