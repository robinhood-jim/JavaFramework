package com.robin.core.fileaccess.util;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;

public class LocalResourceAccessUtil extends AbstractResourceAccessUtil {
	@Override
    public BufferedReader getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException{
		BufferedReader reader;

		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("input file "+resourcePath+" does not exist!");
		}
		reader= getReaderByPath(getProcessPath(resourcePath), FileUtils.openInputStream(file), meta.getEncode());
		return reader;
	}
	
	@Override
    public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException{
		BufferedWriter writer;
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		writer= getWriterByPath(getProcessPath(resourcePath), FileUtils.openOutputStream(file), meta.getEncode());
		return writer;
	}
	@Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException{
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		return getOutputStreamByPath(getProcessPath(resourcePath),FileUtils.openOutputStream(file));
	}
	@Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException{
		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return getInputStreamByPath(getProcessPath(resourcePath),FileUtils.openInputStream(file));

	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		return FileUtils.openOutputStream(file);
	}

	@Override
	public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return FileUtils.openInputStream(file);
	}

	@Override
	public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		return file.exists();
	}

	@Override
	public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return FileUtils.sizeOf(file);
	}
	private static  String getProcessPath(String url){
		try {
			URI uri = new URI(url);
			return uri.getPath();
		}catch (Exception ex){

		}
		return url;
	}
}
