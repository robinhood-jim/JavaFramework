package com.robin.core.compress.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.compress.util</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年02月14日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class CompressUtils {
    public static void zip(File srcDir, File zipFile) throws IOException {
        if (!srcDir.exists()) {
            return;
        }
        if (!zipFile.getParentFile().exists()) {
            zipFile.getParentFile().mkdirs();
        }

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            pack(out, srcDir, "");
        } finally {
            IOUtils.closeQuietly(out);
        }
    }


    public static void jar(File srcDir, File jarFile, Manifest manifest)
            throws IOException {
        if (!srcDir.exists()) {
            return;
        }
        if (!jarFile.getParentFile().exists()) {
            jarFile.getParentFile().mkdirs();
        }

        JarOutputStream out = null;
        try {
            if (manifest == null) {
                out = new JarOutputStream(new FileOutputStream(jarFile));
            } else {
                out = new JarOutputStream(new FileOutputStream(jarFile),
                        manifest);
            }
            pack(out, srcDir, "");
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private static void pack(ZipOutputStream out, File src, String base)
            throws IOException {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            out.putNextEntry(new ZipEntry(base + "/"));
            base = StringUtils.isBlank(base) ? "" : base + "/";
            for (File f : files) {
                pack(out, f, base + f.getName());
            }
        } else {
            out.putNextEntry(new JarEntry(base));

            FileInputStream in = null;
            try {
                in = new FileInputStream(src);
                IOUtils.copy(in, out);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }


    public static void unzip(File srcFile, File destDir) throws IOException {
        ZipFile zipFile = new ZipFile(srcFile);
        try {
            Enumeration<?> zipEnum = zipFile.entries();
            while (zipEnum.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipEnum.nextElement();
                if (entry.isDirectory()) {
                    new File(destDir, entry.getName()).mkdirs();
                    continue;
                }
                File file = new File(destDir, entry.getName());
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                InputStream input = null;
                OutputStream output = null;
                try {
                    input = zipFile.getInputStream(entry);
                    output = new FileOutputStream(file);
                    IOUtils.copy(input, output);
                } finally {
                    IOUtils.closeQuietly(output);
                    IOUtils.closeQuietly(input);
                }
            }
        } finally {
            zipFile.close();
        }
    }

    public static void extractTGzip(File tgzFiles,File destDir) throws IOException{
        if(!tgzFiles.exists()){
            throw new IOException("Source tgz file missing!");
        }
        TarArchiveInputStream in=null;
        try {
            in=new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tgzFiles))));

            ArchiveEntry entry = in.getNextEntry();
            while (entry != null) {
                File destPath = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    checkPathExist(destPath);
                }
                else {
                    destPath.getParentFile().mkdir();
                    destPath.createNewFile();
                    IOUtils.copy(in, new FileOutputStream(destPath));
                }
                entry=in.getNextEntry();
            }
        }catch (IOException ex){
            throw ex;
        }finally {
            if(in!=null){
                in.close();
            }
        }

    }
    public static void checkPathExist(File path){
        File tmpPath=path;
        List<File> list=new ArrayList<File>();
        while(tmpPath!=null && tmpPath.getParentFile()!=tmpPath){
            if(!tmpPath.exists()){
                list.add(tmpPath);
            }
            tmpPath=tmpPath.getParentFile();
        }
        for(int i=list.size()-1;i>0;i--){
            list.get(i).mkdir();
        }
    }
}
