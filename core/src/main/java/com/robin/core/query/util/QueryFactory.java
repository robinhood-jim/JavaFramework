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
package com.robin.core.query.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;

import com.robin.core.base.exception.QueryConfgNotFoundException;

public class QueryFactory implements InitializingBean {
    private String xmlConfigPath = "";
    private static Log log = LogFactory.getLog(QueryFactory.class);
    private static Map<String, QueryString> queryMap=new HashMap<>();

    public QueryFactory() {

    }

    public void init() {
        try {
            String xmlpath = xmlConfigPath;
            log.info("begin to parser xml query files");
            if (xmlpath == null || "".equals(xmlpath)) {
                xmlpath = this.getClass().getClassLoader().getResource("").toURI().getPath();//ClassLoaderUtil.getRelativeClassFilePath("../config/queryConfig");
                System.out.println("parse file path=" + xmlpath);
                int pos = xmlpath.indexOf("classes");
                xmlpath = xmlpath.substring(0, pos) + "config/queryConfig";

            } else {
                if (xmlpath.contains("classpath:")) {
                    String relativePath = xmlpath.substring(10);
                    xmlpath = this.getClass().getClassLoader().getResource("").toURI().getPath();
                    xmlpath += relativePath;
                }
            }
            log.info("parse file path=" + xmlpath);

            File file = new File(xmlpath);
            if (!file.isDirectory()) {
                throw new Exception("no query XML found!");
            }
            File[] files = file.listFiles();
            log.debug("file size=" + files.length);
            for (int i = 0; i < files.length; i++) {
                File subfile = files[i];
                if (subfile.getName().endsWith("xml"))
                    parseXML(subfile);
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    public void parseXML(String xmlfile)
            throws Exception {
        if (xmlfile == null || xmlfile.trim().length() == 0) {
            throw new IllegalArgumentException("No xml files!");
        } else {
            Document document = (new SAXReader()).read(new File(xmlfile));
            putQueryMap(document);
            return;
        }
    }

    public void parseXML(File file) throws Exception {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("xml file missing!");
        } else {
            Document document = (new SAXReader()).read(file);
            putQueryMap(document);
            return;
        }
    }

    public void parseXML(InputStream is) throws Exception {
        if (is == null) {
            throw new IllegalArgumentException("parseXML(InputStream is null)!");
        } else {
            Document document = (new SAXReader()).read(is);
            putQueryMap(document);
            return;
        }
    }

    private void putQueryMap(Document document) throws Exception {
        Element root = document.getRootElement();
        String id;
        Iterator iter = root.elementIterator("SQLSCRIPT");
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            id = element.attributeValue("ID");
            if (queryMap.containsKey(id))
                throw new Exception((new StringBuilder()).append("Duplicated selectId:").append(id).toString());

            String sql = element.elementText("SQL");
            if (sql != null) {
                if (sql.indexOf("&lt;") > -1)
                    sql = sql.replaceAll("&lt;", "<");
                else if (sql.indexOf("&gt;") > -1)
                    sql = sql.replaceAll("&gt;", ">");
            }

            String field = element.elementText("FIELD");
            String countSql = element.elementText("COUNTSQL");
            String fromSql = element.elementText("FROMSQL");
            if (fromSql != null) {
                if (fromSql.indexOf("&lt;") > -1)
                    fromSql = fromSql.replaceAll("&lt;", "<");
                else if (fromSql.indexOf("&gt;") > -1)
                    fromSql = fromSql.replaceAll("&gt;", ">");
            }
            QueryString qs = new QueryString();
            qs.setSql(sql);
            qs.setField(field);
            qs.setCountSql(countSql);
            qs.setFromSql(fromSql);
            queryMap.put(id, qs);
        }

    }

    public QueryString getQuery(String selectId) throws QueryConfgNotFoundException {
        try {
            if (selectId != null && !selectId.isEmpty() && queryMap.containsKey(selectId))
                return queryMap.get(selectId);
            else {
                throw new QueryConfgNotFoundException(new Exception("query id not found"));
            }
        } catch (Exception e) {
            throw new QueryConfgNotFoundException(new Exception("query id not found"));
        }
    }
    public boolean isSelectIdExists(String selectId){
        if (selectId != null && !selectId.isEmpty() && queryMap.containsKey(selectId)){
            return true;
        } else {
            return false;
        }
    }


    public void afterPropertiesSet() throws Exception {
        init();

    }


    public String getXmlConfigPath() {
        return xmlConfigPath;
    }


    public void setXmlConfigPath(String xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }


}