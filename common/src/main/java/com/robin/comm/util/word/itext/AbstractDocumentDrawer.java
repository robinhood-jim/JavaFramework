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

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;

public abstract class AbstractDocumentDrawer {
	protected Document document;
	protected WordConfig cfg;
	protected Font headerfont;
	private Logger logger=LoggerFactory.getLogger(getClass()); 
	
	public void drawHeader(WordConfig config){
		WordHeaderFooterSection section=config.getHeaderFooter();
		try{
		if(section!=null){
			Paragraph headerParagraph=new Paragraph();
			Paragraph footerPara=new Paragraph();
			if(section.getHeaderpicname()!=null && !"".equals(section.getHeaderpicname().trim())){
				Image headerImage = Image.getInstance(section.getHeaderpicname());
				headerParagraph.add(headerImage);
			}
			if(section.getHeaderString()!=null && !"".equals(section.getHeaderString())){
				Paragraph para=new Paragraph(section.getHeaderString());
				headerParagraph.add(para);
			}
			if(section.getFooterpicName()!=null && !"".equals(section.getFooterpicName().trim())){
				Image headerImage = Image.getInstance(section.getFooterpicName());
				footerPara.add(headerImage);
			}
			if(section.getFooterString()!=null && !"".equals(section.getFooterString())){
				Paragraph para=new Paragraph(section.getFooterString());
				footerPara.add(para);
			}
			HeaderFooter headerfooter=new HeaderFooter(headerParagraph,true);
			headerfooter.setAlignment(Paragraph.ALIGN_CENTER);
			HeaderFooter footer=new HeaderFooter(footerPara,true);
			footer.setAlignment(Paragraph.ALIGN_RIGHT);
			document.setHeader(headerfooter);
			document.setFooter(footer);
		}
		}catch (Exception e) {
			logger.error("Encounter Error ",e);
		}
	}
	public void prepareStyle(WordTableDef tabDef,WordTableHeaderDef headerDef){
		int totalCount=0;
		if(headerDef!=null) {
			totalCount=headerDef.getHeaderNums()+tabDef.getRecordSize();
		} else {
			totalCount=tabDef.getRecordSize()+1;
		}
		try{
		Table table=new Table(tabDef.getDbColumnList().size(),totalCount);
		table.setWidths(tabDef.getColWidthPercent());
		table.setWidth(tabDef.getTableWidth());
		table.setAlignment(tabDef.getTableAligement());
		table.setAutoFillEmptyCells(true);
	    table.setBorderWidth(tabDef.getBorderWidth());
	    table.setBorderColor(new Color(0, 125, 255));
	    headerfont=WordBaseUtil.createFont(tabDef.getBaseFont(), cfg.getFontSize(), Font.BOLD);
		}catch (Exception e) {
			logger.error("Encounter Error ",e);
		}
	}
	public abstract void drawContent();
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public WordConfig getCfg() {
		return cfg;
	}

	public void setCfg(WordConfig cfg) {
		this.cfg = cfg;
	}
	
}
