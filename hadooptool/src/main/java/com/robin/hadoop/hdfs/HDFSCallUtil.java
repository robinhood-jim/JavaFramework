package com.robin.hadoop.hdfs;

import com.robin.core.compress.util.CompressDecoder;
import com.robin.core.compress.util.CompressEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@SuppressWarnings("unused")
public class HDFSCallUtil {

    private static final int BUFFER_SIZE = 100 * 1024;

    public static String uploadFile(Configuration config, String filePath, String toPath) throws HdfsException {
        String[] fileArr = filePath.split(",");
        StringBuilder buffer = new StringBuilder();
        try {
            for (int i = 0; i < fileArr.length; i++) {
                buffer.append(upload(config, fileArr[i], toPath));
                if (i < fileArr.length - 1) {
                    buffer.append(",");
                }
            }
        } catch (HdfsException e) {
            log.error("{}", e.getMessage());
            throw e;
        }
        return buffer.toString();
    }

    public static FileSystem getFileSystem(final Configuration config) throws HdfsException {
        try {
            return FileSystem.get(config);
        } catch (IOException ex) {
            throw new HdfsException(ex);
        }
    }

    public static String upload(@NonNull final Configuration config, @NonNull String filePath, String toUrl) throws HdfsException {
        String url;
        try {
            String hdfsUrl = "";
            if (!ObjectUtils.isEmpty(toUrl)) {
                hdfsUrl = toUrl;
            }
            int pos;
            pos = hdfsUrl.lastIndexOf("/");
            String hdfsUrlPath = hdfsUrl.substring(0, pos);

            FileSystem fs = FileSystem.get(config);
            if (!fs.exists(new Path(hdfsUrlPath))) {
                fs.mkdirs(new Path(hdfsUrlPath));
            }
            fs.copyFromLocalFile(new Path(filePath), new Path(toUrl));
            url = hdfsUrl;
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return url;
    }

    public static boolean uploadByInputStream(final Configuration config, InputStream in, String toUrl, int bufferSize) throws HdfsException {
        Path dfs = new Path(toUrl);
        try (FileSystem fs = FileSystem.get(config);
             FSDataOutputStream fsdo = fs.create(dfs)) {
            int len;
            byte[] buffer = new byte[bufferSize <= 0 ? BUFFER_SIZE : bufferSize];
            while ((len = in.read(buffer)) > 0) {
                fsdo.write(buffer, 0, len);
            }
            return true;
        } catch (IOException e) {
            throw new HdfsException(e);
        }
    }

    public static String uploadByInputStream(final Configuration config, InputStream in, String toUrl, int bufferSize, String fromCharset, String toCharset) throws HdfsException {
        Path dfs = new Path(toUrl);
        try (
                FileSystem fs = FileSystem.get(config);
                FSDataOutputStream fsdo = fs.create(dfs);
                InputStreamReader isr = new InputStreamReader(in, Charset.forName(fromCharset))) {
            char[] buf = new char[bufferSize];
            StringBuilder strb = new StringBuilder();
            int readCount = 0;
            int point = 0;
            while (-1 != (readCount = isr.read(buf, 0, bufferSize))) {
                strb.append(buf, 0, readCount);
                if ((point++) % 10000 == 0) {
                    fsdo.write(strb.toString().getBytes(toCharset));
                    strb.delete(0, strb.length());
                }
            }
            if (strb.length() > 0) {
                fsdo.write(strb.toString().getBytes(toCharset));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new HdfsException(e);
        }
        return toUrl;
    }

    // char[]转byte[]
    private static byte[] getBytes(char[] chars, String charset) {
        Charset cs = Charset.forName(charset);
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    // byte[]转char[]
    private char[] getChars(byte[] bytes, String charset) {
        Charset cs = Charset.forName(charset);
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static boolean deleteHdsfUrl(final Configuration config, String uri, String path) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(new URI(uri), config);
            if (fs.exists(new Path(path))) {
                fs.delete(new Path(path), true);
            }
            return true;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
        }
    }

    public static void moveDirectory(final Configuration config, final String hdfsSource, final String hdfstargetPath) throws HdfsException {
        try {
            String frompath = hdfsSource;
            String topath = hdfstargetPath;
            if (!frompath.endsWith("/")) {
                frompath += "/";
            }
            if (!topath.endsWith("/")) {
                topath += "/";
            }
            if (exists(config, hdfsSource) && isDirectory(config, hdfsSource)) {
                FileSystem fs = FileSystem.get(config);
                if (!isDirectory(config, hdfstargetPath)) {
                    mkdir(config, hdfstargetPath);
                }
                List<String> fileList = listFileName(config, hdfsSource);

                for (String fileName : fileList) {
                    fs.rename(new Path(frompath + fileName), new Path(topath + fileName));
                }
            } else {
                log.error("source file does not exists,mv ignore!");
            }
        } catch (IOException e) {
            throw new HdfsException(e);
        }
    }

    public static void moveFile(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);

                fs.rename(new Path(fromPath), new Path(toPath));
            }
        } catch (IOException ex) {
            throw new HdfsException(ex);
        }
    }

    public static List<String> listFileName(final Configuration config, String hdfsUrl) throws HdfsException {
        List<String> hdfsUrlList = new ArrayList<>();
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);

            FileStatus[] status = fs.listStatus(path);
            Path[] listPaths = FileUtil.stat2Paths(status);
            for (Path listPath : listPaths) {
                if (isDirectory(config, listPath)) {
                    hdfsUrlList.add(listPath.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
        }
        return hdfsUrlList;
    }

    /**
     * 获取HDFS文件长度
     *
     * @param config
     * @param hdfsUrl
     * @return
     * @throws HdfsException
     */
    public static Long getHDFSFileSize(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            FileStatus status = fs.getFileStatus(path);
            return status.getLen();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
        }
    }

    public static List<Map<String, String>> listFileAndDirectory(final Configuration config, String hdfsUrl) throws HdfsException {
        List<Map<String, String>> hdfsUrlList = new ArrayList<>();
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            FileStatus[] status = fs.listStatus(path);
            Path[] listPaths = FileUtil.stat2Paths(status);
            for (Path listPath : listPaths) {
                Map<String, String> map = new HashMap<>();
                map.put("name", listPath.getName());
                map.put("path", listPath.toString());
                map.put("isDir",  isDirectory(config, listPath) ? "1" : "0");
                hdfsUrlList.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
        }
        return hdfsUrlList;
    }

    public static boolean isDirectory(final Configuration config, Path sourcePath) throws HdfsException {
        boolean isd = false;
        try {
            FileSystem fs = FileSystem.get(config);
            if (fs.exists(sourcePath)) {
                isd = fs.getFileStatus(sourcePath).isDirectory();
            }
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return isd;
    }


    public static List<String> listFile(final Configuration config, String hdfsUrl) throws HdfsException {
        List<String> hdfsUrlList = new ArrayList<>();
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            FileStatus[] status = fs.listStatus(path);
            Path[] listPaths = FileUtil.stat2Paths(status);
            for (Path listPath : listPaths) {
                if (!listPath.toString().endsWith("_SUCCESS")) {
                    hdfsUrlList.add(listPath.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
        }
        return hdfsUrlList;
    }

    public static void rmdirs(final Configuration config, String relativeName) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            Path workDir = fs.getWorkingDirectory();

            Path path = new Path(workDir + "/" + relativeName);
            fs.delete(path, true);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("", e);
        }
    }

    public static boolean mkdir(final Configuration config, String relativeName) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            String[] pathSeq = relativeName.split("/", -1);
            List<String> retList = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            builder.append("/").append(pathSeq[1]);

            if (pathSeq.length > 2) {
                retList.add(builder.toString());
                for (int i = 2; i < pathSeq.length; i++) {
                    builder.append("/").append(pathSeq[i]);
                    retList.add(builder.toString());
                }
            }
            if (!retList.isEmpty()) {
                log.debug(" path list {} {}", retList, fs);
                for (String path : retList) {
                    log.debug(" path {} exists {}", path, fs.exists(new Path(path)));
                    if (!fs.exists(new Path(path))) {
                        log.debug("mkdir path {}", path);
                        fs.mkdirs(new Path(path));
                    }
                }
            } else {
                fs.mkdirs(new Path(relativeName));
            }
            return true;
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
    }

    public static boolean isDirectory(final Configuration config, String hdfsUrl) throws HdfsException {
        boolean isd = false;
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            if (fs.exists(path)) {
                isd = fs.getFileStatus(path).isDirectory();
            }
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return isd;
    }

    public static boolean emptyDirectory(final Configuration config, String hdfsUrl) throws HdfsException {
        if (exists(config, hdfsUrl)) {
            List<String> paths = listFile(config, hdfsUrl);
            for (String str : paths) {
                delete(config, str);
            }
        }
        return true;
    }

    public static boolean delete(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            if (exists(config, hdfsUrl)) {
                FileSystem fs = FileSystem.get(config);
                Path path = new Path(hdfsUrl);
                fs.delete(path, true);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("", e);
            throw new HdfsException(e);
        }
    }

    public static boolean setresp(final Configuration config, String hdfsUrl, int resp) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            fs.setReplication(new Path(hdfsUrl), (short) resp);
            return true;
        } catch (IOException e) {
            throw new HdfsException(e);
        }
    }

    public static boolean exists(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            return fs.exists(path);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
    }

    public static String read(final Configuration config, String hdfsUrl, String encode) throws HdfsException {
        String retStr = "";
        try (FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config)) {
            Path path = new Path(hdfsUrl);
            if (fs.exists(path)) {
                try (FSDataInputStream is = fs.open(path)) {
                    // get the file info to create the buffer
                    FileStatus stat = fs.getFileStatus(path);
                    byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))];
                    is.readFully(0, buffer);
                    retStr = new String(buffer, encode);
                }
            }
        } catch (IOException e) {
            throw new HdfsException(e);
        }
        return retStr;
    }

    public static byte[] readByte(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config);
            Path path = new Path(hdfsUrl);
            if (fs.exists(path)) {
                try (FSDataInputStream is = fs.open(path)) {
                    // get the file info to create the buffer
                    FileStatus stat = fs.getFileStatus(path);
                    byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))];
                    is.readFully(0, buffer);
                    return buffer;
                }
            } else {
                throw new HdfsException("file path " + hdfsUrl + " does not exists!");
            }
        } catch (IOException e) {
            log.error("", e);
            throw new HdfsException(e);
        }
    }

    public static FSDataOutputStream createFile(final Configuration config, String hdfsUrl, boolean overwriteOrgion) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config);
            if (overwriteOrgion && exists(config, hdfsUrl)) {
                delete(config, hdfsUrl);
            }
            if (!exists(config, hdfsUrl)) {
                return fs.create(new Path(hdfsUrl));
            }
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return null;
    }

    public static void insertLine(FSDataOutputStream out, String outStr) throws HdfsException {
        try {
            out.writeUTF(outStr);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
    }

    public static BufferedReader readStream(final Configuration config, String hdfsUrl, String encode) throws HdfsException {
        try (FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config);
             DataInputStream dis = new DataInputStream(fs.open(new Path(hdfsUrl)));
             BufferedReader br = new BufferedReader(new InputStreamReader(dis, encode))) {
            return br;
        } catch (IOException e) {
            throw new HdfsException(e);
        }
    }

    public static boolean copyToLocal(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);
                fs.copyToLocalFile(new Path(fromPath), new Path(toPath));
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new HdfsException(ex);
        }
    }

    public static boolean copyFromLocal(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);
                fs.copyFromLocalFile(new Path(fromPath), new Path(toPath));
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new HdfsException(ex);
        }
    }

    public static boolean copy(final Configuration config, String fromPath, String toPath) throws HdfsException {
        if (exists(config, fromPath)) {
            try (FileSystem fs = FileSystem.get(URI.create(fromPath), config);
                 DataInputStream dis = new DataInputStream(fs.open(new Path(fromPath)));
                 FSDataOutputStream out = fs.create(new Path(toPath))) {
                IOUtils.copyBytes(dis, out, config);
                return true;
            } catch (IOException e) {
                throw new HdfsException(e);
            }
        }else{
            return false;
        }
    }

    public static boolean chmod(final Configuration configuration, String filePath, short permission) throws HdfsException {
        if (exists(configuration, filePath)) {
            try (FileSystem fs = FileSystem.get(URI.create(filePath), configuration)) {
                fs.setPermission(new Path(filePath), FsPermission.createImmutable(permission));
                return true;
            } catch (IOException ex) {
                throw new HdfsException(ex);
            }
        }
        return false;
    }

    public static boolean mergeToNewFile(final Configuration config, String sourcePath, String fileSuffix, String newFilePath) throws HdfsException {
        if (exists(config, sourcePath) && isDirectory(config, sourcePath)) {
            List<String> files = listFile(config, sourcePath);
            if (!CollectionUtils.isEmpty(files)) {
                List<String> mergeList = files.stream().filter(f -> f.endsWith("." + fileSuffix)).collect(Collectors.toList());
                try (OutputStream outputStream = getHDFSOutputStream(config, newFilePath)) {
                    for (String file : mergeList) {
                        InputStream inputStream = getHDFSDataByInputStream(config, file);
                        if(!ObjectUtils.isEmpty(inputStream)) {
                            IOUtils.copyBytes(inputStream, outputStream, 4096);
                            inputStream.close();
                        }
                    }
                    return true;
                } catch (IOException ex) {
                    throw new HdfsException(ex);
                }

            }

        }
        return false;
    }


    public static BufferedReader getHDFSDataByReader(final Configuration config, String path, String encode) throws HdfsException {
        BufferedReader reader = null;
        try {
            if (exists(config, path)) {
                if (ObjectUtils.isEmpty(encode)) {
                    reader = new BufferedReader(new InputStreamReader(CompressDecoder.getInputStreamByCompressType(path, FileSystem.get(config).open(new Path(path)))));
                } else {
                    reader = new BufferedReader(new InputStreamReader(CompressDecoder.getInputStreamByCompressType(path, FileSystem.get(config).open(new Path(path))), encode));
                }
            }
            return reader;
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static BufferedInputStream getHDFSDataByInputStream(final Configuration config, String path) throws HdfsException {
        try {
            if (exists(config, path)) {
                return new BufferedInputStream(CompressDecoder.getInputStreamByCompressType(path, FileSystem.get(config).open(new Path(path))));
            }
            return null;
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }
    public static OutputStream getHDFSDataByOutputStream(final Configuration config,String path) throws HdfsException{
        try{
            return CompressEncoder.getOutputStreamByCompressType(path,FileSystem.get(config).create(new Path(path)));
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static InputStream getHDFSRawInputStream(final Configuration config, String path) throws HdfsException {
        try{
            return FileSystem.get(config).open(new Path(path));
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static OutputStream getHDFSRawOutputStream(final Configuration config, String path) throws HdfsException {
        try{
            return FileSystem.get(config).create(new Path(path));
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static BufferedWriter getHDFSDataByWriter(final Configuration config, String path, String encode) throws HdfsException {
        BufferedWriter writer;
        try {
            if (exists(config, path)) {
                delete(config, path);
            }
            if (ObjectUtils.isEmpty(encode)) {
                writer = new BufferedWriter(new OutputStreamWriter(CompressEncoder.getOutputStreamByCompressType(path, FileSystem.get(config).create(new Path(path)))));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(CompressEncoder.getOutputStreamByCompressType(path, FileSystem.get(config).create(new Path(path))), encode));
            }
            return writer;
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static OutputStream getHDFSOutputStream(final Configuration config, String path) throws HdfsException {
        try{
            return CompressEncoder.getOutputStreamByCompressType(path, FileSystem.get(config).create(new Path(path)));
        }catch (IOException ex){
            throw new HdfsException(ex);
        }
    }

    public static String getFileSuffix(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        return name.substring(pos + 1);
    }

    public static void createAndInsert(final Configuration config, String hdfsUrl, String txt, boolean overwriteOrgion) throws HdfsException {
        Assert.isTrue(!StringUtils.isEmpty(hdfsUrl), "");
        Assert.notNull(config, "");
        try (FSDataOutputStream stream = createFile(config, hdfsUrl, overwriteOrgion)) {
            Assert.notNull(stream, "");
            stream.writeUTF(txt);
        } catch (IOException e) {
            throw new HdfsException(e);
        }
    }

}
