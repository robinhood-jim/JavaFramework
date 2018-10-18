package com.robin.comm.util.word;

import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Font;

public class WordConfig {
	private String fontName="STSong-Light";
	private String fontPath="";
	private boolean isuseDefaultFont=true;
	private int fontStyle=Font.NORMAL;
	private int fontSize=10;
	private WordHeaderFooterSection headerFooter;
	private List<WordParagraphSection> paragraphList=new ArrayList<WordParagraphSection>();
	
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public String getFontPath() {
		return fontPath;
	}
	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}
	public boolean isIsuseDefaultFont() {
		return isuseDefaultFont;
	}
	public void setIsuseDefaultFont(boolean isuseDefaultFont) {
		this.isuseDefaultFont = isuseDefaultFont;
	}
	public int getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public WordHeaderFooterSection getHeaderFooter() {
		return headerFooter;
	}
	public void setHeaderFooter(WordHeaderFooterSection headerFooter) {
		this.headerFooter = headerFooter;
	}
	public List<WordParagraphSection> getParagraphList() {
		return paragraphList;
	}
	public void setParagraphList(List<WordParagraphSection> paragraphList) {
		this.paragraphList = paragraphList;
	}
	
	

}
