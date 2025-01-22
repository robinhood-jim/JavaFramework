package com.robin.comm.fileaccess.fs;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.hadoop.hdfs.HDFSProperty;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Singleton HDFS FileSystem Accessor,using defaultName as key
 */
public class HdfsFileSystemAccessor extends AbstractFileSystemAccessor {
    private static final Logger logger = LoggerFactory.getLogger(HdfsFileSystemAccessor.class);
    private HDFSUtil hdfsUtil;

    public HdfsFileSystemAccessor() {
        this.identifier = Const.FILESYSTEM.HDFS.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        super.init(meta);
        hdfsUtil = getHdfsUtil(meta);
    }

    @Override
    public Pair<BufferedReader, InputStream> getInResourceByReader(String resourcePath)
            throws IOException {
        InputStream stream = hdfsUtil.getHDFSDataByRawInputStream(resourcePath);
        return Pair.of(getReaderByPath(resourcePath, stream, colmeta.getEncode()), stream);
    }

    @Override
    public Pair<BufferedWriter, OutputStream> getOutResourceByWriter(String resourcePath)
            throws IOException {
        OutputStream outputStream = null;
        try {
            if (hdfsUtil.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                hdfsUtil.delete(colmeta.getPath());
            }
            outputStream = hdfsUtil.getHDFSRawOutputStream(resourcePath);
            return Pair.of(getWriterByPath(resourcePath, outputStream, colmeta.getEncode()), outputStream);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getOutResourceByStream(String resourcePath)
            throws IOException {
        try {
            if (hdfsUtil.exists(resourcePath)) {
                logger.error("output file {} exist!,remove it", resourcePath);
                hdfsUtil.delete(resourcePath);
            }
            return getOutputStreamByPath(resourcePath, hdfsUtil.getHDFSDataByOutputStream(resourcePath));
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getRawOutputStream(String resourcePath) throws IOException {
        try {
            if (hdfsUtil.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                hdfsUtil.delete(resourcePath);
            }
            return hdfsUtil.getHDFSRawOutputStream(resourcePath);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getRawInputStream(String resourcePath) throws IOException {
        try {
            if (hdfsUtil.exists(resourcePath)) {
                return hdfsUtil.getHDFSDataByRawInputStream(resourcePath);
            } else {
                throw new IOException("path " + resourcePath + " not found");
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getInResourceByStream(String resourcePath)
            throws IOException {
        try {
            if (hdfsUtil.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                hdfsUtil.delete(resourcePath);
            }
            return getInputStreamByPath(resourcePath, hdfsUtil.getHDFSDataByInputStream(resourcePath));
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public HDFSUtil getHdfsUtil(DataCollectionMeta meta) {
        HDFSUtil util;
        HDFSProperty property = new HDFSProperty();
        property.setHaConfigByObj(meta.getResourceCfgMap());
        util = new HDFSUtil(property);
        return util;
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        try {
            return hdfsUtil.exists(resourcePath);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        try {
            if (hdfsUtil.exists(resourcePath)) {
                return hdfsUtil.getHDFSFileSize(resourcePath);
            } else {
                throw new IOException("path " + resourcePath + " not found");
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
