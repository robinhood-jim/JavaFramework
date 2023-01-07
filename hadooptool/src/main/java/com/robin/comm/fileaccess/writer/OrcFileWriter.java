package com.robin.comm.fileaccess.writer;

import com.robin.comm.fileaccess.util.MockFileSystem;
import com.robin.comm.fileaccess.util.OrcUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Objects;


public class OrcFileWriter extends AbstractFileWriter {
    private Configuration conf;
    private TypeDescription schema;
    private Writer owriter;
    private VectorizedRowBatch batch;
    public OrcFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
    }

    @Override
    public void beginWrite() throws IOException {
        FileSystem fs;
        if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
            HDFSUtil util=new HDFSUtil(colmeta);
            conf=util.getConfig();
            fs= FileSystem.get(conf);
        }else{
            conf=new Configuration();
            checkAccessUtil(null);
            //out=accessUtil.getOutResourceByStream(colmeta,getOutputPath(colmeta.getPath()));
            fs=new MockFileSystem(colmeta,accessUtil);
        }
        schema= OrcUtil.getSchema(colmeta);
        owriter= OrcFile.createWriter(new Path(colmeta.getPath()), OrcFile.writerOptions(conf).setSchema(schema).fileSystem(fs));
        batch=schema.createRowBatch();
    }

    @Override
    public void finishWrite() throws IOException {
        if(batch.size>0){
            owriter.addRowBatch(batch);
            batch.reset();
        }
        if(owriter!=null){
            //owriter.notify();
            owriter.close();
        }
    }

    @Override
    public void flush() throws IOException {
        if(owriter!=null){
            owriter.notify();
        }
    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        if(batch.size==batch.getMaxSize()){
            owriter.addRowBatch(batch);
            batch.reset();
        }else{
            int row=batch.size++;
            if(!CollectionUtils.isEmpty(colmeta.getColumnList())){

                for(int i=0;i<colmeta.getColumnList().size();i++){
                    DataSetColumnMeta columnMeta=colmeta.getColumnList().get(i);
                    if(StringUtils.isEmpty(columnMeta.getColumnType()) || Objects.isNull(map.get(columnMeta.getColumnName()))){
                        if(!columnMeta.getColumnType().equals(Const.META_TYPE_STRING)) {
                            continue;
                        }
                        else{
                            ((BytesColumnVector) batch.cols[i]).setVal(row, "".getBytes());
                        }
                    }
                    switch (columnMeta.getColumnType()){
                        case Const.META_TYPE_INTEGER:
                            ((LongColumnVector) batch.cols[i]).vector[row]=Integer.valueOf(map.get(columnMeta.getColumnName()).toString());
                            break;
                        case Const.META_TYPE_SHORT:
                            ((LongColumnVector) batch.cols[i]).vector[row]=Short.valueOf(map.get(columnMeta.getColumnName()).toString());
                            break;
                        case Const.META_TYPE_BIGINT:
                            ((LongColumnVector) batch.cols[i]).fill(Long.valueOf(map.get(columnMeta.getColumnName()).toString()));
                            break;
                        case Const.META_TYPE_DOUBLE:
                            ((DoubleColumnVector) batch.cols[i]).fill(Double.valueOf(map.get(columnMeta.getColumnName()).toString()));
                            break;
                        case Const.META_TYPE_DATE:
                            if(Date.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())){
                                ((LongColumnVector) batch.cols[i]).fill(((Date)map.get(columnMeta.getColumnName())).getTime());
                            }else if(java.sql.Date.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())){
                                ((LongColumnVector) batch.cols[i]).fill(((java.sql.Date)map.get(columnMeta.getColumnName())).getTime());
                            }else if(Long.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())){
                                ((LongColumnVector) batch.cols[i]).fill(Long.valueOf(map.get(columnMeta.getColumnName()).toString()));
                            }
                            break;
                        case Const.META_TYPE_TIMESTAMP:
                            if(Timestamp.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())){
                                ((TimestampColumnVector) batch.cols[i]).fill((Timestamp)map.get(columnMeta.getColumnName()));
                            }else if(Long.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())){
                                ((TimestampColumnVector) batch.cols[i]).fill( new Timestamp(Long.valueOf(map.get(columnMeta.getColumnName()).toString())));
                            }
                            break;
                        case Const.META_TYPE_STRING:
                            if(!StringUtils.isEmpty(map.get(columnMeta.getColumnName()))) {
                                ((BytesColumnVector) batch.cols[i]).setVal(row, map.get(columnMeta.getColumnName()).toString().getBytes());
                            }
                            else{
                                ((BytesColumnVector) batch.cols[i]).setVal(row, "".getBytes());
                            }
                            break;
                        case Const.META_TYPE_BINARY:
                        case Const.META_TYPE_BLOB:
                            ((BytesColumnVector) batch.cols[i]).setVal(row,(byte[])map.get(columnMeta.getColumnName()));
                            break;
                        default:
                            throw new InputMismatchException("input type not support!");
                    }
                }
            }
        }
    }
}
