package com.robin.core.fileaccess.fs;

import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.VfsParam;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.FileNotFoundException;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApacheVfsFileSystemAccessor extends AbstractFileSystemAccessor {
    private Map<String,FileObject> objectMap=new HashMap<>();

    public ApacheVfsFileSystemAccessor() {
        this.identifier= Const.FILESYSTEM.VFS.getValue();
        try {
            manager = new StandardFileSystemManager();
            logger.info(" manager {} ", manager);
            manager.init();
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    private StandardFileSystemManager manager = null;
    private static final Logger logger = LoggerFactory.getLogger(ApacheVfsFileSystemAccessor.class);

    @Override
    public Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        InputStream stream;
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            FileObject fileObject = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            stream=getInResource(fileObject, meta);
            return Pair.of(new BufferedReader(new InputStreamReader(stream, meta.getEncode())),stream);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        BufferedWriter writer;
        OutputStream outputStream;
        try {
            FileObject fileObject = createNotExists(meta, resourcePath);
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            outputStream=fileObject.getContent().getOutputStream();
            writer = getWriterByPath(resourcePath, outputStream, meta.getEncode());
            return Pair.of(writer,outputStream);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream out;
        try {
            FileObject fileObject = createNotExists(meta, resourcePath);
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            out = getOutputStreamByPath(resourcePath, fileObject.getContent().getOutputStream());
            return out;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            FileObject fileObject = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            return getInResource(fileObject, meta);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static FileObject getFileObject(FileSystemManager manager, DataCollectionMeta meta, String resPath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            return manager.resolveFile(getUriByParam(param, resPath).toString(), getOptions(param));
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private InputStream getRawInResource(FileObject fo, DataCollectionMeta meta) throws Exception {
        InputStream reader;
        if (fo.exists()) {
            if (FileType.FOLDER.equals(fo.getType())) {
                logger.error("File {} is a directory！", meta.getPath());
                throw new FileNotFoundException("File " + meta.getPath() + " is a directory!");
            } else {
                reader = fo.getContent().getInputStream();
            }
        } else {
            throw new FileNotFoundException("File " + meta.getPath() + " not found!");
        }
        return reader;
    }

    public static InputStream getInResource(FileObject fo, DataCollectionMeta meta) throws Exception {
        InputStream reader;
        if (fo.exists()) {
            if (FileType.FOLDER.equals(fo.getType())) {
                logger.error("File {} is a directory！", meta.getPath());
                throw new FileNotFoundException("File " + meta.getPath() + " is a directory!");
            } else {
                reader = getInputStreamByPath(meta.getPath(), fo.getContent().getInputStream());
            }
        } else {
            throw new FileNotFoundException("File " + meta.getPath() + " not found!");
        }
        return reader;
    }

    public List<String> listFilePath(VfsParam param, String path) {
        List<String> list = new ArrayList<>();
        try(FileObject fo = manager.resolveFile(getUriByParam(param, path).toString(), getOptions(param))) {
            if (FileType.FOLDER.equals(fo.getType())) {
                FileObject[] object = fo.getChildren();
                if (!ObjectUtils.isEmpty(object)) {
                    for (FileObject fileObject : object) {
                        if (!FileType.FOLDER.equals(fileObject.getType())) {
                            list.add(fileObject.getName().getBaseName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public FileObject createNotExists(DataCollectionMeta meta, String resourcePath) throws Exception {
        VfsParam param = new VfsParam();
        ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
       try(FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param))) {
           if (fo.exists()) {
               if (FileType.FOLDER.equals(fo.getType())) {
                   logger.error("File {} is a directory！", resourcePath);
                   throw new FileNotFoundException("File " + resourcePath + " is a directory!");
               } else {
                   logger.warn("File " + resourcePath + " already exists!,Overwrite");
               }
           } else {
               if (!fo.getParent().exists()) {
                   fo.getParent().createFolder();
               }
               //fo.createFile();
           }
           return fo;
       }catch (Exception ex){

       }
       return null;
    }

    public boolean checkFileExist(VfsParam param, String resourcePath) throws Exception {
        try(FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param))) {
            return fo.exists();
        }catch (FileSystemException ex){
            throw ex;
        }
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream out;
        try {
            FileObject fileObject = createNotExists(meta, resourcePath);
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            out = fileObject.getContent().getOutputStream();
            return out;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            FileObject fileObject = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            meta.getResourceCfgMap().put("$processId",setProcessId(fileObject));
            return getRawInResource(fileObject, meta);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private static URI getUriByParam(VfsParam param, String relativePath) throws Exception {
        String userInfo = param.getUserName() + ":" + param.getPassword();//URLEncoder.encode(param.getPassword(), "iso8859-1");// 解决密码中的特殊字符问题，如@。
        String remoteFilePath = relativePath;
        if (!remoteFilePath.startsWith("/")) {
            remoteFilePath = "/" + remoteFilePath;
        }
        URI sftpUri = new URI(param.getProtocol(), userInfo, param.getHostName(), param.getPort(), remoteFilePath, null, null);
        if (logger.isDebugEnabled()) {
            logger.debug("uri ---> {}", sftpUri);
        }
        return sftpUri;
    }

    private static FileSystemOptions getOptions(VfsParam param) throws Exception {
        FileSystemOptions opts = new FileSystemOptions();
        if (Const.VFS_PROTOCOL.SFTP.getValue().equalsIgnoreCase(param.getProtocol())) {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, param.isLockDir());
        } else if (Const.VFS_PROTOCOL.FTP.getValue().equalsIgnoreCase(param.getProtocol())) {
            FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
            if (param.isLockDir()) {
                builder.setUserDirIsRoot(opts, true);
            }
            if (param.isPassive()) {
                logger.debug("---   using passive mode ------");
                builder.setPassiveMode(opts, true);
            }
        } else if (Const.VFS_PROTOCOL.HTTP.getValue().equalsIgnoreCase(param.getProtocol())) {
            HttpFileSystemConfigBuilder builder = HttpFileSystemConfigBuilder.getInstance();
            if(!ObjectUtils.isEmpty(param.getProxyHost())){
                builder.setProxyHost(opts,param.getProxyHost());
            }
            if(!ObjectUtils.isEmpty(param.getProxyPort())){
                builder.setProxyPort(opts, param.getProxyPort());
            }
        }else if(Const.VFS_PROTOCOL.HTTPS.getValue().equalsIgnoreCase(param.getProtocol())){
            HttpFileSystemConfigBuilder builder = HttpFileSystemConfigBuilder.getInstance();
            if(!ObjectUtils.isEmpty(param.getProxyHost())){
                builder.setProxyHost(opts,param.getProxyHost());
            }
            if(!ObjectUtils.isEmpty(param.getProxyPort())){
                builder.setProxyPort(opts, param.getProxyPort());
            }

        }

        return opts;
    }

    public VfsParam returnFtpParam(String hostName, int port, String userName, String password, String protocol) {
        VfsParam param = new VfsParam();
        param.setHostName(hostName);
        param.setProtocol(protocol);
        if (Const.VFS_PROTOCOL.FTP.getValue().equalsIgnoreCase(protocol)) {
            if (port != 0) {
                param.setPort(port);
            } else {
                param.setPort(21);
            }
        } else if (Const.VFS_PROTOCOL.SFTP.getValue().equalsIgnoreCase(protocol)) {
            if (port != 0) {
                param.setPort(port);
            } else {
                param.setPort(22);
            }
        }
        param.setUserName(userName);
        param.setPassword(password);
        return param;
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            try(FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param))){
                return fo.exists();
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
            FileObject fileObject = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            return fileObject.getContent().getSize();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private synchronized String setProcessId(FileObject fileObject){
        String processId=Thread.currentThread().getId()+"_"+System.currentTimeMillis();
        objectMap.put(processId,fileObject);
        return processId;
    }


    public void closeWithProcessId(String processId) throws IOException {
        if(objectMap.containsKey(processId)){
            objectMap.get(processId).close();
            objectMap.remove(processId);
        }
    }
}

