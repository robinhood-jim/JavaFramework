package com.robin.core.base.util;

import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import java.util.List;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.base.util</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年11月07日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class FileResourceType {
    private String sourceType;
    private String format;
    private String compressType;
    private String path;
    private boolean hasHeader=false;
    private List<String> columns;
    private List<DataSetColumnMeta> columnMetas;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<DataSetColumnMeta> getColumnMetas() {
        return columnMetas;
    }

    public void setColumnMetas(List<DataSetColumnMeta> columnMetas) {
        this.columnMetas = columnMetas;
    }
}
