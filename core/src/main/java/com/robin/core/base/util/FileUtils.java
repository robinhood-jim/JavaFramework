package com.robin.core.base.util;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;

@Slf4j
public class FileUtils {
    public static final List<String> avaiableCompressSuffixs = Collections.unmodifiableList(Lists.newArrayList(Const.CompressType.COMPRESS_TYPE_GZ.toString(),
            Const.CompressType.COMPRESS_TYPE_LZO.toString(), Const.CompressType.COMPRESS_TYPE_BZ2.toString(), Const.CompressType.COMPRESS_TYPE_SNAPPY.toString(),
            Const.CompressType.COMPRESS_TYPE_ZIP.toString(), Const.CompressType.COMPRESS_TYPE_LZMA.toString(),Const.CompressType.COMPRESS_TYPE_LZ4.toString(),Const.CompressType.COMPRESS_TYPE_ZSTD.toString(),Const.CompressType.COMPRESS_TYPE_BROTLI.toString(),Const.CompressType.COMPRESS_TYPE_XZ.toString()));
    public static final List<Const.CompressType> compressTypeEnum =Collections.unmodifiableList(Lists.newArrayList(Const.CompressType.COMPRESS_TYPE_GZ, Const.CompressType.COMPRESS_TYPE_LZO,
            Const.CompressType.COMPRESS_TYPE_BZ2, Const.CompressType.COMPRESS_TYPE_SNAPPY, Const.CompressType.COMPRESS_TYPE_ZIP, Const.CompressType.COMPRESS_TYPE_LZMA,Const.CompressType.COMPRESS_TYPE_LZ4,Const.CompressType.COMPRESS_TYPE_ZSTD,Const.CompressType.COMPRESS_TYPE_BROTLI,Const.CompressType.COMPRESS_TYPE_XZ));

    private static final Map<String,String> contentTypeMap=new HashMap<>();

    static {
        ResourceBundle bundle=ResourceBundle.getBundle("contenttype");
        if(!ObjectUtils.isEmpty(bundle)){
            Iterator<String> iter=bundle.keySet().iterator();
            while (iter.hasNext()){
                String key=iter.next();
                contentTypeMap.put(key,bundle.getString(key));
            }
        }
    }

    private FileUtils(){

    }

    public static FileContent parseFile(String path){
        Assert.isTrue(!ObjectUtils.isEmpty(path),"path must not be null!");
        FileContent fileContent=new FileContent();
        int pos=path.lastIndexOf(File.separator);
        if(pos==-1){
            pos=path.lastIndexOf("/");
        }
        if(pos!=-1){
            String fileName=path.substring(pos+1);
            String filePath=path.substring(0,pos);
            fileContent.setFilePath(filePath);
            String[] parts=fileName.split("\\.");
            List<String> sepParts=new ArrayList<>();
            for(int i=parts.length-1;i>0;i--){
                if(Const.CompressType.COMPRESS_TYPE_NONE.equals(fileContent.getCompressType())){
                    Const.CompressType compressType=getFileCompressType(parts[i]);
                    if(!Const.CompressType.COMPRESS_TYPE_NONE.equals(compressType)) {
                        fileContent.setCompressType(compressType);
                        if (contentTypeMap.containsKey(parts[i].toLowerCase())) {
                            fileContent.setContentType(contentTypeMap.get(parts[i].toLowerCase()));
                        }
                    }else{
                        parseFileFormat(fileContent,parts[i],sepParts);
                    }
                }else {
                    parseFileFormat(fileContent,parts[i],sepParts);
                }
            }
            sepParts.add(parts[0]);
            Collections.reverse(sepParts);
            fileContent.setFileName(StringUtils.join(sepParts,"."));
        }
        return fileContent;
    }
    private static void parseFileFormat(FileContent content,String suffix,List<String> sepParts){
        if(ObjectUtils.isEmpty(content.getFileFormat())){
            content.setFileFormat(Const.FILEFORMATSTR.forName(suffix.toLowerCase()));
            if(ObjectUtils.isEmpty(content.getContentType())){
                if(contentTypeMap.containsKey(suffix.toLowerCase())){
                    content.setContentType(contentTypeMap.get(suffix.toLowerCase()));
                }
            }
        }else{
            sepParts.add(suffix);
        }
    }


    public static Const.CompressType getFileCompressType(String suffix) {
        Const.CompressType type = Const.CompressType.COMPRESS_TYPE_NONE;
        if (suffix!=null && !suffix.isEmpty() && avaiableCompressSuffixs.contains(suffix.toLowerCase())) {
            type = compressTypeEnum.get(avaiableCompressSuffixs.indexOf(suffix.toLowerCase()));
        }
        return type;
    }
    public static String getContentType(String fileFormat){
        return contentTypeMap.containsKey(fileFormat.toLowerCase())?contentTypeMap.get(fileFormat.toLowerCase()):null;
    }
    public static String getContentType(DataCollectionMeta meta){
        String fileType=meta.getFileFormat();
        if(ObjectUtils.isEmpty(fileType)){
            FileUtils.FileContent content=FileUtils.parseFile(meta.getPath());
            fileType=content.getFileFormat().getValue();
        }
        return getContentType(fileType);
    }
    public static boolean mkDirWithGroupAndUser(String path,String group,String user) {
        FileUtil.mkdir(path);
        Path filePath= Paths.get(path);
        Set<PosixFilePermission> userPermission=new HashSet<>();
        userPermission.add(PosixFilePermission.OWNER_READ);
        userPermission.add(PosixFilePermission.OWNER_WRITE);
        Set<PosixFilePermission> groupPermission=new HashSet<>();
        groupPermission.add(PosixFilePermission.GROUP_READ);
        groupPermission.add(PosixFilePermission.GROUP_WRITE);
        try {
            Files.setPosixFilePermissions(filePath, userPermission);
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            Files.getFileAttributeView(filePath, PosixFileAttributeView.class).setOwner(lookupService.lookupPrincipalByName(user));
            Files.getFileAttributeView(filePath, PosixFileAttributeView.class).setGroup(lookupService.lookupPrincipalByGroupName(group));
            Files.setPosixFilePermissions(filePath, groupPermission);
        }catch (IOException ex){
            log.error("{}",ex);
            return false;
        }
        return true;
    }
    public static boolean setWithGroupAndUser(File file,String group,String user){
        Path filePath= Paths.get(file.getPath());
        Set<PosixFilePermission> userPermission=new HashSet<>();
        userPermission.add(PosixFilePermission.OWNER_READ);
        userPermission.add(PosixFilePermission.OWNER_WRITE);
        Set<PosixFilePermission> groupPermission=new HashSet<>();
        groupPermission.add(PosixFilePermission.GROUP_READ);
        groupPermission.add(PosixFilePermission.GROUP_WRITE);
        try {
            Files.setPosixFilePermissions(filePath, userPermission);
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            Files.getFileAttributeView(filePath, PosixFileAttributeView.class).setOwner(lookupService.lookupPrincipalByName(user));
            Files.getFileAttributeView(filePath, PosixFileAttributeView.class).setGroup(lookupService.lookupPrincipalByGroupName(group));
            Files.setPosixFilePermissions(filePath, groupPermission);
        }catch (IOException ex){
            log.error("{}",ex);
            return false;
        }
        return true;
    }
    public static boolean mkDirReclusive(String path) throws FileExistsException{
        int pos=path.contains(File.separator)?path.lastIndexOf(File.separator):path.lastIndexOf("/");
        Assert.isTrue(pos>0,"not a valid filePath");
        String processPath=path.substring(0,pos);
        if(processPath.startsWith("file:///")){
            processPath=processPath.substring(8);
        }
        List<String> pathParts=processPath.contains(File.separator)?Lists.newArrayList(processPath.split(File.separator)):Lists.newArrayList(processPath.split("/"));
        if(pathParts.size()==1){
            return true;
        }
        StringBuilder builder=new StringBuilder();

        builder.append(pathParts.get(0)).append(File.separator);
        for(int i=1;i<pathParts.size();i++){
            builder.append(pathParts.get(i));
            mkdir(builder.toString());
            builder.append(File.separator);
        }
        return true;

    }
    public static boolean mkdir(String path) throws FileExistsException {
        File file=new File(path);
        if(file.isDirectory()){
            return true;
        }else if(!file.exists()){
            return file.mkdir();
        }else{
            throw new FileExistsException("path already exists as file");
        }
    }
    public static String getWorkingPath(DataCollectionMeta meta){
        return !ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.WORKINGPATHPARAM))
                ? meta.getResourceCfgMap().get(ResourceConst.WORKINGPATHPARAM).toString()
                : org.apache.commons.io.FileUtils.getTempDirectoryPath();
    }
    @Data
    public static class FileContent{
        private String fileName;
        private String filePath;
        private Const.FILEFORMATSTR fileFormat;
        private String contentType;
        private Const.CompressType compressType= Const.CompressType.COMPRESS_TYPE_NONE;
    }
    public static void main(String[] args){
        String path="tmp/1234.xlsx";
        System.out.println(parseFile(path));
    }
}
