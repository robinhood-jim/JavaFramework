package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.ArrowSchemaUtils;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.compression.CompressionCodec;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ArrowFileIterator extends AbstractFileIterator {
    private Schema schema;
    private ArrowStreamReader streamReader;
    private BufferAllocator allocator;
    private VectorSchemaRoot vectorSchemaRoot;
    private int maxrows;
    private int currentbatchRow;
    private VectorSchemaRoot currentBatch;
    private long curpos=0L;
    public ArrowFileIterator() {
        identifier = Const.FILEFORMATSTR.ARROW.getValue();
    }


    public ArrowFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.ARROW.getValue();
    }
    public ArrowFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.AVRO.getValue();
        accessUtil=accessor;
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
        schema= ArrowSchemaUtils.getSchema(colmeta);
        avroSchema= AvroUtils.getSchemaFromMeta(colmeta);
        allocator=new RootAllocator(Integer.MAX_VALUE);
        vectorSchemaRoot=VectorSchemaRoot.create(schema,allocator);
        streamReader=new ArrowStreamReader(instream,allocator);

    }

    @Override
    public void close() throws IOException {
        if(streamReader!=null){
            streamReader.close();
            vectorSchemaRoot.close();
            allocator.close();
        }
        super.close();
    }

    @Override
    protected void pullNext() {
        Assert.notNull(streamReader,"");
        boolean hasRec=false;
        try {
            cachedValue.clear();
            if((currentbatchRow==0|| maxrows==0) || (currentbatchRow>0 && currentbatchRow>=maxrows)) {
                hasRec = streamReader.loadNextBatch();
                currentbatchRow=0;
                if(!hasRec){
                    return;
                }
                currentBatch= streamReader.getVectorSchemaRoot();
                maxrows=currentBatch.getRowCount();
            }
            List<Field> fields=schema.getFields();
            if(!CollectionUtils.isEmpty(fields)){
                for(int i=0;i<fields.size();i++){
                    wrapValue(fields.get(i),fields.get(i).getName(),colmeta.getColumnList().get(i).getColumnType(),currentbatchRow,cachedValue);
                }
            }
            currentbatchRow++;
            curpos++;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void wrapValue(Field field, String columnName, String columnType, int row, Map<String,Object> valueMap) throws UnsupportedEncodingException {
        FieldVector vectors=currentBatch.getVector(field);
        if(IntVector.class.isAssignableFrom(vectors.getClass())){
            IntVector intVector=(IntVector) vectors;
            if(Const.META_TYPE_INTEGER.equals(columnType)){
                valueMap.put(columnName,intVector.get(row));
            }else if(Const.META_TYPE_BIGINT.equals(columnType)){
                valueMap.put(columnName,intVector.getValueAsLong(row));
            }
        }else if(BigIntVector.class.isAssignableFrom(vectors.getClass())){
            BigIntVector bVector=(BigIntVector) vectors;
            if(Const.META_TYPE_INTEGER.equals(columnType)){
                valueMap.put(columnName,Integer.valueOf(String.valueOf(bVector.get(row))));
            }else if(Const.META_TYPE_BIGINT.equals(columnType)){
                valueMap.put(columnName,bVector.getValueAsLong(row));
            }
        }else if(TimeStampVector.class.isAssignableFrom(vectors.getClass())){
            valueMap.put(columnName,new Timestamp(((TimeStampVector)vectors).get(row)));
        }else if(Const.META_TYPE_STRING.equals(columnType)){
            valueMap.put(columnName,new String(((VarCharVector)vectors).get(row),"utf-8"));
        }
    }
}
