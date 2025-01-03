package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.MockFileSystem;
import com.robin.comm.utils.SysUtils;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.commons.io.FileUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrcFileIterator extends AbstractFileIterator {
    private Configuration conf;
    private List<TypeDescription> fields;
    private TypeDescription schema;
    private RecordReader rows ;
    private VectorizedRowBatch batch ;
    private MemorySegment segment;
    private Double allowOffHeapDumpLimit= ResourceConst.ALLOWOUFHEAPMEMLIMIT;

    public OrcFileIterator(){
        identifier= Const.FILEFORMATSTR.ORC.getValue();
    }
    public OrcFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.ORC.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
    }
    public OrcFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.ORC.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
        accessUtil=accessor;
    }

    private final Map<String,Object> valueMap=new HashMap<>();
    int maxRow=-1;
    int currentRow=0;
    private FileSystem fs;
    private Reader oreader;
    private File tmpFile;


    @Override
    public boolean hasNext() {
        if(maxRow>0 && currentRow<maxRow-1){
            return true;
        }
        try{
            currentRow=0;
            boolean exist= rows.nextBatch(batch);
            maxRow=batch.size;
            return exist;
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, Object> next() {
        List<String> fieldNames=schema.getFieldNames();
        valueMap.clear();
        currentRow++;
        if(!CollectionUtils.isEmpty(fields)){
            for(int i=0;i<fields.size();i++){
                wrapValue(fields.get(i),fieldNames.get(i),batch.cols[i],currentRow,valueMap);
            }
        }
        return valueMap;
    }
    public void wrapValue(TypeDescription schema,String columnName, ColumnVector vector,int row,Map<String,Object> valueMap){
        if(vector.noNulls || !vector.isNull[row]){
            switch (schema.getCategory()){
                case BOOLEAN:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]!=0);
                    break;
                case SHORT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).shortValue());
                    break;
                case INT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).intValue());
                    break;
                case LONG:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]);
                    break;
                case FLOAT:
                case DOUBLE:
                    valueMap.put(columnName,((DoubleColumnVector)vector).vector[row]);
                    break;
                case DECIMAL:
                    valueMap.put(columnName,((DecimalColumnVector)vector).vector[row].doubleValue());
                    break;
                case STRING:
                case CHAR:
                case VARCHAR:
                    valueMap.put(columnName,((BytesColumnVector)vector).toString(row));
                    break;
                case DATE:
                    valueMap.put(columnName,new Timestamp(((LongColumnVector)vector).vector[row]));
                    break;
                case TIMESTAMP:
                case TIMESTAMP_INSTANT:
                    valueMap.put(columnName,((TimestampColumnVector)vector).asScratchTimestamp(row));
                    break;
                case LIST:
                case MAP:
                case STRUCT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type " + schema.toString());
            }
        }
    }

    @Override
    public void beforeProcess() {
        try {
            String readPath=colmeta.getPath();
            if(Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())){
                HDFSUtil util=new HDFSUtil(colmeta);
                conf=util.getConfig();
                fs=FileSystem.get(conf);
            }else {
                checkAccessUtil(null);
                if(Const.FILESYSTEM.LOCAL.getValue().equals(colmeta.getFsType())){
                    fs=FileSystem.get(new Configuration());
                    readPath=new File(readPath).toURI().toString();
                }else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    Double freeMemory= SysUtils.getFreeMemory();
                    if (size < ResourceConst.MAX_ARRAY_SIZE && freeMemory>allowOffHeapDumpLimit) {
                        //use flink memory utils to use offHeapMemory to dump file content
                        segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory((int)size,this,new Thread(){});
                        ByteBuffer byteBuffer=segment.getOffHeapBuffer();
                        try(ReadableByteChannel channel= Channels.newChannel(instream)) {
                            org.apache.commons.io.IOUtils.readFully(channel, byteBuffer);
                            byteBuffer.position(0);
                        }
                        fs=new MockFileSystem(conf,byteBuffer);
                    } else {
                        String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(colmeta);
                        String tmpFilePath = "file:///" + tmpPath + ResourceUtil.getProcessFileName(colmeta.getPath());
                        tmpFile = new File(new URL(tmpFilePath).toURI());
                        copyToLocal(tmpFile, instream);
                        fs = FileSystem.get(new Configuration());
                        readPath = tmpFilePath;
                    }
                }
            }
            oreader =OrcFile.createReader(new Path(readPath),OrcFile.readerOptions(conf).filesystem(fs));
            schema= oreader.getSchema();
            rows= oreader.rows();
            fields=schema.getChildren();
            batch= oreader.getSchema().createRowBatch();
            maxRow=batch.size;

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if(rows!=null){
            rows.close();
        }
        if(!ObjectUtils.isEmpty(fs)){
            fs.close();
        }
        if(!ObjectUtils.isEmpty(oreader)){
            oreader.close();
        }
        if(!ObjectUtils.isEmpty(segment)){
            segment.free();
        }
        if(!ObjectUtils.isEmpty(tmpFile)){
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
