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
package com.robin.core.fileaccess.iterator;

import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.util.IOUtils;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractFileIterator implements IResourceIterator {
    protected BufferedReader reader;
    protected InputStream instream;
    protected AbstractFileSystemAccessor accessUtil;
    protected String identifier;
    protected DataCollectionMeta colmeta;
    protected List<String> columnList = new ArrayList<>();
    protected Map<String, DataSetColumnMeta> columnMap = new HashMap<>();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractFileIterator() {

    }

    public AbstractFileIterator(DataCollectionMeta colmeta) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
        checkAccessUtil(colmeta.getPath());
    }

    public AbstractFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessUtil) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
        this.accessUtil = accessUtil;
    }

    @Override
    public void beforeProcess(String resourcePath) {
        checkAccessUtil(resourcePath);
        Assert.notNull(accessUtil, "ResourceAccessUtil is required!");
        try {
            Pair<BufferedReader, InputStream> pair = accessUtil.getInResourceByReader(colmeta, ResourceUtil.getProcessPath(resourcePath));
            this.reader = pair.getKey();
            this.instream = pair.getValue();
        } catch (Exception ex) {
            logger.error("{}",ex.getMessage());
        }
    }

    @Override
    public void afterProcess() {
        try {
            close();
        } catch (IOException ex) {
            logger.error("{}",ex.getMessage());
        }
    }

    @Override
    public void init() {

    }

    protected void checkAccessUtil(String inputPath) {
        try {
            if (accessUtil == null) {
                URI uri = new URI(StringUtils.isEmpty(inputPath) ? colmeta.getPath() : inputPath);
                String schema = !ObjectUtils.isEmpty(colmeta.getFsType())?colmeta.getFsType():uri.getScheme();
                accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }
    @Override
    public void setInputStream(InputStream stream) {
        this.instream = stream;
    }

    protected void copyToLocal(File tmpFile, InputStream stream) {
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            IOUtils.copyBytes(stream, outputStream, 8192);
        } catch (IOException ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (instream != null) {
            instream.close();
        }
    }
    @Override
    public String getIdentifier() {
        return identifier;
    }
}
