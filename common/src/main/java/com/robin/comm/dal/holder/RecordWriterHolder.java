package com.robin.comm.dal.holder;

import com.robin.core.base.exception.OperationInWorkException;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.IResourceWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.resaccess.CommResWriterFactory;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;


public class RecordWriterHolder extends AbstractResourceHolder {
    private IResourceWriter writer;

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {
        if(writer!=null || busyTag){
            throw new OperationInWorkException("last Opertaion Writer already Exists.May not be shutdown Propery");
        }
        URI uri=new URI(colmeta.getPath());
        String schema=uri.getScheme();
        String path=uri.getPath();
        AbstractFileSystemAccessor util = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
        OutputStream outStream = util.getOutResourceByStream(colmeta, path);
        if(!colmeta.isFsTag()) {
            writer = TextFileWriterFactory.getWriterByPath(colmeta, outStream);
        } else{
            writer= CommResWriterFactory.getFileWriterByType(colmeta.getResType(),colmeta);
        }
    }
    public void writeRecord(Map<String,Object> map) throws IOException, OperationNotSupportedException {
        writer.writeRecord(map);
    }

    @Override
    public void close() {
        try {
            if(writer!=null){
                if(AbstractFileWriter.class.isAssignableFrom(writer.getClass())) {
                    ((AbstractFileWriter)writer).finishWrite();
                    ((AbstractFileWriter)writer).flush();
                }
                writer.close();
            }
        }catch (Exception ex){

        }finally {
            writer=null;
            busyTag=false;
        }

    }
}
