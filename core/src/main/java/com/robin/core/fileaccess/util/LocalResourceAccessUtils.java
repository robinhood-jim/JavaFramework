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
		reader= getReaderByPath(meta.getPath(), FileUtils.openInputStream(file), meta.getEncode());
		return reader;
	}
	
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception{
		BufferedWriter writer=null;
		File file=new File(meta.getPath());
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		writer= getWriterByPath(meta.getPath(), FileUtils.openOutputStream(file), meta.getEncode());
		return writer;
	}
	public OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception{
		File file=new File(meta.getPath());
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		String suffix=getFileSuffix(meta.getPath());
		return getOutputStreamByPath(suffix,FileUtils.openOutputStream(file));
	}
	public InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception{
		File file=new File(meta.getPath());
		if(!file.exists()){
			throw new IOException("file "+meta.getPath()+" not exist!");
		}
		String suffix=getFileSuffix(meta.getPath());
		return getInputStreamByPath(suffix,FileUtils.openInputStream(file));

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
