package com.robin.core.query.mapper;

import com.robin.core.query.mapper.handler.CommHandler;
import com.robin.core.query.mapper.handler.IHandler;
import com.robin.core.query.mapper.segment.AbstractSegment;
import com.robin.core.query.util.ConfigResourceScanner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqlMapperConfigure implements InitializingBean {
    String xmlConfigPath = "";
    Map<String, IHandler> handlerMap = new HashMap<>();
    Map<String, Map<String, ImmutablePair<String, List<AbstractSegment>>>> segmentsMap = new HashMap<>();
    QueryConfigMapperReader reader;
    private IHandler handler;
    public SqlMapperConfigure(){

    }

    @Override
    public void afterPropertiesSet() {
        reader = new QueryConfigMapperReader(handlerMap, segmentsMap);
        handler = new CommHandler();
        parseResource();
    }



    private void parseResource() {
        List<InputStream> configStreams = ConfigResourceScanner.doScan(xmlConfigPath, "queryMapper");
        for (InputStream stream : configStreams) {
            reader.readConfigMapper(stream);
        }
    }

    public Map<String, Map<String, ImmutablePair<String, List<AbstractSegment>>>> getSegmentsMap() {
        return segmentsMap;
    }

    public void setXmlConfigPath(String xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }


    public class QueryConfigMapperReader {
        private Map<String, IHandler> handlerMap = new HashMap<>();
        private Map<String, Map<String, ImmutablePair<String, List<AbstractSegment>>>> segmentsMap = new HashMap<>();

        public QueryConfigMapperReader(Map<String, IHandler> handlerMap, Map<String, Map<String, ImmutablePair<String, List<AbstractSegment>>>> segmentsMap) {
            this.handlerMap = handlerMap;
            this.segmentsMap = segmentsMap;
        }

        public void readConfigMapper(InputStream stream) {
            try {
                Document document = new SAXReader().read(stream);
                Element root = document.getRootElement();
                String namespace = root.attributeValue("namespace");
                segmentsMap.put(namespace, new HashMap<>());

                if (root.elements().size() > 1) {
                    for (Element ele1 : root.elements()) {
                        NavigateElements(namespace, ele1);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void NavigateElements(String nameSpace, Element element) {
            //List<Element> elements=element.elements();
            List<AbstractSegment> segmentList = new ArrayList<>();

            handler.analyse(element, nameSpace, segmentList);

            segmentsMap.get(nameSpace).put(element.attributeValue("id"), new ImmutablePair<>(element.getName(), segmentList));
        }
    }
}


