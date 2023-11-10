package com.robin.hadoop.hdfs;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


@Slf4j
public class HDFSCallUtil {

    private static final int BUFFER_SIZE = 100 * 1024;

    public static String uploadFile(Configuration config, String filePath,String toPath) throws HdfsException {
        String[] fileArr = filePath.split(",");
        StringBuilder buffer = new StringBuilder();
        try {
            for (int i = 0; i < fileArr.length; i++) {
                buffer.append(upload(config, fileArr[i], toPath));
                if (i < fileArr.length - 1) {
                    buffer.append(",");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("", e);
            throw new HdfsException(e);
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

    public static String upload(@NonNull final Configuration config,@NonNull String filePath,String toUrl) throws HdfsException {
        String url ;
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
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return url;
    }

    public static boolean uploadByInputStream(final Configuration config, InputStream in, String toUrl, int bufferSize) throws HdfsException {
        Path dfs = new Path(toUrl);
        try (FileSystem fs = FileSystem.get(config);
             FSDataOutputStream fsdo = fs.create(dfs)) {
            int len ;
            byte[] buffer = new byte[bufferSize <= 0 ? BUFFER_SIZE : bufferSize];
            while ((len = in.read(buffer)) > 0) {
                fsdo.write(buffer, 0, len);
            }
            return true;
        } catch (Exception e) {
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
            int readCount = 0, point = 0;
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

        } catch (Exception e) {
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

    public synchronized static void deleteHdsfUrl(final Configuration config, String uri, String path) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(new URI(uri), config);
            if (fs.exists(new Path(path))) {
                fs.delete(new Path(path), true);
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new HdfsException(e);
        }
    }

    public static void moveFile(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);

                fs.rename(new Path(fromPath), new Path(toPath));
            }
        } catch (Exception ex) {
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
                if (!isDirectory(config, listPath)) {
                    hdfsUrlList.add(listPath.getName());
                }
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
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
            boolean isDir ;
            for (Path listPath : listPaths) {
                Map<String, String> map = new HashMap<>();
                if (!isDirectory(config, listPath)) {
                    isDir = true;
                } else {
                    isDir = false;
                }
                map.put("name", listPath.getName());
                map.put("path", listPath.toString());
                map.put("isDir", isDir ? "1" : "0");
                hdfsUrlList.add(map);
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return isd;
    }

    @SuppressWarnings("unused")
    private String getExtension(String filename, String defExt) {
        if ((filename != null) && (filename.length() > 0)) {
            int i = filename.lastIndexOf('.');

            if ((i > -1) && (i < (filename.length() - 1))) {
                return filename.substring(i + 1);
            }
        }
        return defExt;
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
        } catch (Exception e) {
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

        } catch (Exception e) {
            e.printStackTrace();
            log.error("", e);
        }
    }

    public static void mkdir(final Configuration config, String relativeName) throws HdfsException {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return isd;
    }

    public static void emptyDirectory(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            if (exists(config, hdfsUrl)) {
                List<String> paths = listFile(config, hdfsUrl);
                for (String str : paths) {
                    delete(config, str);
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
            throw new HdfsException(ex);
        }
    }

    public static void delete(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            if (exists(config, hdfsUrl)) {
                FileSystem fs = FileSystem.get(config);
                Path path = new Path(hdfsUrl);
                fs.delete(path, true);
            }
        } catch (Exception e) {
            log.error("", e);
            throw new HdfsException(e);
        }
    }

    public static void setresp(final Configuration config, String hdfsUrl, int resp) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            fs.setReplication(new Path(hdfsUrl), (short) resp);
        } catch (Exception e) {
            throw new HdfsException(e);
        }
    }

    public static boolean exists(final Configuration config, String hdfsUrl) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(config);
            Path path = new Path(hdfsUrl);
            return fs.exists(path);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
    }

    public static String read(final Configuration config, String hdfsUrl, String encode) throws HdfsException {
        String retStr = "";
        try(FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config)) {
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
        } catch (Exception e) {
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
                throw new HdfsException("file path "+hdfsUrl+" does not exists!");
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
        return null;
    }

    public static void insertLine(final Configuration config, FSDataOutputStream out, String outStr) throws HdfsException {
        try {
            out.writeUTF(outStr);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new HdfsException(e);
        }
    }

    public static BufferedReader readStream(final Configuration config, String hdfsUrl, String encode) throws HdfsException {
        try (FileSystem fs = FileSystem.get(URI.create(hdfsUrl), config);
             DataInputStream dis = new DataInputStream(fs.open(new Path(hdfsUrl)));
             BufferedReader br = new BufferedReader(new InputStreamReader(dis, encode))) {
            return br;
        } catch (Exception e) {
            throw new HdfsException(e);
        }
    }

    public static void copyToLocal(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);
                fs.copyToLocalFile(new Path(fromPath), new Path(toPath));
            }
        } catch (Exception ex) {
            throw new HdfsException(ex);
        }
    }

    public static void copyFromLocal(final Configuration config, String fromPath, String toPath) throws HdfsException {
        try {
            if (exists(config, fromPath)) {
                FileSystem fs = FileSystem.get(config);
                fs.copyFromLocalFile(new Path(fromPath), new Path(toPath));
            }
        } catch (Exception ex) {
            throw new HdfsException(ex);
        }
    }

    public static void copy(final Configuration config, String fromPath, String toPath) throws HdfsException {
        if (exists(config, fromPath)) {
            try(FileSystem fs = FileSystem.get(URI.create(fromPath), config);
                DataInputStream dis=new DataInputStream(fs.open(new Path(fromPath)));
                FSDataOutputStream out=fs.create(new Path(toPath))) {
                IOUtils.copyBytes(dis, out, config);
            } catch (Exception e) {
                throw new HdfsException(e);
            }
        }
    }


    public static BufferedReader getHDFSDataByReader(final Configuration config, String path, String encode) throws Exception {
        BufferedReader reader = null;
        File file = new File(path);
        if (exists(config, path)) {
            String suffix = getFileSuffix(file);
            if ("gz".equalsIgnoreCase(suffix)) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(FileSystem.get(config).open(new Path(path))), encode));
            } else if ("zip".equalsIgnoreCase(suffix)) {
                reader = new BufferedReader(new InputStreamReader(new ZipInputStream(FileSystem.get(config).open(new Path(path))), encode));
            } else {
                reader = new BufferedReader(new InputStreamReader(FileSystem.get(config).open(new Path(path)), encode));
            }
        }
        return reader;
    }

    public static BufferedInputStream getHDFSDataByInputStream(final Configuration config, String path) throws Exception {
        BufferedInputStream reader = null;
        File file = new File(path);
        if (exists(config, path)) {
            String suffix = getFileSuffix(file);
            if ("gz".equalsIgnoreCase(suffix)) {
                reader = new BufferedInputStream(new GZIPInputStream(FileSystem.get(config).open(new Path(path))));
            } else if ("zip".equalsIgnoreCase(suffix)) {
                reader = new BufferedInputStream(new ZipInputStream(FileSystem.get(config).open(new Path(path))));
            } else {
                reader = new BufferedInputStream(FileSystem.get(config).open(new Path(path)));
            }
        }
        return reader;
    }
    public static BufferedInputStream getHDFSRawInputStream(final Configuration config, String path) throws IOException{
        return new BufferedInputStream(FileSystem.get(config).open(new Path(path)));
    }

    public static BufferedWriter getHDFSDataByWriter(final Configuration config, String path, String encode) throws Exception {
        BufferedWriter writer ;
        if (exists(config, path)) {
            delete(config, path);
        }
        String suffix = getFileSuffix(new File(path));
        if ("gz".equalsIgnoreCase(suffix)) {
            writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(FileSystem.get(config).create(new Path(path))), encode));
        } else if ("zip".equalsIgnoreCase(suffix)) {
            writer = new BufferedWriter(new OutputStreamWriter(new ZipOutputStream(FileSystem.get(config).create(new Path(path))), encode));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(FileSystem.get(config).create(new Path(path)), encode));
        }
        return writer;
    }

    public static String getFileSuffix(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        return name.substring(pos + 1);
    }

    public static void createAndinsert(final Configuration config, String hdfsUrl, String txt, boolean overwriteOrgion) throws HdfsException {
        Assert.isTrue(!StringUtils.isEmpty(hdfsUrl),"");
        Assert.notNull(config,"");
        try(FSDataOutputStream stream=createFile(config, hdfsUrl, overwriteOrgion)) {
            Assert.notNull(stream,"");
            stream.writeUTF(txt);
        } catch (Exception e) {
            throw new HdfsException(e);
        }
    }

}
