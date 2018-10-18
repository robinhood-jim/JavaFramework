package com.robin.comm.ftp;

import java.util.Calendar;

public class FileInfo {
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	private String path;
	private String name;
	private long size;
	private Calendar date;
	private String tag="0";
	private String interfaceName;
	private String localPath;
	public FileInfo(){
		
	}
	public FileInfo(String path,String name,Long size,Calendar date,String interfaceName){
		this.path=path;
		this.name=name;
		this.size=size;
		this.date=date;
		this.interfaceName=interfaceName;
	}
	public FileInfo(String path,String name,String interfaceName,String size,String id){
		this.path=path;
		this.name=name;
		this.interfaceName=interfaceName;
		this.size=Long.parseLong(size);
		this.id=id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public Calendar getDate() {
		return date;
	}
	public void setDate(Calendar date) {
		this.date = date;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getLocalPath() {
		return localPath;
	}
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	

}
