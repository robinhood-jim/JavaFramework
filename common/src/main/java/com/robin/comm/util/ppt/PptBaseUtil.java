package com.robin.comm.util.ppt;


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.hslf.model.AutoShape;
import org.apache.poi.hslf.model.Fill;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.Line;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.ShapeTypes;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.SlideMaster;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TableCell;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;

import com.robin.comm.util.xls.TableHeaderColumn;
import com.robin.comm.util.xls.TableHeaderProp;


public class PptBaseUtil {
	public static void adjustHeaderFooter(SlideShow show,PptHeaderFooterSection section){
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
	public static void insertTable(Slide slide,PptTableDef def,List<Map<String, String>> resultList){
		TableHeaderProp prop=def.getHeaderProp();
		int width=prop.getContainrow();
		int rows=prop.getTotalCol();
		Table celltab=new Table(width,rows);

		List<TableHeaderColumn> list=prop.getHeaderColumnList().get(0);
		for(int i=0;i<list.size();i++){
			TableHeaderColumn col=list.get(i);
			TableCell cell=celltab.getCell(0, i);
			cell.setText(col.getColumnName());
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
		celltab.setAnchor(new Rectangle(200,200,300,200));
	}
	public static void insertImage(SlideShow show,Slide slide,PptImageDef def) throws IOException, URISyntaxException{
		BufferedImage image=ImageIO.read(new File(def.getPicUrl()));
		String path=def.getPicUrl();
		int pic_type=-1;
		if(path.indexOf(".png") != -1){  
            pic_type = Picture.PNG;  
        }else{  
            pic_type = Picture.JPEG;  
        }  
		int newindex=show.addPicture(new File(def.getPicUrl()), pic_type);
		Picture pic=new Picture(newindex);
		pic.setAnchor(new Rectangle(def.getPosx(),def.getPosy(),image.getWidth(),image.getHeight()));
		slide.addShape(pic);
	}
	public static void insertText(Slide slide,PptPragraph pragraph){
		AutoShape _autoShape = new AutoShape(ShapeTypes.Rectangle);
        TextRun _autoText = _autoShape.createTextRun();  
        _autoText.setRawText(pragraph.getContext());  
        _autoShape.setAnchor(new Rectangle(pragraph.getPosx(),pragraph.getPosy(),pragraph.getPosx()+pragraph.getWidth(),pragraph.getPosy()+pragraph.getHeight()));  
        if(pragraph.getFontColor()!=null)
        	_autoShape.setFillColor(new Color(pragraph.getFontColor()[0],pragraph.getFontColor()[1],pragraph.getFontColor()[2]));  
        else
        	_autoShape.setFillColor(new Color(255,255,255));
        _autoShape.setLineWidth(5.0);  
        _autoShape.setLineStyle(Line.LINE_DOUBLE); 
        slide.addShape(_autoShape);
	}
	public static void setBackground(SlideShow show,PptConfig config) throws IOException{
		SlideMaster master = show.getSlidesMasters()[0];

        Fill fill = master.getBackground().getFill();
        String path=config.getBackgroundPic();
		int pic_type=-1;
		if(path.indexOf(".png") != -1){  
            pic_type = Picture.PNG;  
        }else{  
            pic_type = Picture.JPEG;  
        }
        int idx = show.addPicture(new File(path), pic_type);
        fill.setFillType(Fill.FILL_PICTURE);
        fill.setPictureData(idx);
	}

}
