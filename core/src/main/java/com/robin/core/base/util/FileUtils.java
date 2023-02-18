package com.robin.core.base.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtils {
    public static final List<String> avaiableCompressSuffixs = Collections.unmodifiableList(Arrays.asList(Const.CompressType.COMPRESS_TYPE_GZ.toString(),
            Const.CompressType.COMPRESS_TYPE_LZO.toString(), Const.CompressType.COMPRESS_TYPE_BZ2.toString(), Const.CompressType.COMPRESS_TYPE_SNAPPY.toString(),
            Const.CompressType.COMPRESS_TYPE_ZIP.toString(), Const.CompressType.COMPRESS_TYPE_LZMA.toString(),Const.CompressType.COMPRESS_TYPE_LZ4.toString(),Const.CompressType.COMPRESS_TYPE_ZSTD.toString(),Const.CompressType.COMPRESS_TYPE_BROTLI.toString()));
    public static final List<Const.CompressType> compressTypeEnum =Collections.unmodifiableList(Arrays.asList(Const.CompressType.COMPRESS_TYPE_GZ, Const.CompressType.COMPRESS_TYPE_LZO,
            Const.CompressType.COMPRESS_TYPE_BZ2, Const.CompressType.COMPRESS_TYPE_SNAPPY, Const.CompressType.COMPRESS_TYPE_ZIP, Const.CompressType.COMPRESS_TYPE_LZMA,Const.CompressType.COMPRESS_TYPE_LZ4,Const.CompressType.COMPRESS_TYPE_ZSTD,Const.CompressType.COMPRESS_TYPE_BROTLI));

    public static String parseFileFormat(String path, List<String> suffix) {
        String filePath = null;
        if (suffix == null) {
            suffix = new ArrayList<String>();
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
}
