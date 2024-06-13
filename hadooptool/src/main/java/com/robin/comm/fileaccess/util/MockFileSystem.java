package com.robin.comm.fileaccess.util;

import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

//mock filesystem support parquet and orc file read from outside of hadoop filesystem
public class MockFileSystem extends FileSystem {
    byte[] streamBytes;
    final List<MockInputStream> streams = new ArrayList<>();
    OutputStream outputStream;
    AbstractFileSystemAccessor accessUtil;
    DataCollectionMeta colmeta;

    @Override
    public URI getUri() {
        try {
            return new URI("mock:///");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("bad uri", e);
        }
    }
    public MockFileSystem(Configuration conf, byte[] streamBytes) {
        setConf(conf);
        this.streamBytes = streamBytes;
    }
    public MockFileSystem(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessUtil) {
       this.accessUtil=accessUtil;
       this.colmeta=colmeta;
    }

    @Override
    public FSDataInputStream open(Path f) throws IOException {
        MockInputStream result = new MockInputStream(this, streamBytes);
        streams.add(result);
        return result;
    }

    @Override
    public FSDataInputStream open(Path path, int i) throws IOException {
        MockInputStream result = new MockInputStream(this, streamBytes);
        streams.add(result);
        return result;
    }

    @Override
    public FSDataOutputStream create(Path path, FsPermission fsPermission, boolean b, int i, short i1, long l, Progressable progressable) throws IOException {
        if(outputStream!=null){
            throw new FileAlreadyExistsException("file already write,wait for closing");
        }
        outputStream=accessUtil.getRawOutputStream(colmeta, ResourceUtil.getProcessPath(path.toString()));
        return new FSDataOutputStream(outputStream,new Statistics(""));
    }

    @Override
    public FSDataOutputStream append(Path path, int i, Progressable progressable) throws IOException {
        return null;
    }

    @Override
    public boolean rename(Path path, Path path1) throws IOException {
        return false;
    }

    @Override
    public boolean delete(Path path, boolean b) throws IOException {
        return false;
    }

    @Override
    public FileStatus[] listStatus(Path path) throws FileNotFoundException, IOException {
        return new FileStatus[0];
    }


    @Override
    public FileStatus getFileStatus(Path path) throws IOException {
        return new FileStatus(streamBytes.length, false, 1, 4096, 0, path);
    }
    void removeStream(MockInputStream stream) {
        streams.remove(stream);
    }
    @Override
    public void setWorkingDirectory(Path path) {}
    @Override
    public Path getWorkingDirectory() {
        return new Path("/");
    }
    @Override
    public boolean mkdirs(Path path, FsPermission fsPermission) {
        return false;
    }

}
