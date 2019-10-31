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
package com.robin.comm.util.word;

import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WordGenerater {
	public static void Write(Document document,WordConfig config){
		try{
			
			BaseFont font=BaseFont.createFont(config.getFontName(),BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			List<WordParagraphSection> sectionList=config.getParagraphList();
			WordHeaderFooterSection section=config.getHeaderFooter();
			if(section!=null) {
                WordBaseUtil.insertHeader(document, section);
            }
			for(WordParagraphSection para:sectionList){
				if(para.getType().equalsIgnoreCase(WordParagraphSection.TYPE_PARAGRAPH)) {
                    WordBaseUtil.insertContext(document, font, para.getParagraphText(), para.getFontSize(), para.getFontStyle(), para.getAlignment());
                } else if(para.getType().equalsIgnoreCase(WordParagraphSection.TYPE_TITLE)) {
                    WordBaseUtil.insertTitle(document, font, para.getParagraphText(), para.getFontSize(), para.getFontStyle(), para.getAlignment());
                } else if(para.getType().equalsIgnoreCase(WordParagraphSection.TYPE_IMAGE)) {
                    WordBaseUtil.insertImg(document, para.getImgSection());
                } else if(para.getType().equalsIgnoreCase(WordParagraphSection.TYPE_TABLE)) {
                    WordBaseUtil.insertTable(document, config, para.getTableDef(), para.getHeaderDef(), para.getResultList());
                }
				
			}
		}catch (Exception e) {
			log.error("",e);
		}
	}
	public static void Write(AbstractDocumentDrawer drawer){
		drawer.drawContent();
	}
}
