package com.robin.comm.ftp;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support Multi Thread ftp download and upload and support with
 *
 * @author robinjim
 * @version 1.0
 */

public class MultiThreadFtp extends SimpleFtp {
    //LinkedBlockingQueue<Runnable> queue=new LinkedBlockingQueue<Runnable>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public MultiThreadFtp(String hostName, String userName, String password) {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ENCODING_DEFAULT);
        this.hostname = hostName;
        this.userName = userName;
        this.password = password;
        this.port = PORT_DEFAULT;
    }

    public boolean downLoadLargeFile(String remote, String local, int threads, int retrys) throws IOException {
        connect();
        ExecutorService pool = new ThreadPoolExecutor(2, 4, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        ftpClient.enterLocalPassiveMode();//Enter PassiveMode
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        boolean retflag = false;
        List<Boolean> boolTag = new ArrayList<Boolean>();
        if (!exists(remote)) {
            log.info("remote path not exists " + remote);
            return false;
        }
        long totalsize = filesize(remote);
        logger.info("totalsize=" + totalsize);
        long partsize = totalsize / threads + 1;
        Map<Integer, Boolean> executeMap = new HashMap<Integer, Boolean>();

        try (RandomAccessFile file = new RandomAccessFile(new File(local), "rw")) {
            file.setLength(totalsize);
            file.close();
            disconnect();
            for (int i = 0; i < threads; i++) {
                pool.execute(new DownThread(remote, local, retrys, i, threads, partsize, totalsize, executeMap, boolTag));
            }
            try {
                while (boolTag.isEmpty()) {
                    Thread.sleep(10000);
                }
            } catch (Exception ex) {
                logger.error("", ex);
            }
            pool.shutdown();
            logger.info("job finished with status {}", boolTag.get(0));
            return boolTag.get(0);
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }

    //TODO wait to code
    public void uploadFile(String localFile, String remoteFile, int threads, int retrys) {

    }

    private class DownThread extends Thread {
        String remote;
        String local;
        int retryNums;
        int part;
        long partsize;
        long totalsize;
        int totalpart;
        boolean success = false;
        Map<Integer, Boolean> executeMap;
        List<Boolean> boolTag;

        public DownThread(String remote, String local, int retryNums, int part, int totalpart,
                          long partsize, long totalsize, Map<Integer, Boolean> executeMap, List<Boolean> boolTag) {
            this.remote = remote;
            this.local = local;
            this.retryNums = retryNums;
            this.totalsize = totalsize;
            this.part = part;
            this.partsize = partsize;
            this.executeMap = executeMap;
            this.boolTag = boolTag;
            this.totalpart = totalpart;
        }

        @Override
        public void run() {
            success = doDownloadPart(part, partsize, totalsize, remote, local, retryNums);
            setJobFinshTag(executeMap, part, success, totalpart, boolTag);
        }

        private boolean doDownloadPart(int part, long partsize, long totalsize, String remote, String local, int retryTimes) {
            long formsize = part * partsize;
            long tosize = (part + 1) * partsize;
            int processsize = 0;
            boolean retflag = false;
            if (tosize > totalsize) {
                tosize = totalsize;
            }
            while (!retflag && processsize != partsize) {
                try {
                    processsize += doDownloadSect(formsize + processsize, tosize, local, remote);
                    if (formsize + processsize != tosize && ((Long.parseLong(String.valueOf(processsize)) != partsize || !(tosize == totalsize && processsize != 0)))) {
                        Thread.sleep(waitForReconnect);
                    } else {
                        retflag = true;
                    }
                } catch (Exception ex) {
                    logger.error("", ex);
                }
            }
            return retflag;
        }

        private int doDownloadSect(long formsize, long tosize, String local, String remote) throws IOException {
            int processsize = 0;
            try (RandomAccessFile file = new RandomAccessFile(new File(local), "rw")) {
                file.seek(formsize);
                FTPClient client = getClient();
                client.enterLocalPassiveMode();
                client.setFileType(FTP.BINARY_FILE_TYPE);
                if (formsize != 0) {
                    client.setRestartOffset(formsize);
                }
                InputStream in = client.retrieveFileStream(remote);


                boolean retflag = true;

                byte[] bytes = new byte[81920];
                int c;
                while ((c = in.read(bytes)) != -1) {
                    if (formsize + processsize + c < tosize) {
                        file.write(bytes, 0, c);
                        processsize += c;
                    } else if (formsize + processsize + c == tosize) {
                        file.write(bytes, 0, c);
                        processsize += c;
                        break;
                    } else {
                        int remain = (int) (tosize - processsize - formsize);
                        file.write(bytes, 0, remain);
                        processsize += remain;
                        break;
                    }
                }
                logger.info("finish download section {} stratfrom {} to pos {}", part, formsize, formsize + processsize);
            }
            return processsize;
        }
    }

    public void setJobFinshTag(Map<Integer, Boolean> finshMap, int part, boolean flag, int totalpart, List<Boolean> boolTag) {
        finshMap.put(part, flag);
        boolean finish = false;
        if (flag) {
            int count = 0;
            Iterator<Boolean> iter = finshMap.values().iterator();
            while (iter.hasNext()) {
                if (iter.next()) {
                    count++;
                }
            }
            if (count == totalpart) {
                finish = true;
                boolTag.add(true);
            }
        } else if (finshMap.size() == totalpart) {
            boolTag.add(false);
        }

    }

    public FTPClient getClient() throws IOException {
        FTPClient client = new FTPClient();
        client.setControlEncoding(ENCODING_DEFAULT);
        //client.setFileType(FTPClient.BINARY_FILE_TYPE);
        connect(client);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            try {
                if (client.login(userName, password)) {

                } else {
                    client = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    private void connect(FTPClient client) {
        try {
            client.connect(hostname, port);
            client.setDataTimeout(20000);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void closeClient(FTPClient client) throws IOException {
        client.disconnect();
    }

}

