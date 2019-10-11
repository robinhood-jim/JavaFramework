package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
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
            manager.init();
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    private StandardFileSystemManager manager = null;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public BufferedReader getInResourceByReader(DataCollectionMeta meta) throws Exception {
        VfsParam param = new VfsParam();
        ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
        BufferedReader reader;

        FileObject fo = manager.resolveFile(getUriByParam(param, meta.getPath()).toString(), getOptions(param));
        if (fo.exists()) {
            if (FileType.FOLDER.equals(fo.getType())) {
                logger.error("File {} is a directory！", meta.getPath());
                throw new FileNotFoundException("File " + meta.getPath() + " is a directory!");
            } else {
                reader = getReaderByPath(meta.getPath(), fo.getContent().getInputStream(), meta.getEncode());
            }
        } else {
            throw new FileNotFoundException("File " + meta.getPath() + " not found!");
        }
        return reader;
    }

    @Override
    public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception {
        BufferedWriter writer;
        FileObject fo = checkFileExist(meta);
        writer = getWriterByPath(meta.getPath(), fo.getContent().getOutputStream(), meta.getEncode());
        return writer;
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception {
        OutputStream out;
        FileObject fo = checkFileExist(meta);
        out = getOutputStreamByPath(meta.getPath(), fo.getContent().getOutputStream());
        return out;
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception {
        VfsParam param = new VfsParam();
        ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
        InputStream reader;
        FileObject fo = manager.resolveFile(getUriByParam(param, meta.getPath()).toString(), getOptions(param));
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

    private FileObject checkFileExist(DataCollectionMeta meta) throws Exception {
        VfsParam param = new VfsParam();
        ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
        FileObject fo = manager.resolveFile(getUriByParam(param, meta.getPath()).toString(), getOptions(param));
        if (fo.exists()) {
            if (FileType.FOLDER.equals(fo.getType())) {
                logger.error("File {} is a directory！", meta.getPath());
                throw new FileNotFoundException("File " + meta.getPath() + " is a directory!");
            } else {
                logger.warn("File " + meta.getPath() + " aready exists!,Overwirte");
            }
        } else {
            if (!fo.getParent().exists()) {
                fo.getParent().createFolder();
            }
            fo.createFile();
        }
        return fo;
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta) throws Exception {
        OutputStream out;
        FileObject fo = checkFileExist(meta);
        out = fo.getContent().getOutputStream();
        return out;
    }

    private URI getUriByParam(VfsParam param, String relativePath) throws Exception {
        String userInfo = param.getUserName() + ":" + URLEncoder.encode(param.getPassword(), "iso8859-1");// 解决密码中的特殊字符问题，如@。
        String remoteFilePath = relativePath;
        if (!remoteFilePath.startsWith("/")) {
            remoteFilePath = "/" + remoteFilePath;
        }
        URI sftpUri = new URI(param.getProtocol(), userInfo, param.getHostName(), param.getPort(), remoteFilePath, null, null);
        if (logger.isDebugEnabled()) {
            logger.debug("uri ---> " + sftpUri.toString());
        }
        return sftpUri;
    }

    private FileSystemOptions getOptions(VfsParam param) throws Exception {
        FileSystemOptions opts = new FileSystemOptions();
        if (param.getProtocol().equalsIgnoreCase(Const.PREFIX_SFTP)) {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            if (param.isLockDir()) {
                SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            } else {
                SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
            }
        }

        return opts;
    }

    public VfsParam returnFtpParam(String hostName, int port, String userName, String password, String protocol) {
        VfsParam param = new VfsParam();
        param.setHostName(hostName);
        param.setProtocol(protocol);
        if ("ftp".equalsIgnoreCase(protocol)) {
            if (port != 0) {
                param.setPort(port);
            } else {
                param.setPort(21);
            }
        } else if ("sftp".equalsIgnoreCase(protocol)) {
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

    public static class VfsParam {
        private String protocol;
        private String hostName;
        private int port;
        private String userName;
        private String password;

        private boolean lockDir = false;

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

        public void setLockDir(boolean lockDir) {
            this.lockDir = lockDir;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

    }

}

