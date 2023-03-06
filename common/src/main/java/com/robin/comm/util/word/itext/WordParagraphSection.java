/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.comm.util.word.itext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;

public class WordParagraphSection {
	private String paragraphText;
	private String type;
	private String fontName;
	private int fontSize=10;
	private int fontStyle=Font.NORMAL;
	private int alignment=Paragraph.ALIGN_LEFT;
	private WordTableDef tableDef;
	private WordTableHeaderDef headerDef;
	private WordImageSection imgSection;
	private List<Map<String, String>> resultList=new ArrayList<Map<String,String>>();
	
	public static final String TYPE_PARAGRAPH="PARAGRAPH";
	public static final String TYPE_IMAGE="IMG";
	public static final String TYPE_TABLE="TAB";
	public static final String TYPE_HEADER="HEADER";
	public static final String TYPE_TITLE="TITLE";
	
	
	public String getParagraphText() {
		return paragraphText;
	}
	public void setParagraphText(String paragraphText) {
		this.paragraphText = paragraphText;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public WordTableDef getTableDef() {
		return tableDef;
	}
	public void setTableDef(WordTableDef tableDef) {
		this.tableDef = tableDef;
	}
	public WordTableHeaderDef getHeaderDef() {
		return headerDef;
	}
	public void setHeaderDef(WordTableHeaderDef headerDef) {
		this.headerDef = headerDef;
	}
	public WordImageSection getImgSection() {
		return imgSection;
	}
	public void setImgSection(WordImageSection imgSection) {
		this.imgSection = imgSection;
	}
	public int getAlignment() {
		return alignment;
	}
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}
	public List<Map<String, String>> getResultList() {
		return resultList;
	}
	public void setResultList(List<Map<String, String>> resultList) {
		this.resultList = resultList;
	}
	

}
