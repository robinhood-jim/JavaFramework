package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.FileSeekableInputStream;
import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.comm.fileaccess.util.SeekableInputStream;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.ApacheVfsFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class ParquetFileIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> preader;
    private Schema schema;
    private MessageType msgtype;
    private GenericData.Record record;
    private ParquetReader<Map> ireader;
    private boolean useAvroEncode = false;
    public ParquetFileIterator(){
        identifier= Const.FILEFORMATSTR.PARQUET.getValue();
    }
    public ParquetFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.PARQUET.getValue();
    }

    private List<Schema.Field> fields;
    Map<String, Object> rsMap;
    private FileSeekableInputStream seekableInputStream = null;
    private File tmpFile;

    @Override
    public void beforeProcess() {
        Configuration conf;
        InputFile file;
        // max allowable parquet file size to load in memory for no hdfs input
        long maxSize=1024L*1024L*300L;
        try {
            checkAccessUtil(null);
            if(colmeta.getResourceCfgMap().containsKey("parquetMaxLoadableSize")){
                maxSize=Long.parseLong(colmeta.getResourceCfgMap().get("parquetMaxLoadableSize").toString());
            }
            if (colmeta.getResourceCfgMap().containsKey("file.useAvroEncode") && "true".equalsIgnoreCase(colmeta.getResourceCfgMap().get("file.useAvroEncode").toString())) {
                useAvroEncode = true;
            }
            if (ResourceConst.IngestType.TYPE_HDFS.getValue().equals(colmeta.getSourceType())) {
                conf = new HDFSUtil(colmeta).getConfig();
                if (colmeta.getColumnList().isEmpty()) {
                    ParquetMetadata meta = ParquetFileReader.readFooter(conf, new Path(colmeta.getPath()), ParquetMetadataConverter.NO_FILTER);
                    msgtype = meta.getFileMetaData().getSchema();
                    parseSchemaByType();
                } else {
                    schema = AvroUtils.getSchemaFromMeta(colmeta);
                }
                if (!useAvroEncode) {
                    ParquetReader.Builder<Map> builder=ParquetReader.builder(new CustomReadSupport(colmeta),new Path(ResourceUtil.getProcessPath(colmeta.getPath()))).withConf(conf);
                    ireader = builder.build();
                } else {
                    preader = AvroParquetReader
                            .<GenericData.Record>builder(HadoopInputFile.fromPath(new Path(ResourceUtil.getProcessPath(colmeta.getPath())), conf)).withConf(conf).build();
                }
            } else {
                // no hdfs input source
                if (ResourceConst.IngestType.TYPE_LOCAL.getValue().equals(colmeta.getSourceType())) {
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    seekableInputStream = new FileSeekableInputStream(colmeta.getPath());
                    file = ParquetUtil.makeInputFile(seekableInputStream, size);
                } else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = instream.available();//accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    //file size too large ,may cause OOM,download as tmpfile
                    if(size>maxSize) {
                        String tmpPath = (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get("output.tmppath"))) ? colmeta.getResourceCfgMap().get("output.tmppath").toString() : FileUtils.getUserDirectoryPath();
                        String tmpFilePath = "file:///" + tmpPath + ResourceUtil.getProcessFileName(colmeta.getPath());
                        tmpFile = new File(new URL(tmpFilePath).toURI());
                        copyToLocal(tmpFile, instream);
                        seekableInputStream = new FileSeekableInputStream(tmpPath);
                        file = ParquetUtil.makeInputFile(seekableInputStream, size);
                    }else{
                        ByteArrayOutputStream byteout=new ByteArrayOutputStream((int)size);
                        IOUtils.copyBytes(instream,byteout,8000);
                        SeekableInputStream seekableInputStream=new SeekableInputStream(byteout.toByteArray());
                        file = ParquetUtil.makeInputFile(seekableInputStream);
                    }
                }
                if (colmeta.getColumnList().isEmpty()) {
                    ParquetReadOptions options= ParquetReadOptions.builder().withMetadataFilter(ParquetMetadataConverter.NO_FILTER).build();
                    ParquetFileReader ireader=ParquetFileReader.open(file,options);
                    ParquetMetadata meta =ireader.getFooter(); //ParquetFileReader.readFooter(file, ParquetMetadataConverter.NO_FILTER);
                    msgtype = meta.getFileMetaData().getSchema();
                    parseSchemaByType();
                    //read footer and schema,must return header
                    file.newStream().seek(0L);
                } else {
                    schema = AvroUtils.getSchemaFromMeta(colmeta);
                }
                if (!useAvroEncode) {
                    fields = schema.getFields();
                    ireader = CustomParquetReader.builder(file, colmeta).build();
                } else {
                    preader = AvroParquetReader.<GenericData.Record>builder(file).build();
                }
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }

    }

    @Override
    public boolean hasNext() {
        try {
            if (useAvroEncode) {
                record = null;
                record = preader.read();
                return record != null;
            } else {
                rsMap = ireader.read();
                return rsMap != null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> next() {
        if (useAvroEncode) {
            Map<String, Object> retMap = new HashMap<>();
            if (record == null) {
                throw new NoSuchElementException("");
            }
            for (Schema.Field field : fields) {
                retMap.put(field.name(), record.get(field.name()));
            }
            return retMap;
        } else {
            return rsMap;
        }
    }

    private void parseSchemaByType() {
        List<Type> colList = msgtype.getFields();

        for (Type type : colList) {
            colmeta.addColumnMeta(type.getName(), ParquetReaderUtil.parseColumnType(type.asPrimitiveType()), null);
        }
        schema = AvroUtils.getSchemaFromMeta(colmeta);
    }


    public Schema getSchema() {
        return schema;
    }

    @Override
    public void remove() {
        try {
            reader.read();
            if(tmpFile!=null){
                FileUtils.deleteQuietly(tmpFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MessageType getMessageType() {
        return msgtype;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if(!ObjectUtils.isEmpty(ireader)){
            ireader.close();
        }
        if(!ObjectUtils.isEmpty(preader)){
            preader.close();
        }
        if(!ObjectUtils.isEmpty(seekableInputStream)) {
            seekableInputStream.closeQuitly();
        }
        if(!ObjectUtils.isEmpty(tmpFile)){
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
