package com.robin.comm.util.ppt;

import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;

import java.io.IOException;
import java.util.List;

public class PptGenerater {
	public static void GeneratePpt(HSLFSlideShow show, PptConfig config) throws IOException{
		List<List<PptSection>> sectionList=config.getSectionList();
		PptHeaderFooterSection headerFooter=config.getHeaderFooter();
		if(config.getBackgroundPic()!=null){
			PptBaseUtil.setBackground(show, config);
		}
		if(headerFooter!=null)
			PptBaseUtil.adjustHeaderFooter(show, headerFooter);
		for (int i=0;i<sectionList.size();i++) {
			Slide slide=show.createSlide();
			GenerateSlide(show,slide, sectionList.get(i));
		}
		
	}
	private static void GenerateSlide(SlideShow show,Slide slide,List<PptSection> sectionList){
		try{
		for (PptSection sec:sectionList) {
			if(sec.getType().equalsIgnoreCase(PptSection.TYPE_PARAGRAPH))
			{
				for (PptPragraph paragah:sec.getParagrahList()) {
					PptBaseUtil.insertText(slide, paragah);
				}
			}else if(sec.getType().equalsIgnoreCase(PptSection.TYPE_TABLE))
			{
				PptBaseUtil.insertTable((HSLFSlide)slide, sec.getTableDef(),sec.getResultList());
			}else if(sec.getType().equalsIgnoreCase(PptSection.TYPE_IMAGE))
				PptBaseUtil.insertImage(show, slide, sec.getImageDef());
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
