package com.robin.comm.subversion.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


public class SvnUtil {
	private String svnUrl;
	private SVNURL reposUrl=null;
	private Logger logger=LoggerFactory.getLogger(getClass());
	private  SVNClientManager clientManager=null;
	public SvnUtil(String svnUrl){
		this.svnUrl=svnUrl;
		try{
			reposUrl=SVNURL.parseURIEncoded(svnUrl);
		}catch(Exception ex){
			
		}
	}
	public  void setupLibrary() { 
        if(svnUrl.startsWith("svn:"))
        	SVNRepositoryFactoryImpl.setup(); 
        else
        	DAVRepositoryFactory.setup();
        FSRepositoryFactory.setup();  
    }  
	 public  void authSvn(String username,  
	            String password) {  
		 	setupLibrary();
	        SVNRepository repository = null;  
	        try {  
	            repository = SVNRepositoryFactory.create(reposUrl);  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);  
	        repository.setAuthenticationManager(authManager);  
	  
	        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);  
	        clientManager = SVNClientManager.newInstance(options,  
	                authManager);  
	    }  
	 /** 
	     * Make directory in svn repository 
	     * @param clientManager 
	     * @param url  
	     *          eg: http://svn.ambow.com/wlpt/bsp/trunk  
	     * @param commitMessage 
	     * @return 
	     * @throws SVNException 
	     */  
	    public  SVNCommitInfo makeDirectory(SVNURL url, String commitMessage) {  
	        try {  
	            return clientManager.getCommitClient().doMkDir(  
	                    new SVNURL[] { url }, commitMessage);  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	        return null;  
	    }  
	    /** 
	     * Imports an unversioned directory into a repository location denoted by a 
	     *  destination URL 
	     * @param clientManager 
	     * @param localPath 
	     *          a local unversioned directory or singal file that will be imported into a  
	     *          repository; 
	     * @param dstURL 
	     *          a repository location where the local unversioned directory/file will be  
	     *          imported into 
	     * @param commitMessage 
	     * @param isRecursive 
	     * @return 
	     */  
	    public SVNCommitInfo importDirectory(File localPath, String commitMessage,  
	            boolean isRecursive) {  
	        try {  
	            return clientManager.getCommitClient().doImport(localPath, reposUrl,  
	                    commitMessage, null, true, true,  
	                    SVNDepth.fromRecurse(isRecursive));  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	        return null;  
	    }  
	    /** 
	     * Puts directories and files under version control 
	     * @param clientManager 
	     *          SVNClientManager 
	     * @param wcPath  
	     *          work copy path 
	     */  
	    public void addEntry(File wcPath) {  
	        try {  
	            clientManager.getWCClient().doAdd(new File[] { wcPath },true,false, false, SVNDepth.INFINITY, false, false,true);  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	    }  
	    /** 
	     * Collects status information on a single Working Copy item 
	     * @param clientManager 
	     * @param wcPath 
	     *          local item's path 
	     * @param remote 
	     *          true to check up the status of the item in the repository,  
	     *          that will tell if the local item is out-of-date (like '-u' option in the SVN client's  
	     *          'svn status' command), otherwise false 
	     * @return 
	     * @throws SVNException 
	     */  
	    public SVNStatus showStatus(File wcPath, boolean remote) {  
	        SVNStatus status = null;  
	        try {  
	            status = clientManager.getStatusClient().doStatus(wcPath, remote);  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	        return status;  
	    }  
	    /** 
	     * Commit work copy's change to svn 
	     * @param clientManager 
	     * @param wcPath  
	     *          working copy paths which changes are to be committed 
	     * @param keepLocks 
	     *          whether to unlock or not files in the repository 
	     * @param commitMessage 
	     *          commit log message 
	     * @return 
	     * @throws SVNException 
	     */  
	    public SVNCommitInfo commit(File wcPath, boolean keepLocks, String commitMessage) {  
	        try {  
	            return clientManager.getCommitClient().doCommit(  
	                    new File[] { wcPath }, keepLocks, commitMessage, null,  
	                    null, false, false, SVNDepth.INFINITY);  
	        } catch (SVNException e) {  
	            logger.error(e.getMessage(),e.getErrorMessage());  
	        }  
	        return null;  
	    }
		public SVNURL getReposUrl() {
			return reposUrl;
		}
		public void setReposUrl(SVNURL reposUrl) {
			this.reposUrl = reposUrl;
		}  
		/** 
	     * Updates a working copy (brings changes from the repository into the working copy). 
	     * @param clientManager 
	     * @param wcPath 
	     *          working copy path 
	     * @param updateToRevision 
	     *          revision to update to 
	     * @param depth 
	     *        
	     * @return 
	     * @throws SVNException 
	     */  
	    public long update(File wcPath,SVNRevision updateToRevision, SVNDepth depth) {  
	        SVNUpdateClient updateClient = clientManager.getUpdateClient();  
	  
	        /* 
	         * sets externals not to be ignored during the update 
	         */  
	        updateClient.setIgnoreExternals(false);  
	  
	        /* 
	         * returns the number of the revision wcPath was updated to 
	         */  
	        try {  
	            return updateClient.doUpdate(wcPath, updateToRevision,depth, false, false);  
	        } catch (SVNException e) {  
	            logger.error(e.getErrorMessage().toString());  
	        }  
	        return 0;  
	    }  
	      
	    /** 
	     * recursively checks out a working copy from url into wcDir 
	     * @param clientManager 
	     * @param url 
	     *          a repository location from where a Working Copy will be checked out 
	     * @param revision 
	     *          the desired revision of the Working Copy to be checked out 
	     * @param destPath 
	     *          the local path where the Working Copy will be placed 
	     * @param depth 
	     *          checkout����ȣ�Ŀ¼����Ŀ¼���ļ� 
	     * @return 
	     * @throws SVNException 
	     */  
	    public long checkout(SVNURL url,SVNRevision revision, File destPath, SVNDepth depth) {  
	  
	        SVNUpdateClient updateClient = clientManager.getUpdateClient();  
	        /* 
	         * sets externals not to be ignored during the checkout 
	         */  
	        updateClient.setIgnoreExternals(false);  
	        /* 
	         * returns the number of the revision at which the working copy is 
	         */  
	        try {  
	            return updateClient.doCheckout(url, destPath, revision, revision,depth, false);  
	        } catch (SVNException e) {  
	            logger.error(e.getErrorMessage().toString());  
	        }  
	        return 0;  
	    }  
	      
	    /** 
	     * ȷ��path�Ƿ���һ�������ռ� 
	     * @param path 
	     * @return 
	     */  
	    public boolean isWorkingCopy(File path){  
	        if(!path.exists()){  
	            logger.warn("'" + path + "' not exist!");  
	            return false;  
	        }  
	        try {  
	            if(null == SVNWCUtil.getWorkingCopyRoot(path, false)){  
	                return false;  
	            }  
	        } catch (SVNException e) {  
	            logger.error(e.getErrorMessage().toString());  
	        }  
	        return true;  
	    }  
	      
	    /** 
	     * ȷ��һ��URL��SVN���Ƿ���� 
	     * @param url 
	     * @return 
	     */  
	    public static boolean isURLExist(SVNURL url,String username,String password){  
	        try {  
	            SVNRepository svnRepository = SVNRepositoryFactory.create(url);  
	            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);  
	            svnRepository.setAuthenticationManager(authManager);  
	            SVNNodeKind nodeKind = svnRepository.checkPath("", -1);  
	            return nodeKind == SVNNodeKind.NONE ? false : true;   
	        } catch (SVNException e) {  
	            e.printStackTrace();  
	        }  
	        return false;  
	    }  
	    
}
