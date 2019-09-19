package com.robin.core.fileaccess.holder;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.pool.ResourceAccessHolder;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * <p>Created at: 2019-09-19 17:50:12</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class BufferedWriterHolder extends AbstractResourceHolder {
    private AbstractFileWriter writer;

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {
        String[] tag = AbstractResourceAccessUtil.retrieveResource(colmeta.getPath());
        AbstractResourceAccessUtil util = ResourceAccessHolder.getAccessUtilByProtocol(tag[0].toLowerCase());
        OutputStream inputStream = util.getOutResourceByStream(colmeta);
        writer = TextFileWriterFactory.getFileWriterByPath(colmeta, inputStream);
    }
    public void writeRecord(Map<String,Object> map) throws IOException{
        writer.writeRecord(map);
    }

    @Override
    public void close() {
        try {
            if(writer!=null){
                writer.finishWrite();
                writer.flush();
                writer.close();
            }
        }catch (Exception ex){

        }finally {
            writer=null;
            busyTag=false;
        }

    }
}
