package com.robin.comm.subversion.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.util.git</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年02月25日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class GitUtil {
    private static Map<String,Git> gitMap=new HashMap<String, Git>();
    public static void cloneProject(String repName,String cloneUrl,String localPath,String userName,String password) throws GitAPIException{
        clone(repName,cloneUrl,localPath,userName,password);
    }
    public static void cloneProject(String cloneUrl,String localPath) throws GitAPIException{
        Git.cloneRepository().setURI(cloneUrl).setDirectory(new File(localPath)).call();
    }
    public static void pull(String localDir) throws Exception{
        Git.open(new File(localDir)).pull();
    }
    public static void switchToBranch(String localDir,String branch) throws GitAPIException, IOException {
        List<Ref> list=Git.open(new File(localDir)).branchList().call();
        for(Ref ref:list){
            if(ref.getName().equals(branch)){
                Git.open(new File(localDir)).branchDelete().setBranchNames(branch).setForce(true).call();
                break;
            }
        }
    }
    public static void addResource(String localDir,String patterner) throws GitAPIException,IOException{
        Git.open(new File(localDir)).add().addFilepattern(patterner).call();
    }
    public static void commit(String localDir,String message)throws GitAPIException,IOException{
        Git.open(new File(localDir)).commit().setMessage(message).call();
        Git.open(new File(localDir)).push().call();
    }
    public static void commit(String localDir,String message,String userName,String password)throws GitAPIException,IOException{
        Git.open(new File(localDir)).commit().setMessage(message).call();
        Git.open(new File(localDir)).push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName,password)).call();
    }
    public static void initGitRepositoryByConf(String repName,String remoteUrl,String localPath) throws GitAPIException{
        Git git=null;
        if(!gitMap.containsKey(repName) || gitMap.get(repName)==null){
            git =Git.cloneRepository().setURI(remoteUrl).setDirectory(new File(localPath)).call();
            gitMap.put(repName,git);
        }
    }
    private static Git clone(String repName,String remoteUrl,String localPath,String userName,String password) throws GitAPIException {
        Git git=null;
        if(!gitMap.containsKey(repName) || gitMap.get(repName)==null){
            git =Git.cloneRepository().setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName,password)).setURI(remoteUrl).setDirectory(new File(localPath)).call();
            gitMap.put(repName,git);
        }else{
            git=gitMap.get(repName);
        }
        return git;
    }
}
