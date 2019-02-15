package com.robin.core.fileaccess.util;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.io.FileUtils;

import java.io.*;

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
		File file=new File(meta.getPath());
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		String suffix=getFileSuffix(meta.getPath());
		return getOutputStreamBySuffix(suffix,FileUtils.openOutputStream(file));
	}
	public InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception{
		File file=new File(meta.getPath());
		if(!file.exists()){
			throw new IOException("file "+meta.getPath()+" not exist!");
		}
		String suffix=getFileSuffix(meta.getPath());
		return getInputStreamBySuffix(suffix,FileUtils.openInputStream(file));

	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta) throws Exception {
		File file=new File(meta.getPath());
		if(!file.exists()){
			FileUtils.forceDelete(file);
		}
		return FileUtils.openOutputStream(file);
	}
}
