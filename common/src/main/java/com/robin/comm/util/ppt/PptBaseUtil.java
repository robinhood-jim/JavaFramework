package com.robin.comm.util.ppt;


import com.robin.comm.util.xls.TableConfigProp;
import com.robin.comm.util.xls.TableHeaderColumn;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTable;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;


public class PptBaseUtil {
	public static void adjustHeaderFooter(HSLFSlideShow show, PptHeaderFooterSection section){
		 HeadersFooters slideHeaders = show.getSlideHeadersFooters();
		 if(section.getHeaderStr()!=null && !"".equals(section.getHeaderStr()))
			 slideHeaders.setHeaderText(section.getHeaderStr());
		 
		 slideHeaders.setSlideNumberVisible(true);
		 slideHeaders.setDateTimeText("日期");
		 if(section.getFooterStr()!=null && !"".equals(section.getFooterStr()))
			 slideHeaders.setFootersText(section.getFooterStr());
		 HeadersFooters notesHeaders  = show.getNotesHeadersFooters();
		 if(section.getNodeFooterStr()!=null && !"".equals(section.getNodeFooterStr()))
			 notesHeaders.setFootersText(section.getNodeFooterStr());
		 if(section.getNodeHeaderStr()!=null && !"".equals(section.getNodeHeaderStr()))
			 notesHeaders.setHeaderText(section.getNodeHeaderStr());
	}
	public static void insertTable(HSLFSlide slide, PptTableDef def, List<Map<String, String>> resultList){
        TableConfigProp prop=def.getHeaderProp();
		int width=prop.getContainrow();
		int rows=prop.getTotalCol();
		/*HSLFTable celltab=new HSLFTable(width,rows,null);

		List<TableHeaderColumn> list=prop.getHeaderColumnList().get(0);
		for(int i=0;i<list.size();i++){
			TableHeaderColumn col=list.get(i);
			TableCell cell=celltab.getCell(0, i);
			TextRun run= cell.setText(col.getColumnName());
			RichTextRun rt = cell.getTextRun().getRichTextRuns()[0];
            rt.setFontSize(10);
            rt.setFontName("STSong-Light");
            
            cell.getFill().setForegroundColor(new Color(0, 51, 102));
            rt.setFontColor(Color.white);
            rt.setBold(true);
            rt.setFontSize(14);
            cell.setHorizontalAlignment(1);
             
            cell.setVerticalAlignment(1);
		}
		List<TableHeaderColumn> columnCodeList=prop.getHeaderColumnList().get(0);
		for(int i=0;i<resultList.size();i++){
			for(int j=0;j<columnCodeList.size();j++){
				TableCell cell=celltab.getCell(i+1, j);
				cell.setText(resultList.get(i).get(columnCodeList.get(j).getColumnCode()));
				 RichTextRun rt = cell.getTextRun().getRichTextRuns()[0];
	             rt.setFontSize(10);
	             rt.setFontName("STSong-Light");
	             rt.setBullet(false);
                 rt.setFontSize(12);
                 cell.setHorizontalAlignment(0);
			}
			
		}
		Line border=celltab.createBorder();
		border.setLineColor(Color.BLACK);
		border.setLineWidth(1.0D);
		celltab.setAllBorders(border);
		celltab.setColumnWidth(0, 60);
		celltab.moveTo(200, 200);
		slide.addShape(celltab);
		celltab.setAnchor(new Rectangle(200,200,300,200));*/
	}
	public static void insertImage(SlideShow show, Slide slide, PptImageDef def) throws IOException, URISyntaxException {
		BufferedImage image= ImageIO.read(new File(def.getPicUrl()));
		String path=def.getPicUrl();
		PictureType pic_type;
		if(path.indexOf(".png") != -1){  
            pic_type = PictureType.PNG;
        }else{  
            pic_type = PictureType.JPEG;
        }  
		PictureData data =show.addPicture(new File(def.getPicUrl()), pic_type);
		/*Picture pic=new Picture(newindex);
		pic.setAnchor(new Rectangle(def.getPosx(),def.getPosy(),image.getWidth(),image.getHeight()));
		slide.addShape(pic);*/
	}
	public static void insertText(Slide slide,PptPragraph pragraph){
		/*AutoShape _autoShape = new AutoShape(ShapeTypes.Rectangle);
        TextRun _autoText = _autoShape.createTextRun();  
        _autoText.setRawText(pragraph.getContext());  
        _autoShape.setAnchor(new Rectangle(pragraph.getPosx(),pragraph.getPosy(),pragraph.getPosx()+pragraph.getWidth(),pragraph.getPosy()+pragraph.getHeight()));  
        if(pragraph.getFontColor()!=null)
        	_autoShape.setFillColor(new Color(pragraph.getFontColor()[0],pragraph.getFontColor()[1],pragraph.getFontColor()[2]));  
        else
        	_autoShape.setFillColor(new Color(255,255,255));
        _autoShape.setLineWidth(5.0);  
        _autoShape.setLineStyle(Line.LINE_DOUBLE); 
        slide.addShape(_autoShape);*/
	}
	public static void setBackground(SlideShow show,PptConfig config) throws IOException{
		List<MasterSheet> master = show.getSlideMasters();
		XSLFSlideMaster slideMaster=(XSLFSlideMaster) master.get(0);

		XSLFSlideLayout layout=slideMaster.getLayout(SlideLayout.BLANK);
        String path=config.getBackgroundPic();
		PictureType pic_type;
		if(path.indexOf(".png") != -1){  
            pic_type = PictureType.PNG;
        }else{  
            pic_type = PictureType.JPEG;
        }
        PictureData data = show.addPicture(new File(path), pic_type);
		
	}

}
