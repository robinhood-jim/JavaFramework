package com.robin.comm.ftp;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class SimpleFtp {
	protected static Log log = LogFactory.getLog(SimpleFtp.class);
	public static final int PORT_DEFAULT = 21;
	public static final String ENCODING_DEFAULT = "UTF-8";
	protected FTPClient ftpClient;
	protected String hostname;
	protected String userName;
	protected String password;
	protected int port;
	protected long waitForReconnect=60000;
	
	public SimpleFtp(){
		ftpClient = new FTPClient();
		ftpClient.setControlEncoding(ENCODING_DEFAULT);
	}
	public SimpleFtp(String hostName,String userName,String password){
		ftpClient = new FTPClient();
		ftpClient.setControlEncoding(ENCODING_DEFAULT);
		this.hostname=hostName;
		this.userName=userName;
		this.password=password;
		this.port=PORT_DEFAULT;
	}
	
	public void setControlEncoding(String encode){
		ftpClient.setControlEncoding(encode);
	}
	public boolean connect(){
		return connect(hostname,port,userName,password);
	}
	public boolean connect(String host, String username, String password){
		return connect(host, PORT_DEFAULT, username, password);
	}

	public boolean connect(String host, int port, String username, String password){
		try {
			ftpClient.connect(host, port);
			ftpClient.setDataTimeout(20000);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
			try {
				if(ftpClient.login(username, password)){
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	public boolean exists(String remote) throws IOException{
		if (isDirectory(remote)){//dir
			return true;
		}else{
			FTPFile[] files = ftpClient.listFiles(remote);
			return files.length>0;
		}
	}
	public long filesize(String remote) throws IOException{
		if (isDirectory(remote)){//dir
			return -1;
		}else{
			FTPFile[] files = ftpClient.listFiles(remote);
			return files[0].getSize();
		}
	}
	
	public boolean isDirectory(String remote) throws IOException{
		return ftpClient.changeWorkingDirectory(remote);
	}

	public boolean isFile(String remote) throws IOException{
		return exists(remote)&&(!isDirectory(remote));
	}

	public List<String> listFile(String... args) throws IOException{
		int length=args.length;
		List<String> retStr=new ArrayList<String>();
		String path=args[0];
		List<String> ignorefiles=null;
		if(length>=2){
			ignorefiles=new ArrayList<String>();
			for(int i=1;i<args.length;i++)
				ignorefiles.add(args[i]);
		}
		FTPFile[] files = ftpClient.listFiles(path);
		for (int i = 0; i < files.length; i++) {
			if(ignorefiles==null || !ignorefiles.contains(files[i].getName().toLowerCase())){
				retStr.add(path+"/"+files[i].getName());
			}
		}
		return retStr;
	}
	public List<FileInfo> listFileDetail(String path,String interfaceName) throws IOException{
		List<FileInfo> retStr=new ArrayList<FileInfo>();
		try{
		
		FTPFile[] files = ftpClient.listFiles(path);
		for (int i = 0; i < files.length; i++) {
			retStr.add(new FileInfo(path, files[i].getName(), files[i].getSize(), files[i].getTimestamp(),interfaceName));
		}
		}catch(Exception ex){
			log.error(ex);
		}
		return retStr;
	}
	
	public List<String> listFile(String path,Date afterTime) throws IOException{
		List<String> retStr=new ArrayList<String>();
		Calendar cal1=Calendar.getInstance();
		cal1.setTime(afterTime);
		FTPFile[] files = ftpClient.listFiles(path);
		for (int i = 0; i < files.length; i++) {
			Calendar filescal=files[i].getTimestamp();
			if(filescal.after(cal1))
				retStr.add(path+"/"+files[i].getName());
		}
		return retStr;
	}
	public List<String> listFileWithPattern(String pathname,String patterntxt) throws IOException{
		List<String> retStr=new ArrayList<String>();
		ftpClient.changeWorkingDirectory(pathname);
		String[] names = ftpClient.listNames(patterntxt);
		String relatepath=pathname.endsWith("/")?pathname:pathname+"/";
		for (int i = 0; i < names.length; i++) {
			String tmppath=relatepath+names[i];
			retStr.add(tmppath);
		}
		return retStr;	
	}
	

	public boolean downloadFile(String remote,String local,int retrys) throws IOException{
		ftpClient.enterLocalPassiveMode();//Enter PassiveMode
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		int retrynum=0;
		boolean retflag=false;
		if (!exists(remote)){
			log.info("remote path not exists "+remote);
			return false;
		}
		if (isDirectory(remote)){
			log.info("remote file is directory");
			return false;
		}
		while(!retflag && retrynum<retrys){
			if(retrynum>0){
				try{
					retflag=doDownload(local, remote);
					if(!retflag)
						Thread.sleep(waitForReconnect);
				}catch(InterruptedException ex){
					log.error(ex);
					retrynum++;
				}
			}
		}
		return retflag;
	}
	private boolean doDownload(String local,String remote) throws IOException{
		boolean retflag=true;
		File localfile = new File(local);
		long lRemoteSize = filesize(remote);
		if (localfile.exists()) {
			log.info("local file Exist");
			long localSize = localfile.length();
			if (localSize >= lRemoteSize) {
				log.info("file length not fit");
				return true;
			}
			InputStream in = ftpClient.retrieveFileStream(remote);
			FileOutputStream os = new FileOutputStream(localfile, true);
			ftpClient.setRestartOffset(localSize);
			try {
				byte[] bytes = new byte[8192];
				int c;
				while ((c = in.read(bytes)) != -1) {
					os.write(bytes, 0, c);
				}
				retflag=true;
			} catch (IOException ex) {
				log.error(ex);
				retflag=false;
			}finally{
				in.close();
				if(os!=null){
					os.close();
				}
			}
		}else{
			String path=localfile.getParent();
			File parentpath=new File(path);
			if(!parentpath.exists()){
				System.out.println("create parent folder");
				parentpath.mkdir();
			}
			FileOutputStream os = new FileOutputStream(localfile);
			InputStream in = ftpClient.retrieveFileStream(remote);
			try {
				byte[] bytes = new byte[8192];
				int c;
				while ((c = in.read(bytes)) != -1) {
					os.write(bytes, 0, c);
				}
				retflag=true;
			} catch (IOException ex) {
				ex.printStackTrace();
				retflag=false;
			}finally{
				in.close();
				if(os!=null){
					os.close();
				}
			}
		}
		return retflag;
	}

	public boolean downloadFile(List<String> remotelist,String local,int retrys) throws IOException{
		boolean ret=false;
		if(!local.endsWith("/"))
			local+="/";
		File localFile=new File(local);
		if(!localFile.exists())
			localFile.mkdir();
		for (int i = 0; i < remotelist.size(); i++) {
			String remotefile=remotelist.get(i);
			int pos=remotefile.lastIndexOf("/");
			String fileName=remotefile.substring(pos,remotefile.length());
			ret=downloadFile(remotefile, local+fileName,retrys);
		}
		return ret;
	}
	

	public boolean uploadFile(String local, String remote,int retrys) throws IOException{
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		File remotefile=new File(remote);
		File localfile = new File(local);
		String remoteDir=remotefile.getParent();
		int retrynum = 0;
		boolean retflag = false;

		while (!retflag || retrynum < retrys) {
			if(retrynum>0){
				try{
					retflag=doUpload(localfile, remoteDir, remote);
					if(!retflag)
					Thread.currentThread().sleep(waitForReconnect);
				}catch(InterruptedException ex){
					log.error(ex);
					retrynum++;
				}
			}
		}
		return retflag;
	}
	public boolean doUpload(File localfile,String remoteDir,String remote) throws IOException{
		boolean retflag = false;
		long remotesize = 0;
		long localreadbytes = 0L;
		if (!localfile.exists()) {
			return false;
		}
		
		if (!exists(remoteDir)){
			createDir(remoteDir);
		}
		if (exists(remote)) {
			FTPFile[] files = ftpClient.listFiles(remote);
			remotesize = files[0].getSize();
			long localsize = localfile.length();
			if (localsize == remotesize)
				return true;
			else if (remotesize > localsize) {
				log.error("remote file length larger than local");
				remotesize=0L;
				removeFile(remote);
			}
		}

		this.changeDirectory(remoteDir);
		try {
			RandomAccessFile raf = new RandomAccessFile(localfile.getAbsolutePath(), "r");
			OutputStream out = ftpClient.appendFileStream(remote);
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
			raf.close();
			out.close();
			retflag = ftpClient.completePendingCommand();

		} catch (Exception ex) {
			log.error(ex);
			retflag=false;
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

	public boolean uploadFileAutoMkDir(String local, String remote) throws IOException{
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		
		File localfile = new File(local);
		if (!localfile.exists()){
			return false;
		}
		
		if (exists(remote)){
			log.info("remote file exist,replace");
		}
		
		String remoteDir = remote.substring(0, remote.lastIndexOf("/"));
		if (!exists(remoteDir)){
			createDir(remoteDir);
		}
		
		return uploadFile(local, remote, 10);
	}
	

	public boolean deleteFile(String remote) throws IOException{
		if (!exists(remote)){
			return false;
		}
		
		return ftpClient.deleteFile(remote);
	}
	
	
	/**
	 * @param remoteDir
	 * @return
	 * @throws IOException
	 */
	public boolean createDir(String remoteDir) throws IOException{
		return ftpClient.makeDirectory(remoteDir);
	}
	
	/**
	 *
	 * @param remoteDir
	 * @return
	 * @throws IOException
	 */
	public boolean removeEmptyDir(String remoteDir) throws IOException{
		if (!exists(remoteDir)){
			log.info("");
			return false;
		}
		if (!isDirectory(remoteDir)){
			log.info("");
			return false;
		}
		return ftpClient.removeDirectory(remoteDir);
	}
	
	/**
	 * 
	 * @param remoteDir
	 * @throws IOException
	 */
	public void removeDirWithFiles(String remoteDir) throws IOException{
		if (!exists(remoteDir)){
			log.info("");
			return;
		}
		if (!isDirectory(remoteDir)){
			log.info("");
			return;
		}
		
		FTPFile[] filelist = ftpClient.listFiles(remoteDir);
		
		for (FTPFile f : filelist){//
			if (f.isFile()){
				ftpClient.deleteFile(remoteDir+"/"+f.getName());
			}
			if (f.isDirectory()){
				removeDirWithFiles(remoteDir+"/"+f.getName());
			}
		}
		
		ftpClient.removeDirectory(remoteDir);
	}
	public void removeFile(String remoteFile) throws IOException{
		if (!isFile(remoteFile)){
			log.info("");
			return;
		}
		ftpClient.deleteFile(remoteFile);
	}
	
	/**
	 * 
	 * @param old
	 * @param newname
	 * @return
	 * @throws IOException
	 */
	public boolean renname(String old, String newname) throws IOException{
		return ftpClient.rename(old, newname);
	}
	
	/**
	 * 
	 * @param localfiles
	 * @param remoteDir
	 * @throws IOException
	 */
	public void uploadFiles(String[] localfiles, String remoteDir,int retrys) throws IOException{
		if (!isDirectory(remoteDir)){
			createDir(remoteDir);
		}
		
		for (String local : localfiles){
			String filename = local.substring(local.lastIndexOf("/")+1);
			String remotefile = remoteDir+"/"+filename;
			uploadFile(local, remotefile,retrys);
		}
	}
	public InputStream retriveFileStream(String remote) throws IOException{
		return ftpClient.retrieveFileStream(remote);
	}
	
	/**
	 *
	 */
	public void disconnect(){
		try {
			if (ftpClient.isConnected()){
				ftpClient.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setWaitForReconnectDelay(int second){
		this.waitForReconnect=second*1000;
	}
	
}