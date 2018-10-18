package com.robin.comm.util.ppt;

import java.util.ArrayList;
import java.util.List;

public class PptConfig {
	private PptHeaderFooterSection headerFooter;
	private String backgroundPic;
	private List<List<PptSection>> sectionList=new ArrayList<List<PptSection>>();
	public PptHeaderFooterSection getHeaderFooter() {
		return headerFooter;
	}
	public void setHeaderFooter(PptHeaderFooterSection headerFooter) {
		this.headerFooter = headerFooter;
	}
	public List<List<PptSection>> getSectionList() {
		return sectionList;
	}
	public void setSectionList(List<List<PptSection>> sectionList) {
		this.sectionList = sectionList;
	}
	public String getBackgroundPic() {
		return backgroundPic;
	}
	public void setBackgroundPic(String backgroundPic) {
		this.backgroundPic = backgroundPic;
	}
	
	

}
