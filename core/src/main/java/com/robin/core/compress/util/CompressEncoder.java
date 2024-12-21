package com.robin.core.compress.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.anarres.lzo.LzoAlgorithm;
import org.anarres.lzo.LzoCompressor;
import org.anarres.lzo.LzoLibrary;
import org.anarres.lzo.LzoOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class CompressEncoder {
    private CompressEncoder() {

    }

    /**
     * wrap OutputStream with specify compress Format  (Zip file can only support One file)
     *
     * @param path      file path
     * @param rawstream raw outputstream
     * @return wrap outputStream
     */
    public static OutputStream getOutputStreamByCompressType(String path, OutputStream rawstream) throws IOException {
        OutputStream outputStream;
        FileUtils.FileContent content = FileUtils.parseFile(path);
        Const.CompressType type = content.getCompressType();
        switch (type) {
            case COMPRESS_TYPE_GZ:
                outputStream = new GZIPOutputStream(rawstream);
                break;
            case COMPRESS_TYPE_BZ2:
                outputStream = new BZip2CompressorOutputStream(rawstream);
                break;
            case COMPRESS_TYPE_LZO:
                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
                LzoCompressor compressor = LzoLibrary.getInstance().newCompressor(algorithm, null);
                outputStream = new LzoOutputStream(rawstream, compressor);
                break;
            case COMPRESS_TYPE_SNAPPY:
                outputStream = new SnappyOutputStream(rawstream);
                break;
            case COMPRESS_TYPE_ZIP:
                ZipOutputStream stream1 = new ZipOutputStream(rawstream);
                stream1.putNextEntry(new ZipEntry(content.getFileName() + "." + content.getFileFormat()));
                outputStream = stream1;
                break;
            case COMPRESS_TYPE_LZ4:
                outputStream = new LZ4FrameOutputStream(rawstream);
                break;
            case COMPRESS_TYPE_LZMA:
                outputStream = new XZOutputStream(rawstream, new LZMA2Options());
                break;
            case COMPRESS_TYPE_ZSTD:
                outputStream = new ZstdCompressorOutputStream(rawstream);
                break;
            case COMPRESS_TYPE_BROTLI:
                throw new IOException("BROTLI encode not support now!");
            case COMPRESS_TYPE_XZ:
                outputStream = new XZCompressorOutputStream(rawstream);
                break;
            default:
                outputStream = rawstream;
        }

        return outputStream;
    }
}
