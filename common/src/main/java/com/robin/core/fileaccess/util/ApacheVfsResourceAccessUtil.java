package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ApacheVfsResourceAccessUtil extends AbstractResourceAccessUtil {
    public ApacheVfsResourceAccessUtil() {
        try {
            manager = new StandardFileSystemManager();
            logger.info(" manager {} ",manager);
            manager.init();
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    private StandardFileSystemManager manager = null;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public BufferedReader getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());

            FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));

            return new BufferedReader(new InputStreamReader(getInResource(fo, meta), meta.getEncode()));
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        BufferedWriter writer;
        try {
            FileObject fo = checkFileExist(meta, resourcePath);
            writer = getWriterByPath(resourcePath, fo.getContent().getOutputStream(), meta.getEncode());
            return writer;
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream out;
        try {
            FileObject fo = checkFileExist(meta, resourcePath);
            out = getOutputStreamByPath(resourcePath, fo.getContent().getOutputStream());
            return out;
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());

            FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            InputStream reader = getInResource(fo, meta);
            return reader;
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }
    private InputStream getRawInResource(FileObject fo,DataCollectionMeta meta) throws Exception{
        InputStream reader=null;
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

    private InputStream getInResource(FileObject fo,DataCollectionMeta meta) throws Exception{
        InputStream reader=null;
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
        List<String> list = new ArrayList<String>();
        try {
            FileObject fo = manager.resolveFile(getUriByParam(param, path).toString(), getOptions(param));
            if (FileType.FOLDER.equals(fo.getType())) {
                FileObject[] object = fo.getChildren();
                for (int i = 0; i < object.length; i++) {
                    if (!FileType.FOLDER.equals(object[i].getType())) {
                        list.add(object[i].getName().getBaseName());
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public FileObject checkFileExist(DataCollectionMeta meta, String resourcePath) throws Exception {
        VfsParam param = new VfsParam();
        ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
        FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
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
            fo.createFile();
        }
        return fo;
    }
    public boolean checkFileExist(VfsParam param,String resourcePath) throws Exception{
        FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
        if (fo.exists()) {
            return true;
        } else {
           return false;
        }
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream out;
        try {
            FileObject fo = checkFileExist(meta, resourcePath);
            out = fo.getContent().getOutputStream();
            return out;
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        VfsParam param = new VfsParam();
        try {
            ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());

            FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            InputStream reader = getRawInResource(fo, meta);
            return reader;
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    private URI getUriByParam(VfsParam param, String relativePath) throws Exception {
        String userInfo = param.getUserName() + ":" + param.getPassword();//URLEncoder.encode(param.getPassword(), "iso8859-1");// 解决密码中的特殊字符问题，如@。
        String remoteFilePath = relativePath;
        if (!remoteFilePath.startsWith("/")) {
            remoteFilePath = "/" + remoteFilePath;
        }
        URI sftpUri = new URI(param.getProtocol(), userInfo, param.getHostName(), param.getPort(), remoteFilePath, null, null);
        if (logger.isDebugEnabled()) {
            logger.debug("uri ---> {}" , sftpUri);
        }
        return sftpUri;
    }

    private FileSystemOptions getOptions(VfsParam param) throws Exception {
        FileSystemOptions opts = new FileSystemOptions();
        if (Const.VFS_PROTOCOL.SFTP.getValue().equalsIgnoreCase(param.getProtocol())) {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            if (param.isLockDir()) {
                SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            } else {
                SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
            }
        }else if(Const.VFS_PROTOCOL.FTP.getValue().equalsIgnoreCase(param.getProtocol())){
            FtpFileSystemConfigBuilder builder=FtpFileSystemConfigBuilder.getInstance();
            if(param.isLockDir()){
                builder.setUserDirIsRoot(opts,true);
            }
            if(param.isPassive()){
                logger.debug("---   using passive mode ------");
                builder.setPassiveMode(opts,true);
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
            FileObject fo = manager.resolveFile(getUriByParam(param, resourcePath).toString(), getOptions(param));
            return fo.exists();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    public static class VfsParam {
        private String protocol;
        private String hostName;
        private int port;
        private String userName;
        private String password;

        private boolean lockDir = false;
        private boolean passive=false;

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isLockDir() {
            return lockDir;
        }

        public boolean isPassive() {
            return passive;
        }

        public void setPassive(boolean passive) {
            this.passive = passive;
        }

        public void setLockDir(boolean lockDir) {
            this.lockDir = lockDir;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
        public VfsParam(){

        }
        public VfsParam(String hostName,String protocol,String userName,String password){
            this.hostName=hostName;
            this.protocol=protocol;
            adjustProtocol();
            this.userName=userName;
            this.password=password;
        }
        public VfsParam(String hostName,String protocol,int port,String userName,String password){
            this.hostName=hostName;
            this.protocol=protocol;
            this.port=port;
            this.userName=userName;
            this.password=password;
        }
        private void adjustProtocol(){
            if(Const.VFS_PROTOCOL.FTP.getValue().equalsIgnoreCase(this.protocol)){
                port=21;
            }else if(Const.VFS_PROTOCOL.SFTP.getValue().equalsIgnoreCase(this.protocol)){
                port=22;
            }
        }
    }

}

