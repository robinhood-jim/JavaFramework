package com.robin.comm.util.ppt;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;

public class PptGenerater {
	public static void GeneratePpt(SlideShow show,PptConfig config){
		List<List<PptSection>> sectionList=config.getSectionList();
		PptHeaderFooterSection headerFooter=config.getHeaderFooter();
		if(config.getBackgroundPic()!=null)
			try {
				PptBaseUtil.setBackground(show, config);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				PptBaseUtil.insertTable(slide, sec.getTableDef(),sec.getResultList());
			}else if(sec.getType().equalsIgnoreCase(PptSection.TYPE_IMAGE))
				PptBaseUtil.insertImage(show, slide, sec.getImageDef());
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
