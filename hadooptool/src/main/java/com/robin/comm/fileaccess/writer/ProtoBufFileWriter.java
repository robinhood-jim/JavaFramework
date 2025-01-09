package com.robin.comm.fileaccess.writer;


import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.base.util.Const;
import com.robin.core.compress.util.CompressEncoder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import org.springframework.util.ObjectUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public class ProtoBufFileWriter extends AbstractFileWriter {

    private ProtoBufUtil.ProtoContainer container;
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
            container=ProtoBufUtil.initSchema(colmeta);
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
        container.getMesgBuilder().clear();
        while(iter.hasNext()){
            Map.Entry<String,Object> entry=iter.next();
            Descriptors.FieldDescriptor des=container.getMsgDesc().findFieldByName(entry.getKey());
            if(des!=null && !ObjectUtils.isEmpty(entry.getValue())){
                container.getMesgBuilder().setField(des,entry.getValue());
            }
        }
        DynamicMessage message=container.getMesgBuilder().build();
        message.writeDelimitedTo(wrapOutputStream);
    }
}
