package com.robin.core.compress.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import net.jpountz.lz4.LZ4FrameInputStream;
import org.anarres.lzo.*;
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;


public class CompressDecoder {
    private CompressDecoder(){

    }
    public static InputStream getInputStreamByCompressType(String path, InputStream rawstream) throws IOException{
        InputStream inputStream;
        List<String> suffixList=new ArrayList<>();
        FileUtils.parseFileFormat(path,suffixList);
        Const.CompressType type=FileUtils.getFileCompressType(suffixList);
        switch (type){
            case COMPRESS_TYPE_GZ:
                inputStream=new GZIPInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_BZ2:
                inputStream=new BZip2CompressorInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_LZO:
                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
                LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(algorithm, null);
                inputStream = new LzoInputStream(wrapInputStream(rawstream), decompressor);
                break;
            case COMPRESS_TYPE_SNAPPY:
                inputStream=new SnappyInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_ZIP:
                ZipInputStream stream1=new ZipInputStream(wrapInputStream(rawstream));
                stream1.getNextEntry();
                inputStream=stream1;
                break;
            case COMPRESS_TYPE_LZ4:
                inputStream=new LZ4FrameInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_LZMA:
                inputStream=new LZMAInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_ZSTD:
                inputStream=new ZstdCompressorInputStream(wrapInputStream(rawstream));
                break;
            case COMPRESS_TYPE_BROTLI:
                inputStream=new BrotliCompressorInputStream(wrapInputStream(rawstream));
                break;
            default:
                inputStream=wrapInputStream(rawstream);

        }
        return inputStream;
    }
    private static InputStream wrapInputStream(InputStream instream){
        InputStream in=null;
        if(instream instanceof BufferedInputStream){
            in=instream;
        }else{
            in=new BufferedInputStream(instream);
        }
        return in;
    }
}
