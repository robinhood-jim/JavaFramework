package com.robin.core.fileaccess.fs;

import com.robin.core.base.util.Const;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFileSystemAccessor extends AbstractFileSystemAccessor {
	private static LocalFileSystemAccessor accessor=new LocalFileSystemAccessor();
	private LocalFileSystemAccessor(){
		this.identifier= Const.FILESYSTEM.LOCAL.getValue();
	}
	public static LocalFileSystemAccessor getInstance(){
		return accessor;
	}


	@Override
    public Pair<BufferedReader,InputStream> getInResourceByReader(String resourcePath) throws IOException{
		BufferedReader reader;
		InputStream stream;

		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("input file "+resourcePath+" does not exist!");
		}
		stream=FileUtils.openInputStream(file);
		reader= getReaderByPath(getProcessPath(resourcePath), stream, colmeta.getEncode());
		return Pair.of(reader,stream);
	}
	
	@Override
    public Pair<BufferedWriter,OutputStream> getOutResourceByWriter(String resourcePath) throws IOException{
		BufferedWriter writer;
		OutputStream outputStream;
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		outputStream=FileUtils.openOutputStream(file);
		writer= getWriterByPath(getProcessPath(resourcePath), outputStream, colmeta.getEncode());
		return Pair.of(writer,outputStream);
	}
	@Override
    public OutputStream getOutResourceByStream(String resourcePath) throws IOException{
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		return getOutputStreamByPath(getProcessPath(resourcePath),FileUtils.openOutputStream(file));
	}
	@Override
    public InputStream getInResourceByStream(String resourcePath) throws IOException{
		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return getInputStreamByPath(getProcessPath(resourcePath),FileUtils.openInputStream(file));

	}

	@Override
	public OutputStream getRawOutputStream(String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		return FileUtils.openOutputStream(file);
	}

	@Override
	public InputStream getRawInputStream(String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		if(!file.exists()){
			throw new IOException("file "+resourcePath+" not exist!");
		}
		return FileUtils.openInputStream(file);
	}

	@Override
	public boolean exists(String resourcePath) throws IOException {
		File file=new File(getProcessPath(resourcePath));
		return file.exists();
	}

	@Override
	public long getInputStreamSize(String resourcePath) throws IOException {
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

	@Override
	public List<String> listPath(String inputPath) throws Exception {
		return FileUtils.listFiles(new File(getProcessPath(inputPath)), FileFilterUtils.fileFileFilter(), DirectoryFileFilter.DIRECTORY).stream().map(f->f.getPath()).collect(Collectors.toList());
	}

	@Override
	public boolean isDirectory(String inputPath) {
		File file=new File(getProcessPath(inputPath));
		return file.exists() && file.isDirectory();
	}
}
