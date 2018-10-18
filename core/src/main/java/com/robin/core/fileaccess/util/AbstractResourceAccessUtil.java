/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import net.jpountz.lz4.*;
import org.anarres.lzo.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.*;
import java.util.zip.*;

public abstract class AbstractResourceAccessUtil {
	public static String[] retrieveResource(String path){
		String[] ret=new String[2];
		ret[1]=getFileSuffix(path);
		ret[0]=getFilePrefix(path);
		return ret;
	}
	
	public static String getFileSuffix(String path){
		int pos=path.lastIndexOf(".");
		String suffix=path.substring(pos+1,path.length());
		if(suffix.contains(File.separator)){
			suffix="";
		}
		return suffix;
	}
	public static String getFilePrefix(String path){
		String prefix="";
		if(!path.startsWith(File.separator)){
			int pos=path.indexOf(":");
			prefix=path.substring(0,pos);
		}
		return prefix;
	}
	public static String getFileName(String name){
		int pos=name.lastIndexOf(".");
		int frompos=name.lastIndexOf(File.separator)+1;
		String filename=name.substring(frompos,pos);
		return filename;
	}	
	public static String getFilePath(String name){
		int pos=name.lastIndexOf(File.separator);
		return name.substring(0,pos);
	}
	protected BufferedReader getReaderBySuffix(String suffix,InputStream  in,String encode) throws IOException{
		return new BufferedReader(new InputStreamReader(getInputStreamBySuffix(suffix,in),encode));
	}
	protected InputStream getInputStreamBySuffix(String suffix,InputStream  in) throws IOException{
		InputStream instream= null;
		if(Const.SUFFIX_GZIP.equalsIgnoreCase(suffix)){
			instream=new GZIPInputStream(wrapInputStream(in));
		}else if(Const.SUFFIX_ZIP.equalsIgnoreCase(suffix)){
			instream=new ZipInputStream(wrapInputStream(in));
			((ZipInputStream)instream).getNextEntry();
		}else if(Const.SUFFIX_BZIP2.equalsIgnoreCase(suffix)){
			instream=new BZip2CompressorInputStream(wrapInputStream(in));
		}else if(Const.SUFFIX_LZO.equalsIgnoreCase(suffix)){
			LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
			LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(algorithm, null);
			instream = new LzoInputStream(wrapInputStream(in), decompressor);
		}else if(Const.SUFFIX_SNAPPY.equalsIgnoreCase(suffix)){
			instream=new SnappyInputStream(wrapInputStream(in));
		}else if(Const.SUFFIX_LZMA.equalsIgnoreCase(suffix)){
			instream=new LZMAInputStream(wrapInputStream(in));
		}else if(Const.SUFFIX_LZ4.equalsIgnoreCase(suffix)){
			LZ4Factory factory = LZ4Factory.fastestInstance();
			LZ4FastDecompressor decompressor=factory.fastDecompressor();
			instream=new LZ4BlockInputStream(wrapInputStream(in),decompressor);
		}
		else
			instream=in;
		return instream;
	}
	protected BufferedWriter getWriterBySuffix(String suffix,OutputStream out,String encode) throws IOException{
		return new BufferedWriter(new OutputStreamWriter(getOutputStreamBySuffix(suffix,out),encode));
	}
	private static OutputStream wrapOutputStream(OutputStream outputStream){
		OutputStream out=null;
		if(outputStream instanceof  BufferedOutputStream){
			out=outputStream;
		}else{
			out=new BufferedOutputStream(outputStream);
		}
		return out;
	}
	private static InputStream wrapInputStream(InputStream instream){
		InputStream in=null;
		if(instream instanceof  BufferedInputStream){
			in=instream;
		}else{
			in=new BufferedInputStream(instream);
		}
		return in;
	}
	protected OutputStream getOutputStreamBySuffix(String suffix,OutputStream out) throws IOException{
		OutputStream outputStream=null;
		if(Const.SUFFIX_GZIP.equalsIgnoreCase(suffix)){
			outputStream=new GZIPOutputStream(wrapOutputStream(out));
		}else if(Const.SUFFIX_ZIP.equalsIgnoreCase(suffix)){
			outputStream=new ZipOutputStream(wrapOutputStream(out));
			((ZipOutputStream)outputStream).putNextEntry(new ZipEntry("result"));
		}else if(Const.SUFFIX_BZIP2.equalsIgnoreCase(suffix)){
			outputStream=new BZip2CompressorOutputStream(wrapOutputStream(out));
		}else if(Const.SUFFIX_SNAPPY.equalsIgnoreCase(suffix)){
			outputStream=new SnappyOutputStream(wrapOutputStream(out));
		}else if(Const.SUFFIX_LZO.equalsIgnoreCase(suffix)){
			LzoAlgorithm algorithm=LzoAlgorithm.LZO1X;
			LzoCompressor compressor= LzoLibrary.getInstance().newCompressor(algorithm,null);
			outputStream=new LzoOutputStream(wrapOutputStream(out),compressor);
		}else if(Const.SUFFIX_LZMA.equalsIgnoreCase(suffix)){
			outputStream=new LZMAOutputStream(wrapOutputStream(out),new LZMA2Options(),-1);
		}else if(Const.SUFFIX_LZ4.equalsIgnoreCase(suffix)){
			LZ4Factory factory = LZ4Factory.fastestInstance();
			LZ4Compressor compressor= factory.fastCompressor();
			outputStream=new LZ4BlockOutputStream(wrapOutputStream(out),8192,compressor);
		}
		else
			outputStream=out;
		return outputStream;
	}
	
	public abstract BufferedReader getInResourceByReader(DataCollectionMeta meta) throws Exception;
	public abstract BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception;
	public abstract OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception;
	public abstract InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception;
}
