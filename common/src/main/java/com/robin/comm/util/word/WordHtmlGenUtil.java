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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;

public class WordHtmlGenUtil {
	public static void GeneratePdfFromSacure(String url,String fontPath,OutputStream out,String inputEncode) {
		try{
//			DOMParser parser=new DOMParser();
//			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "utf-8");
			Tidy tidy=new Tidy();
			tidy.setXHTML(true);
			tidy.setInputEncoding(inputEncode);
			tidy.setOutputEncoding("UTF-8");
			ByteArrayOutputStream tidyOut=new ByteArrayOutputStream();
			
			URL urlconn=new URL(url);
			URLConnection con=urlconn.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(6000);
			con.setReadTimeout(30000);
			//parser.parse(new InputSource(con.getInputStream()));
			//Document doc=parser.getDocument();
			tidy.parse(con.getInputStream(), tidyOut);
			InputStream tidyIn=new ByteArrayInputStream(tidyOut.toByteArray());
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
			dbf.setFeature("http://xml.org/sax/features/validation", Boolean.FALSE);
			dbf.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc=db.parse(tidyIn);
			Element style=doc.createElement("style");
			style.setTextContent("body {font-family:SimHei;font-size:12px}");
			Element root=doc.getDocumentElement();
			root.getElementsByTagName("head").item(0).appendChild(style);
			
			
			ITextRenderer renderer=new ITextRenderer();
			renderer.setDocument(doc,url);
			ITextFontResolver resolver=renderer.getFontResolver();
			resolver.addFont(fontPath, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			renderer.layout();
			renderer.createPDF(out);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void GenerateWordFromUrl(String url,OutputStream out){
		try{
			com.lowagie.text.Document document=new com.lowagie.text.Document(PageSize.A4);
			RtfWriter2.getInstance(document, out);  

			document.open();  
			Paragraph context = new Paragraph();  
			URL urlconn=new URL(url);
			URLConnection con=urlconn.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(6000);
			con.setReadTimeout(30000);
			StyleSheet ss=new StyleSheet();
			ss.loadStyle("body", "font-family", "SimSun");
			//HTMLWorker htmlWorker = new HTMLWorker(document);
			List htmlList=HTMLWorker.parseToList(new InputStreamReader(con.getInputStream(),"UTF-8"), ss);
			for (int i = 0; i < htmlList.size(); i++) {  
				com.lowagie.text.Element e = (com.lowagie.text.Element) htmlList  
       	             .get(i);  
				context.add(e);  
			}  
			
			
			document.add(context);  
//			htmlWorker.parse(new InputStreamReader(con.getInputStream(),"UTF-8"));
			document.close();  
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
