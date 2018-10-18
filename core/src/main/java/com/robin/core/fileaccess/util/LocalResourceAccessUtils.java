package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LocalResourceAccessUtils extends AbstractResourceAccessUtil {
	public BufferedReader getInResourceByReader(DataCollectionMeta meta) throws Exception{
		BufferedReader reader=null;
		File file=new File(meta.getPath());
		if(!file.exists()){
			throw new IOException("input file "+meta.getPath()+" does not exist!");
		}
		String suffix=getFileSuffix(meta.getPath());
		reader=getReaderBySuffix(suffix, FileUtils.openInputStream(file), meta.getEncode());
		return reader;
	}
	
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception{
		BufferedWriter writer=null;
		File file=new File(meta.getPath());
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		String suffix=getFileSuffix(meta.getPath());
		writer=getWriterBySuffix(suffix, FileUtils.openOutputStream(file), meta.getEncode());
		return writer;
	}
	public OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception{
		OutputStream stream=null;
		File file=new File(meta.getPath());
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		String suffix=getFileSuffix(meta.getPath());
		if(suffix.equalsIgnoreCase(Const.SUFFIX_GZIP)){
			stream=new GZIPOutputStream(FileUtils.openOutputStream(file));
		}else if(suffix.equalsIgnoreCase(Const.SUFFIX_ZIP)){
			stream=new ZipOutputStream(FileUtils.openOutputStream(file));
		}else
			stream=FileUtils.openOutputStream(file);
		return stream;
	}
	public InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception{
		InputStream stream=null;
		File file=new File(meta.getPath());
		if(!file.exists()){
			throw new IOException("file "+meta.getPath()+" not exist!");
		}
		String suffix=getFileSuffix(meta.getPath());
		if(suffix.equalsIgnoreCase(Const.SUFFIX_GZIP)){
			stream=new GZIPInputStream(FileUtils.openInputStream(file));
		}else if(suffix.equalsIgnoreCase(Const.SUFFIX_ZIP)){
			stream=new ZipInputStream(FileUtils.openInputStream(file));
		}else
			stream=FileUtils.openInputStream(file);
		return stream;
	}

	
	
	
	
}
