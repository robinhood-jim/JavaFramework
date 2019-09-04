package com.robin.core.compress.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import org.anarres.lzo.*;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:CompressEncoder</p>
 * <p>
 * <p>Copyright: Copyright (c) 2016 create at 2016年12月27日</p>
 * <p>
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class CompressEncoder {

        /**
         *wrap OutputStream with specify compress Format  (Zip file can only support One file)
         * @param path   file path
         * @param rawstream  raw outputstream
         * @return  wrap outputStream
         */
    public static OutputStream getOutputStreamByCompressType(String path,OutputStream rawstream) throws IOException{
        OutputStream outputStream=null;
        List<String> suffixList=new ArrayList<String>();
        String fileName= FileUtils.parseFileFormat(path,suffixList);
        Const.CompressType type=FileUtils.getFileCompressType(suffixList);
        if(!type.equals(Const.CompressType.COMPRESS_TYPE_NONE)){
            if(type.equals(Const.CompressType.COMPRESS_TYPE_GZ)){
                outputStream=new GZIPOutputStream(wrapOutputStream(rawstream));
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_BZ2)){
                outputStream=new BZip2CompressorOutputStream(wrapOutputStream(rawstream));
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_LZO)){
                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
                LzoCompressor compressor = LzoLibrary.getInstance().newCompressor(algorithm, null);
                outputStream = new LzoOutputStream(wrapOutputStream(rawstream), compressor);
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_SNAPPY)){
                outputStream=new SnappyOutputStream(wrapOutputStream(rawstream));
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_ZIP)){
                ZipOutputStream stream1=new ZipOutputStream(wrapOutputStream(rawstream));
                stream1.putNextEntry(new ZipEntry(fileName));
                outputStream=stream1;
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_LZMA)){
                outputStream=new LZMAOutputStream(wrapOutputStream(rawstream),new LZMA2Options(),false);
            }
        }else{
            //no compress
            outputStream=rawstream;
        }
        return outputStream;
    }
    private static OutputStream wrapOutputStream(OutputStream outputStream){
        OutputStream out=null;
        if(outputStream instanceof BufferedOutputStream){
            out=outputStream;
        }else{
            out=new BufferedOutputStream(outputStream);
        }
        return out;
    }
}
