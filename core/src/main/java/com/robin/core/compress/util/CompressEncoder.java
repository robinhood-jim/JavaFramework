package com.robin.core.compress.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.anarres.lzo.*;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
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


public class CompressEncoder {
    private CompressEncoder(){

    }

        /**
         *wrap OutputStream with specify compress Format  (Zip file can only support One file)
         * @param path   file path
         * @param rawstream  raw outputstream
         * @return  wrap outputStream
         */
    public static OutputStream getOutputStreamByCompressType(String path,OutputStream rawstream) throws IOException{
        OutputStream outputStream;
        List<String> suffixList=new ArrayList<>();
        String fileName= FileUtils.parseFileFormat(path,suffixList);
        Const.CompressType type=FileUtils.getFileCompressType(suffixList);
        switch (type){
            case COMPRESS_TYPE_GZ:
                outputStream=new GZIPOutputStream(wrapOutputStream(rawstream));
                break;
            case COMPRESS_TYPE_BZ2:
                outputStream=new BZip2CompressorOutputStream(wrapOutputStream(rawstream));
                break;
            case COMPRESS_TYPE_LZO:
                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
                LzoCompressor compressor = LzoLibrary.getInstance().newCompressor(algorithm, null);
                outputStream = new LzoOutputStream(wrapOutputStream(rawstream), compressor);
                break;
            case COMPRESS_TYPE_SNAPPY:
                outputStream=new SnappyOutputStream(wrapOutputStream(rawstream));
                break;
            case COMPRESS_TYPE_ZIP:
                ZipOutputStream stream1=new ZipOutputStream(wrapOutputStream(rawstream));
                stream1.putNextEntry(new ZipEntry(fileName));
                outputStream=stream1;
                break;
            case COMPRESS_TYPE_LZ4:
                outputStream=new LZ4FrameOutputStream(wrapOutputStream(rawstream));
                break;
            case COMPRESS_TYPE_LZMA:
                outputStream=new LZMAOutputStream(wrapOutputStream(rawstream),new LZMA2Options(),false);
                break;
            case COMPRESS_TYPE_ZSTD:
                outputStream=new ZstdCompressorOutputStream(wrapOutputStream(rawstream));
                break;
            case COMPRESS_TYPE_BROTLI:
                throw new IOException("BROTLI encode not support now!");
            default:
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
