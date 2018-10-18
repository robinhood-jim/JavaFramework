package com.robin.comm.util.ppt;

import java.util.List;
import java.util.Map;

public class PptSection {
	
	private PptImageDef imageDef;
	private PptTableDef tableDef;
	
	private List<PptPragraph> paragrahList;
	private String type;
	private List<Map<String, String>> resultList;
	public static String TYPE_PARAGRAPH="PARAGRAPH";
	public static String TYPE_IMAGE="IMG";
	public static String TYPE_TABLE="TAB";
	public static String TYPE_TITLE="TITLE";
	
	
	public PptImageDef getImageDef() {
		return imageDef;
	}
	public void setImageDef(PptImageDef imageDef) {
		this.imageDef = imageDef;
	}
	public PptTableDef getTableDef() {
		return tableDef;
	}
	public void setTableDef(PptTableDef tableDef) {
		this.tableDef = tableDef;
	}
	public List<PptPragraph> getParagrahList() {
		return paragrahList;
	}
	public void setParagrahList(List<PptPragraph> paragrahList) {
		this.paragrahList = paragrahList;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Map<String, String>> getResultList() {
		return resultList;
	}
	public void setResultList(List<Map<String, String>> resultList) {
		this.resultList = resultList;
	}


}
