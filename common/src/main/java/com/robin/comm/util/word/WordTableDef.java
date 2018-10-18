package com.robin.comm.util.word;

import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.pdf.BaseFont;

public class WordTableDef {
	private int tableWidth;
	private int tableAligement;
	private int borderWidth;
	private int[] borderColor;
	private int padding;
	private int spacing;
	private int border;
	private float[] colWidthPercent;
	private List<String> dbColumnList=new ArrayList<String>();
	private List<String> displayColumnList=new ArrayList<String>();
	private BaseFont baseFont;
	private int recordSize;
	public int getTableWidth() {
		return tableWidth;
	}
	public void setTableWidth(int tableWidth) {
		this.tableWidth = tableWidth;
	}
	public int getTableAligement() {
		return tableAligement;
	}
	public void setTableAligement(int tableAligement) {
		this.tableAligement = tableAligement;
	}
	public int getBorderWidth() {
		return borderWidth;
	}
	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}
	public int[] getBorderColor() {
		return borderColor;
	}
	public void setBorderColor(int[] borderColor) {
		this.borderColor = borderColor;
	}
	public int getPadding() {
		return padding;
	}
	public void setPadding(int padding) {
		this.padding = padding;
	}
	public int getSpacing() {
		return spacing;
	}
	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}
	public int getBorder() {
		return border;
	}
	public void setBorder(int border) {
		this.border = border;
	}
	public float[] getColWidthPercent() {
		return colWidthPercent;
	}
	public void setColWidthPercent(float[] colWidthPercent) {
		this.colWidthPercent = colWidthPercent;
	}
	public List<String> getDbColumnList() {
		return dbColumnList;
	}
	public void setDbColumnList(List<String> dbColumnList) {
		this.dbColumnList = dbColumnList;
	}
	public BaseFont getBaseFont() {
		return baseFont;
	}
	public void setBaseFont(BaseFont baseFont) {
		this.baseFont = baseFont;
	}
	public List<String> getDisplayColumnList() {
		return displayColumnList;
	}
	public void setDisplayColumnList(List<String> displayColumnList) {
		this.displayColumnList = displayColumnList;
	}
	public int getRecordSize() {
		return recordSize;
	}
	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}

}
