package com.robin.core.compress.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import org.anarres.lzo.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:Compress File Decoder</p>
 * <p>
 * <p>Copyright: Copyright (c) 2016 create at 2016年12月27日</p>
 * <p>
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class CompressDecoder {
    public static InputStream getInputStreamByCompressType(String path, InputStream rawstream) throws IOException{
        InputStream inputStream=null;
        List<String> suffixList=new ArrayList<String>();
        String fileName= FileUtils.parseFileFormat(path,suffixList);
        Const.CompressType type=FileUtils.getFileCompressType(suffixList);
        if(!type.equals(Const.CompressType.COMPRESS_TYPE_NONE)){
            if(type.equals(Const.CompressType.COMPRESS_TYPE_GZ)){
                inputStream=new GZIPInputStream(rawstream);
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_BZ2)){
                inputStream=new BZip2CompressorInputStream(rawstream);
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_LZO)){
                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
                LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(algorithm, null);
                inputStream = new LzoInputStream(rawstream, decompressor);
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_SNAPPY)){
                inputStream=new SnappyInputStream(rawstream);
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_ZIP)){
                ZipInputStream stream1=new ZipInputStream(rawstream);
                stream1.getNextEntry();
                inputStream=stream1;
            }else if(type.equals(Const.CompressType.COMPRESS_TYPE_LZMA)){
                inputStream=new LZMAInputStream(rawstream);
            }
        }else{
            //no compress
            inputStream=rawstream;
        }
        return inputStream;
    }
}
