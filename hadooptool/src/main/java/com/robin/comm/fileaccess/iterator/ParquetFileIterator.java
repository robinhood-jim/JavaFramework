package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.ByteBufferSeekableInputStream;
import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.comm.utils.SysUtils;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
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
import org.apache.parquet.io.LocalInputFile;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.apache.slider.server.appmaster.management.Timestamp;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class ParquetFileIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> preader;
    private Schema schema;
    private MessageType msgtype;
    private GenericData.Record record;
    private ParquetReader<Map<String, Object>> ireader;
    private boolean useAvroEncode = false;
    private MemorySegment segment;
    private Double allowOffHeapDumpLimit= ResourceConst.ALLOWOUFHEAPMEMLIMIT;

    public ParquetFileIterator() {
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
    }

    public ParquetFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
    }
    public ParquetFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
        accessUtil=accessor;
    }

    private List<Schema.Field> fields;
    Map<String, Object> rsMap;
    private File tmpFile;

    @Override
    public void beforeProcess() {
        Configuration conf;
        InputFile file;
        try {
            checkAccessUtil(null);
            if (colmeta.getResourceCfgMap().containsKey("file.useAvroEncode") && "true".equalsIgnoreCase(colmeta.getResourceCfgMap().get("file.useAvroEncode").toString())) {
                useAvroEncode = true;
            }

            if (Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())) {
                conf = new HDFSUtil(colmeta).getConfig();
                file=HadoopInputFile.fromPath(new Path(colmeta.getPath()), conf);
                getSchema(file,false);
                if (!useAvroEncode) {
                    ParquetReader.Builder<Map<String, Object>> builder = ParquetReader.builder(new CustomReadSupport(), new Path(ResourceUtil.getProcessPath(colmeta.getPath()))).withConf(conf);
                    ireader = builder.build();
                } else {
                    preader = AvroParquetReader
                            .<GenericData.Record>builder(HadoopInputFile.fromPath(new Path(ResourceUtil.getProcessPath(colmeta.getPath())), conf)).withConf(conf).build();
                }
            } else {
                // no hdfs input source
                if (Const.FILESYSTEM.LOCAL.getValue().equals(colmeta.getFsType())) {
                    file = new LocalInputFile(Paths.get(colmeta.getPath()));
                } else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    Double freeMemory= SysUtils.getFreeMemory();
                    //file size too large ,can not store in ByteBuffer or freeMemory too low
                    if (size >= ResourceConst.MAX_ARRAY_SIZE || freeMemory<allowOffHeapDumpLimit) {
                        String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(colmeta);
                        String tmpFilePath = "file:///" + tmpPath + ResourceUtil.getProcessFileName(colmeta.getPath());
                        tmpFile = new File(new URL(tmpFilePath).toURI());
                        copyToLocal(tmpFile, instream);
                        file = new LocalInputFile(Paths.get(new URI(tmpFilePath)));
                    } else {
                        //use flink memory utils to use offHeapMemory to dump file content
                        segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory((int)size,this,new Thread(){});
                        ByteBuffer byteBuffer=segment.getOffHeapBuffer();
                        try(ReadableByteChannel channel= Channels.newChannel(instream)) {
                            IOUtils.readFully(channel, byteBuffer);
                            byteBuffer.position(0);
                            ByteBufferSeekableInputStream seekableInputStream = new ByteBufferSeekableInputStream(byteBuffer);
                            file = ParquetUtil.makeInputFile(seekableInputStream);
                        }
                    }
                }
                getSchema(file,true);

                if (!useAvroEncode) {
                    ireader = CustomParquetReader.builder(file, colmeta).build();
                } else {
                    preader = AvroParquetReader.<GenericData.Record>builder(file).build();
                }
            }
            fields = schema.getFields();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }

    }

    private void getSchema(InputFile file,boolean seekFrist) throws IOException {
        if (colmeta.getColumnList().isEmpty()) {
            ParquetReadOptions options = ParquetReadOptions.builder().withMetadataFilter(ParquetMetadataConverter.NO_FILTER).build();
            try(ParquetFileReader ireader = ParquetFileReader.open(file, options)) {
                ParquetMetadata meta = ireader.getFooter();
                msgtype = meta.getFileMetaData().getSchema();
                parseSchemaByType();
                if (seekFrist) {
                    file.newStream().seek(0L);
                }
            }
        } else {
            schema = AvroUtils.getSchemaFromMeta(colmeta);
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
                Object value=record.get(field.name());
                if(LogicalTypes.timestampMillis().equals(field.schema().getLogicalType())){
                    value=new Timestamp((Long)value);
                }
                retMap.put(field.name(), value);
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
        if (!ObjectUtils.isEmpty(ireader)) {
            ireader.close();
        }
        if (!ObjectUtils.isEmpty(preader)) {
            preader.close();
        }
        //free offHeap memory
        if(!ObjectUtils.isEmpty(segment)){
            segment.free();
        }

        if (!ObjectUtils.isEmpty(tmpFile)) {
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
