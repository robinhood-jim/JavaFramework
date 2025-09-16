package com.robin.comm.fileaccess.writer;

import com.robin.comm.fileaccess.util.MockFileSystem;
import com.robin.comm.fileaccess.util.OrcUtil;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.math.BigDecimal;
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
    private int batchSize=4096;
    public static final String BACTH_SIZE="orcWrite.batchSize";

    public OrcFileWriter() {
        this.identifier = Const.FILEFORMATSTR.ORC.getValue();
    }

    public OrcFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        this.identifier = Const.FILEFORMATSTR.ORC.getValue();
        if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(BACTH_SIZE))) {
            batchSize = Integer.parseInt(colmeta.getResourceCfgMap().get(BACTH_SIZE).toString());
        }
    }
    public OrcFileWriter(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor){
        super(colmeta,accessor);
        this.identifier=Const.FILEFORMATSTR.ORC.getValue();
        if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(BACTH_SIZE))) {
            batchSize = Integer.parseInt(colmeta.getResourceCfgMap().get(BACTH_SIZE).toString());
        }
    }

    @Override
    public void beginWrite() throws IOException {
        FileSystem fs;
        CompressionKind compressionKind;
        Const.CompressType type = getCompressType();
        switch (type) {
            case COMPRESS_TYPE_GZ:
                throw new IOException("orc does not support gzip compression");
            case COMPRESS_TYPE_BZ2:
                throw new IOException("orc does not support bzip2 compression");
            case COMPRESS_TYPE_SNAPPY:
                compressionKind = CompressionKind.SNAPPY;
                break;
            case COMPRESS_TYPE_ZIP:
                throw new IOException("orc does not support zip compression");
            case COMPRESS_TYPE_LZO:
                compressionKind = CompressionKind.LZO;
                break;
            case COMPRESS_TYPE_LZ4:
                compressionKind = CompressionKind.LZ4;
                break;
            case COMPRESS_TYPE_LZMA:
                throw new IOException("orc does not support lzma compression");
            case COMPRESS_TYPE_BROTLI:
                throw new IOException("orc does not support brotli compression");
            case COMPRESS_TYPE_ZSTD:
                compressionKind = CompressionKind.ZSTD;
                break;
            case COMPRESS_TYPE_XZ:
                throw new IOException("orc does not support xz compression");
            default:
                compressionKind = CompressionKind.ZLIB;
        }

        if (Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())) {
            HDFSUtil util = new HDFSUtil(colmeta);
            conf = util.getConfig();
            fs = FileSystem.get(conf);
        } else {
            conf = new Configuration(false);
            checkAccessUtil(null);
            out = accessUtil.getRawOutputStream(ResourceUtil.getProcessPath(colmeta.getPath()));
            fs = new MockFileSystem(colmeta, out);
        }
        schema = OrcUtil.getSchema(colmeta);
        owriter = OrcFile.createWriter(new Path(colmeta.getPath()), OrcFile.writerOptions(conf).setSchema(schema).compress(compressionKind).fileSystem(fs));
        batch = schema.createRowBatch(batchSize);
    }

    @Override
    public void finishWrite() throws IOException {
        if (batch.size > 0) {
            owriter.addRowBatch(batch);
            batch.reset();
        }
        if (owriter != null) {
            owriter.close();
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {

        int row = batch.size++;
        if (!CollectionUtils.isEmpty(colmeta.getColumnList())) {

            for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                DataSetColumnMeta columnMeta = colmeta.getColumnList().get(i);
                if (StringUtils.isEmpty(columnMeta.getColumnType()) || Objects.isNull(map.get(columnMeta.getColumnName()))) {
                    if (!columnMeta.getColumnType().equals(Const.META_TYPE_STRING)) {
                        continue;
                    } else {
                        ((BytesColumnVector) batch.cols[i]).setVal(row, "".getBytes());
                    }
                }
                switch (columnMeta.getColumnType()) {
                    case Const.META_TYPE_INTEGER:
                        ((LongColumnVector) batch.cols[i]).vector[row] = Integer.valueOf(map.get(columnMeta.getColumnName()).toString());
                        break;
                    case Const.META_TYPE_SHORT:
                        ((LongColumnVector) batch.cols[i]).vector[row] = Short.valueOf(map.get(columnMeta.getColumnName()).toString());
                        break;
                    case Const.META_TYPE_BIGINT:
                        ((LongColumnVector) batch.cols[i]).vector[row] = Long.valueOf(map.get(columnMeta.getColumnName()).toString());
                        break;
                    case Const.META_TYPE_DOUBLE:
                        ((DoubleColumnVector) batch.cols[i]).vector[row] = Double.valueOf(map.get(columnMeta.getColumnName()).toString());
                        break;
                    case Const.META_TYPE_DECIMAL:
                        BigDecimal decimal = new BigDecimal(Double.valueOf(map.get(columnMeta.getColumnName()).toString()));
                        if (columnMeta.getScale() > 0) {
                            decimal.setScale(columnMeta.getScale(), BigDecimal.ROUND_DOWN);
                        }
                        HiveDecimal decimal1 = HiveDecimal.create(decimal);
                        if (columnMeta.getScale() > 0) {
                            decimal1.setScale(columnMeta.getScale());
                        }
                        ((DecimalColumnVector) batch.cols[i]).vector[row] = new HiveDecimalWritable(decimal1);
                        break;
                    case Const.META_TYPE_DATE:
                        if (Date.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())) {
                            ((LongColumnVector) batch.cols[i]).vector[row] = (((Date) map.get(columnMeta.getColumnName())).getTime());
                        } else if (java.sql.Date.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())) {
                            ((LongColumnVector) batch.cols[i]).vector[row] = (((java.sql.Date) map.get(columnMeta.getColumnName())).getTime());
                        } else if (Long.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())) {
                            ((LongColumnVector) batch.cols[i]).vector[row] = (Long.valueOf(map.get(columnMeta.getColumnName()).toString()));
                        }
                        break;
                    case Const.META_TYPE_TIMESTAMP:
                        if (Timestamp.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())) {
                            ((TimestampColumnVector) batch.cols[i]).set(row, (Timestamp) map.get(columnMeta.getColumnName()));
                        } else if (Long.class.isAssignableFrom(map.get(columnMeta.getColumnName()).getClass())) {
                            ((TimestampColumnVector) batch.cols[i]).set(row, new Timestamp(Long.valueOf(map.get(columnMeta.getColumnName()).toString())));
                        }
                        break;
                    case Const.META_TYPE_STRING:
                        if (!StringUtils.isEmpty(map.get(columnMeta.getColumnName()))) {
                            ((BytesColumnVector) batch.cols[i]).setVal(row, map.get(columnMeta.getColumnName()).toString().getBytes());
                        } else {
                            ((BytesColumnVector) batch.cols[i]).setVal(row, "".getBytes());
                        }
                        break;
                    case Const.META_TYPE_BINARY:
                    case Const.META_TYPE_BLOB:
                        ((BytesColumnVector) batch.cols[i]).setVal(row, (byte[]) map.get(columnMeta.getColumnName()));
                        break;
                    default:
                        throw new InputMismatchException("input type not support!");
                }
            }
            if (batch.size == batch.getMaxSize()) {
                owriter.addRowBatch(batch);
                batch.reset();
            }
        }
    }
}
