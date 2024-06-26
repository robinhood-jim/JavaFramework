package com.robin.core.fileaccess.fs;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.URI;

public class LocalFileSystemAccessor extends AbstractFileSystemAccessor {
	public LocalFileSystemAccessor(){
		this.identifier= Const.FILESYSTEM.LOCAL.getValue();
	}


	@Override
    public Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException{
		BufferedReader reader;
		InputStream stream;

		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("input file "+resourcePath+" does not exist!");
		}
		stream=FileUtils.openInputStream(file);
		reader= getReaderByPath(getProcessPath(resourcePath), stream, meta.getEncode());
		return Pair.of(reader,stream);
	}
	
	@Override
    public Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException{
		BufferedWriter writer;
		OutputStream outputStream;
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		outputStream=FileUtils.openOutputStream(file);
		writer= getWriterByPath(getProcessPath(resourcePath), outputStream, meta.getEncode());
		return Pair.of(writer,outputStream);
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
