package com.robin.core.fileaccess.util;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.io.FileUtils;

import java.io.*;

public class LocalResourceAccessUtils extends AbstractResourceAccessUtil {
	@Override
    public BufferedReader getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws Exception{
		BufferedReader reader=null;
		File file=new File(resourcePath);
		if(!file.exists()){
			throw new IOException("input file "+resourcePath+" does not exist!");
		}
		reader= getReaderByPath(resourcePath, FileUtils.openInputStream(file), meta.getEncode());
		return reader;
	}
	
	@Override
    public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws Exception{
		BufferedWriter writer=null;
		File file=new File(resourcePath);
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		writer= getWriterByPath(resourcePath, FileUtils.openOutputStream(file), meta.getEncode());
		return writer;
	}
	@Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws Exception{
		File file=new File(resourcePath);
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		return getOutputStreamByPath(resourcePath,FileUtils.openOutputStream(file));
	}
	@Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws Exception{
		File file=new File(resourcePath);
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return getInputStreamByPath(resourcePath,FileUtils.openInputStream(file));

	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws Exception {
		File file=new File(resourcePath);
		if(!file.exists()){
			FileUtils.forceDelete(file);
		}
		return FileUtils.openOutputStream(file);
	}
}
