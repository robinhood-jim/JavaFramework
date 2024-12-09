package com.robin.core.base.util;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
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
            content.setFileFormat(suffix);
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
            fileType=content.getFileFormat();
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
    public static String getWorkingPath(DataCollectionMeta meta){
        return !ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.WORKINGPATHPARAM))
                ? meta.getResourceCfgMap().get(ResourceConst.WORKINGPATHPARAM).toString()
                : org.apache.commons.io.FileUtils.getTempDirectoryPath();
    }
    @Data
    public static class FileContent{
        private String fileName;
        private String filePath;
        private String fileFormat;
        private String contentType;
        private Const.CompressType compressType= Const.CompressType.COMPRESS_TYPE_NONE;
    }
    public static void main(String[] args){
        String path="file:///e:/tmp/test/test1.avro.cs.lz4";
        System.out.println(parseFile(path));
    }
}
