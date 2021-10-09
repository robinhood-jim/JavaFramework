package com.robin.comm.util.word;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;

public class WordBaseUtil {
    private WordBaseUtil(){

    }
    public static Font createFont(BaseFont baseFont, int fontsize, int fontStyle) {
        return new Font(baseFont, fontsize, fontStyle);
    }

    public static void insertImg(Document document, WordImageSection section) throws MalformedURLException, IOException, DocumentException {
        Image img = Image.getInstance(section.getImgUrl());
        if (img == null) {
            return;
        }
        img.setAbsolutePosition(section.getAbsoluteX(), section.getAbsoluteY());
        img.setAlignment(section.getImageAlign());
        img.scaleAbsolute(section.getHeight(), section.getWeight());
        img.scalePercent(section.getPercent());
        img.scalePercent(section.getHeightPercent(), section.getWeightPercent());
        img.setRotation(section.getRotation());

        document.add(img);
    }

    public static void insertTitle(Document document, BaseFont baseFont, String titleStr, int fontsize, int fontStyle, int elementAlign) throws DocumentException {
        Font titleFont = new Font(baseFont, fontsize, fontStyle);
        Paragraph title = new Paragraph(titleStr);
        title.setAlignment(elementAlign);
        title.setFont(titleFont);
        document.add(title);
    }

    public static void insertContext(Document document, BaseFont baseFont, String contextStr, int fontsize, int fontStyle, int elementAlign) throws DocumentException {
        Font contextFont = new Font(baseFont, fontsize, fontStyle);
        Paragraph context = new Paragraph(contextStr);
        context.setLeading(30f);
        context.setAlignment(elementAlign);
        context.setFont(contextFont);
        context.setSpacingBefore(5);
        context.setFirstLineIndent(20);
        document.add(context);
    }

    public static void insertTable(Document document, WordConfig cfg, WordTableDef tabDef, WordTableHeaderDef headerDef, List<Map<String, String>> resultList) throws DocumentException {
        int totalCount = 0;
        if (headerDef != null) {
            totalCount = headerDef.getHeaderNums() + resultList.size();
        } else {
            totalCount = resultList.size() + 1;
        }
        Table table = new Table(tabDef.getDbColumnList().size(), totalCount);
        table.setWidths(tabDef.getColWidthPercent());
        table.setWidth(tabDef.getTableWidth());
        table.setAlignment(tabDef.getTableAligement());
        table.setAutoFillEmptyCells(true);
        table.setBorderWidth(tabDef.getBorderWidth());
        table.setBorderColor(new Color(0, 125, 255));
        Font headerfont = createFont(tabDef.getBaseFont(), cfg.getFontSize(), Font.BOLD);

        if (headerDef != null) {
            for (int i = 0; i < headerDef.getColumnList().size(); i++) {
                List<WordTableColumnDef> list = headerDef.getColumnList().get(i);
                for (WordTableColumnDef coldef : list) {
                    Cell cell = new Cell(new Phrase(coldef.getColumnName(), headerfont));
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setRowspan(coldef.getRowSpan());
                    cell.setColspan(coldef.getColSpan());
                    table.addCell(cell);
                }
            }
        } else {
            for (int i = 0; i < tabDef.getDisplayColumnList().size(); i++) {
                Cell cell = new Cell(new Phrase(tabDef.getDisplayColumnList().get(i), headerfont));
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

        }
        table.endHeaders();
        for (int i = 0; i < resultList.size(); i++) {
            Map<String, String> resultMap = resultList.get(i);
            for (String column : tabDef.getDbColumnList()) {
                Cell cell = new Cell(resultMap.get(column));
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

        }
        document.add(table);
        document.add(new Paragraph("\n"));

    }

    public static void insertHeader(Document document, WordHeaderFooterSection section) throws BadElementException, MalformedURLException, IOException {
        if (section != null) {
            Paragraph headerParagraph = new Paragraph();
            Paragraph footerPara = new Paragraph();
            if (section.getHeaderpicname() != null && !"".equals(section.getHeaderpicname().trim())) {
                Image headerImage = Image.getInstance(section.getHeaderpicname());
                headerParagraph.add(headerImage);
            }
            if (section.getHeaderString() != null && !"".equals(section.getHeaderString())) {
                Paragraph para = new Paragraph(section.getHeaderString());
                headerParagraph.add(para);
            }
            if (section.getFooterpicName() != null && !"".equals(section.getFooterpicName().trim())) {
                Image headerImage = Image.getInstance(section.getFooterpicName());
                footerPara.add(headerImage);
            }
            if (section.getFooterString() != null && !"".equals(section.getFooterString())) {
                Paragraph para = new Paragraph(section.getFooterString());
                footerPara.add(para);
            }
            HeaderFooter headerfooter = new HeaderFooter(headerParagraph, true);
            headerfooter.setAlignment(Paragraph.ALIGN_CENTER);
            HeaderFooter footer = new HeaderFooter(footerPara, true);
            footer.setAlignment(Paragraph.ALIGN_RIGHT);
            document.setHeader(headerfooter);
            document.setFooter(footer);
        }
    }

}
