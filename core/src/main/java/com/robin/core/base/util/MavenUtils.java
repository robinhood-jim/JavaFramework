package com.robin.core.base.util;

import com.google.common.collect.Lists;
import com.robin.core.base.shell.CommandLineExecutor;
import com.robin.core.hardware.MachineIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

@Slf4j
public class MavenUtils {
    public static String getMavenRepository(){
        File m2Path=new File(FileUtils.getUserDirectoryPath()+"/.m2/settings.xml");
        String localRepository=null;
        try {
            if (m2Path.exists()) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(m2Path);
                if(!ObjectUtils.isEmpty(document)){
                    Element element=document.getRootElement().element("localRepository");
                    if(element!=null){
                        localRepository=element.getText();
                    }
                }
            }else{
                String mavenHome=System.getenv("MAVEN_HOME");
                if(!ObjectUtils.isEmpty(mavenHome)){
                    File configPath=new File(mavenHome+"/conf/settings.xml");
                    if(configPath.exists()){
                        SAXReader reader = new SAXReader();
                        Document document = reader.read(configPath);
                        if(!ObjectUtils.isEmpty(document)){
                            Element element=document.getRootElement().element("localRepository");
                            if(element!=null){
                                localRepository=element.getText();
                            }
                        }
                    }

                }else{
                    log.error("can not get maven config from m2 path or MAVEN_HOME!");
                }
            }
        }catch (DocumentException ex){
            log.error("{}",ex.getMessage());
        }
        return localRepository;
    }
    public static List<String> getDepenendcyList(String mavenRepoPath,String mavenFilePath){
        List<String> dependencyList=new ArrayList<>();
        try {
            String content = null;
            if(MachineIdUtils.isWindows()) {
                content=CommandLineExecutor.getInstance().executeCmd(Lists.newArrayList("cmd.exe", "/c", "CD " + mavenFilePath + " & mvn dependency:tree -D outputType=dot"));
            }else if(MachineIdUtils.isLinux() || MachineIdUtils.isMacosName()){
                content=CommandLineExecutor.getInstance().executeCmd(Lists.newArrayList("sh", "-c", "cd " + mavenFilePath + "; mvn dependency:tree -D outputType=dot"));
            }
            process(mavenRepoPath, dependencyList, content);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
        return dependencyList;
    }

    private static void process(String mavenRepoPath, List<String> dependencyList, String content) {
        try(Scanner scanner = new Scanner(content)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!ObjectUtils.isEmpty(line) && line.contains("->")) {
                    String[] arr = StringUtils.split(line, "->");
                    if (arr.length == 2) {
                        String trimStr = arr[1].trim().replace("\"", "").replace(";","");

                        String[] dependencyPart = trimStr.trim().split(":");
                        if (dependencyPart.length>=5 ) {
                            if ("compile".equalsIgnoreCase(dependencyPart[4])) {
                                dependencyList.add(mavenRepoPath + File.separator + dependencyPart[0].replace(".", File.separator) + File.separator + dependencyPart[1] + File.separator + dependencyPart[3] + File.separator + dependencyPart[1] + "-" + dependencyPart[3] + ".jar");
                            } else {
                                log.info("{} scope {}", trimStr, dependencyPart[4]);
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
    }

    public static void main(String[] args){
        String mavenPath=getMavenRepository();
        System.out.println(getDepenendcyList(mavenPath,"E:/dev/workspaceframe/JavaFramework/core"));

        /*Collection<File> files=FileUtils.listFiles(new File("e:/tmp/testoutput/META-INF/maven"), FileFilterUtils.suffixFileFilter("xml"), DirectoryFileFilter.INSTANCE);
        if (!CollectionUtils.isEmpty(files)) {
            for(File file:files) {
                System.out.println(getDepenendcyList(mavenPath, file.getParent()));
            }
        }*/

    }
}
