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

import com.robin.core.base.exception.MissingConfigExecption;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class QueryFactory implements InitializingBean {
    private String xmlConfigPath = "";
    private static Logger log = LoggerFactory.getLogger(QueryFactory.class);
    private static Map<String, QueryString> queryMap = new HashMap<>();

    public QueryFactory() {

    }

    public void init() {
        try {
            List<InputStream> configStreams=ConfigResourceScanner.doScan(xmlConfigPath);
            for(InputStream stream:configStreams){
                parseXML(stream);
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {

        }
    }


    private void parseXML(File file) throws Exception {
        if (file != null && file.isFile()) {
            Document document = new SAXReader().read(file);
            putQueryMap(document);
        }
    }

    private void parseXML(InputStream is) throws Exception {
        if (is == null) {
            throw new IllegalArgumentException("parseXML(InputStream is null)!");
        } else {
            Document document = new SAXReader().read(is);
            putQueryMap(document);
        }
    }


    private void putQueryMap(Document document) {
        Element root = document.getRootElement();
        String id;
        Iterator iter = root.elementIterator("SQLSCRIPT");
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            id = element.attributeValue("ID");
            if (queryMap.containsKey(id))
                throw new MissingConfigExecption((new StringBuilder()).append("Duplicated selectId:").append(id).toString());

            String sql = decodeSql(element.elementText("SQL"));
            String field = element.elementText("FIELD");
            String countSql = element.elementText("COUNTSQL");
            String fromSql = decodeSql(element.elementText("FROMSQL"));
            if(element.element("WHERE")!=null){

            }

            QueryString qs = new QueryString();
            qs.setSql(sql);
            qs.setField(field);
            qs.setCountSql(countSql);
            qs.setFromSql(fromSql);
            queryMap.put(id, qs);
        }

    }

    private String decodeSql(String sql) {
        String tmpSql = sql;
        if (tmpSql != null) {
            if (tmpSql.indexOf("&lt;") > -1)
                tmpSql = tmpSql.replaceAll("&lt;", "<");
            else if (tmpSql.indexOf("&gt;") > -1)
                tmpSql = tmpSql.replaceAll("&gt;", ">");
        }
        return tmpSql;
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

    public boolean isSelectIdExists(String selectId) {
        return selectId != null && !selectId.isEmpty() && queryMap.containsKey(selectId);
    }


    public void afterPropertiesSet() {
        init();
    }


    public String getXmlConfigPath() {
        return xmlConfigPath;
    }


    public void setXmlConfigPath(String xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }


}