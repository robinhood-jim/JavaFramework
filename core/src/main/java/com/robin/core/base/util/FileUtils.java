package com.robin.core.base.util;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

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

    private FileUtils(){

    }
    public static String parseFileFormat(String path, List<String> suffix) {
        String filePath = null;
        if (suffix == null) {
            suffix = new ArrayList<>();
        }
        if (path != null && !path.trim().isEmpty()) {
            int pos = path.lastIndexOf(File.separator);
            if (pos == -1) {
                pos = path.lastIndexOf("/");
            }
            if (pos != -1) {
                String fileName = path.substring(pos + 1);
                String[] arr = fileName.split("\\.");
                int lastpos = fileName.lastIndexOf(".");
                filePath = fileName.substring(0, lastpos);
                for (int i = arr.length - 1; i > 0; i--) {
                    suffix.add(arr[i]);
                }
            }
        }
        return filePath;
    }

    public static Const.CompressType getFileCompressType(List<String> suffixList) {
        Const.CompressType type = Const.CompressType.COMPRESS_TYPE_NONE;
        if (!suffixList.isEmpty() && avaiableCompressSuffixs.contains(suffixList.get(0).toLowerCase())) {
            type = compressTypeEnum.get(avaiableCompressSuffixs.indexOf(suffixList.get(0)));
        }
        return type;
    }
    public static Const.CompressType getFileCompressType(String suffix) {
        Const.CompressType type = Const.CompressType.COMPRESS_TYPE_NONE;
        if (suffix!=null && !suffix.isEmpty() && avaiableCompressSuffixs.contains(suffix.toLowerCase())) {
            type = compressTypeEnum.get(avaiableCompressSuffixs.indexOf(suffix.toLowerCase()));
        }
        return type;
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
}
