/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.comm.subversion.util;


import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitUtil {
    private static Map<String, Git> gitMap = new HashMap<String, Git>();
    private GitUtil(){

    }

    public static void cloneProject(String repName, String cloneUrl, String localPath, String userName, String password) throws GitAPIException {
        clone(repName, cloneUrl, localPath, userName, password);
    }

    public static void cloneProject(String cloneUrl, String localPath) throws GitAPIException {
        Git.cloneRepository().setURI(cloneUrl).setDirectory(new File(localPath)).call();
    }

    public static void pull(String localDir) throws IOException {
        try (Git git = Git.open(new File(localDir))) {
            git.pull();
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static void switchToBranch(String localDir, String branch) throws GitAPIException, IOException {
        try (Git git = Git.open(new File(localDir))) {
            List<Ref> list = git.branchList().call();
            for (Ref ref : list) {
                if (ref.getName().equals(branch)) {
                    git.branchDelete().setBranchNames(branch).setForce(true).call();
                    break;
                }
            }
        }
    }

    public static void addResource(String localDir, String patterner) throws GitAPIException, IOException {
        try (Git git = Git.open(new File(localDir))) {
            git.add().addFilepattern(patterner).call();
        }
    }

    public static Iterable<PushResult> commit(Git git, String message) throws GitAPIException, IOException {
        git.commit().setMessage(message).call();
        return git.push().call();
    }

    public static boolean add(Git git, String filePath) throws Exception {
        boolean runningOk = false;
        try (Repository repository = git.getRepository()) {
            File myFile = new File(repository.getDirectory().getParent(), filePath);
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            git.add().addFilepattern(filePath).call();
            runningOk = true;
        }
        return runningOk;
    }

    public static Iterable<PushResult> commit(String localDir, String message, String userName, String password) throws GitAPIException, IOException {
        try (Git git = Git.open(new File(localDir))) {
            git.commit().setMessage(message).call();
            return git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).call();
        }
    }

    public static void initGitRepositoryByConf(String repName, String remoteUrl, String localPath) throws GitAPIException {
        Git git = null;
        if (!gitMap.containsKey(repName) || gitMap.get(repName) == null) {
            git = Git.cloneRepository().setURI(remoteUrl).setDirectory(new File(localPath)).call();
            gitMap.put(repName, git);
        }
    }

    private static Git clone(String repName, String remoteUrl, String localPath, String userName, String password) throws GitAPIException {
        Git git = null;
        if (!gitMap.containsKey(repName) || gitMap.get(repName) == null) {
            git = Git.cloneRepository().setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).setURI(remoteUrl).setDirectory(new File(localPath)).call();
            gitMap.put(repName, git);
        } else {
            git = gitMap.get(repName);
        }
        return git;
    }
    public static File initRepoistory(String baseDir) throws Exception{
        File file=new File(baseDir);
        if(file.exists()){
            FileUtils.deleteDirectory(file);
        }
        try(Git git=Git.init().setDirectory(file).call()){
            return git.getRepository().getDirectory();
        }
    }

    public static Repository openJGitRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }

    public static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }
        try(Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"))) {
            // create the directory
            repository.create();
            return repository;
        }
    }
}
