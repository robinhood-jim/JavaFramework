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
    private ThreadLocal<HDFSUtil> localUtils = new ThreadLocal<>();

    public HdfsFileSystemAccessor() {
        this.identifier = Const.FILESYSTEM.HDFS.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        super.init(meta);
        HDFSUtil util = getHdfsUtil(meta);
        localUtils.set(util);
    }

    @Override
    public Pair<BufferedReader, InputStream> getInResourceByReader(String resourcePath)
            throws IOException {
        HDFSUtil util = localUtils.get();
        InputStream stream = util.getHDFSDataByRawInputStream(resourcePath);
        return Pair.of(getReaderByPath(resourcePath, stream, colmetaLocal.get().getEncode()), stream);
    }

    @Override
    public Pair<BufferedWriter, OutputStream> getOutResourceByWriter(String resourcePath)
            throws IOException {
        HDFSUtil util = localUtils.get();
        OutputStream outputStream = null;
        try {
            if (util.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                util.delete(colmetaLocal.get().getPath());
            }
            outputStream = util.getHDFSRawOutputStream(resourcePath);
            return Pair.of(getWriterByPath(resourcePath, outputStream, colmetaLocal.get().getEncode()), outputStream);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getOutResourceByStream(String resourcePath)
            throws IOException {
        HDFSUtil util = localUtils.get();
        try {
            if (util.exists(resourcePath)) {
                logger.error("output file {} exist!,remove it", resourcePath);
                util.delete(resourcePath);
            }
            return getOutputStreamByPath(resourcePath, util.getHDFSDataByOutputStream(resourcePath));
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getRawOutputStream(String resourcePath) throws IOException {
        HDFSUtil util = localUtils.get();
        try {
            if (util.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                util.delete(resourcePath);
            }
            return util.getHDFSRawOutputStream(resourcePath);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getRawInputStream(String resourcePath) throws IOException {
        HDFSUtil util = localUtils.get();
        try {
            if (util.exists(resourcePath)) {
                return util.getHDFSDataByRawInputStream(resourcePath);
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
        HDFSUtil util = localUtils.get();
        try {
            if (util.exists(resourcePath)) {
                logger.error("output file {}  exist!,remove it", resourcePath);
                util.delete(resourcePath);
            }
            return getInputStreamByPath(resourcePath, util.getHDFSDataByInputStream(resourcePath));
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
        HDFSUtil util = localUtils.get();
        try {
            return util.exists(resourcePath);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        HDFSUtil util = localUtils.get();
        try {
            if (util.exists(resourcePath)) {
                return util.getHDFSFileSize(resourcePath);
            } else {
                throw new IOException("path " + resourcePath + " not found");
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
