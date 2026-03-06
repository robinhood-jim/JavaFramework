package com.robin.core.resaccess.iterator;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueueIterator extends AbstractResIterator {
    protected AbstractQueueIterator(DataCollectionMeta colmeta){
        super(colmeta);
    }
    protected Schema schema;
    protected Map<String,Object> cfgMap;
    protected Injection<GenericRecord,byte[]> recordInjection;
    public AbstractQueueIterator(){

    }

    @Override
    public void beforeProcess() {
        Assert.notNull(colmeta,"");
        cfgMap=colmeta.getResourceCfgMap();
        if(!CollectionUtils.isEmpty(colmeta.getColumnList())) {
            schema = AvroUtils.getSchemaFromMeta(colmeta);
            recordInjection = GenericAvroCodecs.toBinary(schema);
        }
    }

    @Override
    public void afterProcess() {

    }
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Map<String, Object> next() {
        return null;
    }
    public abstract List<Map<String,Object>> pollMessage() throws IOException;

    @Override
    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        throw new OperationNotSupportException("");
    }
}
