package com.robin.comm.ftp;


import com.google.common.collect.Lists;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class SimpleFtp {
    protected static final Logger log = LoggerFactory.getLogger(SimpleFtp.class);
    public static final int PORT_DEFAULT = 21;
    public static final String ENCODING_DEFAULT = "UTF-8";
    protected FTPClient ftpClient;
    protected String hostname;
    protected String userName;
    protected String password;
    protected int port;
    protected long waitForReconnect = 60000L;

    public SimpleFtp() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ENCODING_DEFAULT);
    }

    public SimpleFtp(String hostName, String userName, String password) {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ENCODING_DEFAULT);
        this.hostname = hostName;
        this.userName = userName;
        this.password = password;
        this.port = PORT_DEFAULT;
    }

    public void setControlEncoding(String encode) {
        ftpClient.setControlEncoding(encode);
    }

    public boolean connect() {
        return connect(hostname, port, userName, password);
    }

    public boolean connect(String host, String username, String password) {
        return connect(host, PORT_DEFAULT, username, password);
    }

    public boolean connect(String host, int port, String username, String password) {
        try {
            log.info("login to {} {} using {} {}", host, port, username, password);
            ftpClient.connect(host, port);
            ftpClient.setDataTimeout(20000);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();//Enter PassiveMode
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (Exception e) {
            log.error("", e);
        }


        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            return true;
        }

        return false;
    }

    public boolean exists(String remote) throws IOException {
        if (isDirectory(remote)) {//dir
            return true;
        } else {
            FTPFile[] files = ftpClient.listFiles(remote);
            return files.length > 0;
        }
    }

    public long filesize(String remote) throws IOException {
        if (isDirectory(remote)) {//dir
            return -1;
        } else {
            FTPFile[] files = ftpClient.listFiles(remote);
            return files[0].getSize();
        }
    }

    public boolean isDirectory(String remote) throws IOException {
        return ftpClient.changeWorkingDirectory(remote);
    }

    public boolean isFile(String remote) throws IOException {
        return exists(remote) && (!isDirectory(remote));
    }

    public List<String> listFile(String... args) throws IOException {
        int length = args.length;
        List<String> retStr = new ArrayList<>();
        String path = args[0];
        List<String> ignorefiles = null;
        if (length >= 2) {
            ignorefiles = Lists.newArrayList(Arrays.copyOfRange(args,1, args.length-1));
        }
        FTPFile[] files = ftpClient.listFiles(path);
        for (FTPFile file : files) {
            if (ignorefiles == null || !ignorefiles.contains(file.getName().toLowerCase())) {
                retStr.add(path + "/" + file.getName());
            }
        }
        return retStr;
    }

    public List<FileInfo> listFileDetail(String path, String interfaceName) {
        List<FileInfo> retStr = new ArrayList<>();
        try {

            FTPFile[] files = ftpClient.listFiles(path);
            for (FTPFile file : files) {
                retStr.add(new FileInfo(path, file.getName(), file.getSize(), file.getTimestamp(), interfaceName));
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return retStr;
    }

    /**
     * get increment ftp files by last grep maximum timestamp
     *
     * @param path
     * @param afterTime file timestamp after this value,value changed after call,means the max timestamp ftp files
     * @return
     * @throws IOException
     */

    public List<String> listIncrementFile(String path, Date afterTime) throws IOException {
        List<String> retStr = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(afterTime);
        FTPFile[] files = ftpClient.listFiles(path);
        long lastTs = 0L;
        for (int i = 0; i < files.length; i++) {
            Calendar filescal = files[i].getTimestamp();
            lastTs = filescal.getTime().getTime();
            if (filescal.after(cal1.getTime())) {
                retStr.add(path + "/" + files[i].getName());
            }
        }
        afterTime.setTime(lastTs);
        return retStr;
    }


    public List<String> listFileWithPattern(String pathname, String patterntxt) throws IOException {
        List<String> retStr = new ArrayList<>();
        ftpClient.changeWorkingDirectory(pathname);
        String[] names = ftpClient.listNames(patterntxt);
        String relatepath = pathname.endsWith("/") ? pathname : pathname + "/";
        for (String name : names) {
            String tmppath = relatepath + name;
            retStr.add(tmppath);
        }
        return retStr;
    }


    public boolean downloadFile(String remote, String local, int retrys) throws IOException {
        ftpClient.enterLocalPassiveMode();//Enter PassiveMode
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        int retrynum = 0;
        boolean retflag = false;
        IOException iex=null;
        if (!exists(remote)) {
            log.info("remote path not exists {}", remote);
            return false;
        }
        if (isDirectory(remote)) {
            log.info("remote file is directory");
            return false;
        }
        while (!retflag && retrynum < retrys) {
            try {
                retflag = doDownload(remote, local);
                if (!retflag) {
                    Thread.sleep(waitForReconnect);
                }
            } catch (IOException ex) {
                iex=ex;
                retrynum++;
            }catch (InterruptedException e1){
                retrynum++;
            }
        }
        if(!retflag){
            if(iex!=null) {
                throw iex;
            }else{
                throw new IOException("sleep Interrupt");
            }
        }
        return true;
    }

    private boolean doDownload(String remote, String local) throws IOException {
        boolean retflag = false;
        File localfile = new File(local);
        if (localfile.exists()) {
            log.info("local file Exist");
            long localSize = localfile.length();
            try (InputStream in = ftpClient.retrieveFileStream(remote);
                 FileOutputStream os = new FileOutputStream(localfile, true)) {
                ftpClient.setRestartOffset(localSize);
                byte[] bytes = new byte[8192];
                int c;
                while ((c = in.read(bytes)) != -1) {
                    os.write(bytes, 0, c);
                }
                retflag = true;
            } catch (IOException ex) {
                throw ex;
            }

        } else {
            String path = localfile.getParent();
            File parentpath = new File(path);
            if (!parentpath.exists()) {
                System.out.println("create parent folder");
                parentpath.mkdir();
            }
            try (FileOutputStream os = new FileOutputStream(localfile);
                 InputStream in = ftpClient.retrieveFileStream(remote)) {
                byte[] bytes = new byte[8192];
                int c;
                while ((c = in.read(bytes)) != -1) {
                    os.write(bytes, 0, c);
                }
                retflag = true;
            }
        }
        return retflag;
    }

    public boolean downloadFile(List<String> remotelist, String local, int retrys) throws IOException {
        boolean ret = false;
        if (!local.endsWith("/")) {
            local += "/";
        }
        File localFile = new File(local);
        if (!localFile.exists()) {
            localFile.mkdir();
        }
        for (String remotefile : remotelist) {
            int pos = remotefile.lastIndexOf("/");
            String fileName = remotefile.substring(pos);
            ret = downloadFile(remotefile, local + fileName, retrys);
        }
        return ret;
    }


    public boolean uploadFile(String local, String remote, int retrys) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        File remotefile = new File(remote);
        File localfile = new File(local);
        String remoteDir = remotefile.getParent();
        int retrynum = 0;
        boolean retflag = false;
        InterruptedException ex = null;

        while (!retflag || retrynum < retrys) {
            try {
                retflag = doUpload(localfile, remoteDir, remote);
                if (!retflag) {
                    Thread.sleep(waitForReconnect);
                }
            } catch (InterruptedException e) {
                ex = e;
                retrynum++;
            }
        }
        if (retrynum >= retrys) {
            throw new IOException(ex);
        }
        return retflag;
    }

    public boolean doUpload(File localfile, String remoteDir, String remote) throws IOException {
        boolean retflag ;
        long remotesize = 0;
        long localreadbytes = 0L;
        if (!localfile.exists()) {
            return false;
        }

        if (!exists(remoteDir)) {
            createDir(remoteDir);
        }
        if (exists(remote)) {
            FTPFile[] files = ftpClient.listFiles(remote);
            remotesize = files[0].getSize();
            long localsize = localfile.length();
            if (localsize == remotesize) {
                return true;
            } else if (remotesize > localsize) {
                log.error("remote file length larger than local");
                remotesize = 0L;
                removeFile(remote);
            }
        }

        this.changeDirectory(remoteDir);

        try (RandomAccessFile raf = new RandomAccessFile(localfile.getAbsolutePath(), "r");
             OutputStream out = ftpClient.appendFileStream(remote)) {
            if (remotesize > 0) {
                ftpClient.setRestartOffset(remotesize);
                raf.seek(remotesize);
                localreadbytes = remotesize;
            }
            byte[] bytes = new byte[1024];
            int c;
            while ((c = raf.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localreadbytes += c;
            }
            out.flush();
            retflag = ftpClient.completePendingCommand();
        }
        return retflag;
    }

    public void changeDirectory(String remoteFoldPath) throws IOException {

        if (remoteFoldPath != null) {
            boolean flag = ftpClient.changeWorkingDirectory(remoteFoldPath);
            if (!flag) {
                ftpClient.makeDirectory(remoteFoldPath);
                ftpClient.changeWorkingDirectory(remoteFoldPath);
            }
        }

    }

    public boolean uploadFileAutoMkDir(String local, String remote) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        File localfile = new File(local);
        if (!localfile.exists()) {
            return false;
        }

        if (exists(remote)) {
            log.info("remote file exist,replace");
        }

        String remoteDir = remote.substring(0, remote.lastIndexOf("/"));
        if (!exists(remoteDir)) {
            createDir(remoteDir);
        }

        return uploadFile(local, remote, 10);
    }


    public boolean deleteFile(String remote) throws IOException {
        if (!exists(remote)) {
            return false;
        }

        return ftpClient.deleteFile(remote);
    }


    /**
     * @param remoteDir
     * @return
     * @throws IOException
     */
    public boolean createDir(String remoteDir) throws IOException {
        return ftpClient.makeDirectory(remoteDir);
    }

    /**
     * @param remoteDir
     * @return
     * @throws IOException
     */
    public boolean removeEmptyDir(String remoteDir) throws IOException {
        if (!exists(remoteDir)) {
            log.info("");
            return false;
        }
        if (!isDirectory(remoteDir)) {
            log.info("");
            return false;
        }
        return ftpClient.removeDirectory(remoteDir);
    }

    /**
     * @param remoteDir
     * @throws IOException
     */
    public void removeDirWithFiles(String remoteDir) throws IOException {
        if (!exists(remoteDir)) {
            log.info("");
            return;
        }
        if (!isDirectory(remoteDir)) {
            log.info("");
            return;
        }

        FTPFile[] filelist = ftpClient.listFiles(remoteDir);

        for (FTPFile f : filelist) {//
            if (f.isFile()) {
                ftpClient.deleteFile(remoteDir + "/" + f.getName());
            }
            if (f.isDirectory()) {
                removeDirWithFiles(remoteDir + "/" + f.getName());
            }
        }

        ftpClient.removeDirectory(remoteDir);
    }

    public void removeFile(String remoteFile) throws IOException {
        if (!isFile(remoteFile)) {
            log.info("");
            return;
        }
        ftpClient.deleteFile(remoteFile);
    }

    /**
     * @param old
     * @param newname
     * @return
     * @throws IOException
     */
    public boolean renname(String old, String newname) throws IOException {
        return ftpClient.rename(old, newname);
    }

    /**
     * @param localfiles
     * @param remoteDir
     * @throws IOException
     */
    public void uploadFiles(String[] localfiles, String remoteDir, int retrys) throws IOException {
        if (!isDirectory(remoteDir)) {
            createDir(remoteDir);
        }

        for (String local : localfiles) {
            String filename = local.substring(local.lastIndexOf("/") + 1);
            String remotefile = remoteDir + "/" + filename;
            uploadFile(local, remotefile, retrys);
        }
    }

    public InputStream retriveFileStream(String remote) throws IOException {
        return ftpClient.retrieveFileStream(remote);
    }

    /**
     *
     */
    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWaitForReconnectDelay(int second) {
        this.waitForReconnect = second * 1000L;
    }

}