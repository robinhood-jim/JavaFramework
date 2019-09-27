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
import com.robin.core.compress.util.CompressDecoder;
import com.robin.core.compress.util.CompressEncoder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import net.jpountz.lz4.*;
import org.anarres.lzo.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;
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
		String suffix=path.substring(pos+1);
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
	protected BufferedReader getReaderByPath(String path, InputStream  in, String encode) throws IOException{
		return new BufferedReader(new InputStreamReader(getInputStreamByPath(path,in),encode));
	}
	protected InputStream getInputStreamByPath(String path, InputStream  in) throws IOException{
		return CompressDecoder.getInputStreamByCompressType(path,in);
	}
	protected BufferedWriter getWriterByPath(String path, OutputStream out, String encode) throws IOException{
		return new BufferedWriter(new OutputStreamWriter(getOutputStreamByPath(path,out),encode));
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
	protected OutputStream getOutputStreamByPath(String path, OutputStream out) throws IOException{
		return CompressEncoder.getOutputStreamByCompressType(path,out);
	}
	
	public abstract BufferedReader getInResourceByReader(DataCollectionMeta meta) throws Exception;
	public abstract BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception;
	public abstract OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception;
	public abstract OutputStream getRawOutputStream(DataCollectionMeta meta) throws Exception;
	public abstract InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception;
}
