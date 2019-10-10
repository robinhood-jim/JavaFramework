package com.robin.core.query.mapper.handler;

import com.robin.core.query.mapper.segment.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractHandler implements IHandler {
    private StringBuilder builder=new StringBuilder();
    @Override
    public void analyse(Element element,String nameSpace, List<AbstractSegment> segments) {
        List<Node> elements=element.content();
        if(elements.size()>1 && !"resultMap".equalsIgnoreCase(element.getName())) {
            if("select".equalsIgnoreCase(element.getName()) || "update".equalsIgnoreCase(element.getName()) || "insert".equalsIgnoreCase(element.getName()) || "batch".equalsIgnoreCase(element.getName())) {
                doProcessElement(element,nameSpace,segments);
            }else {
                for (Node ele : elements) {
                    doProcessNode(ele, nameSpace, segments);
                }
            }
        }else{
            if("resultMap".equalsIgnoreCase(element.getName())){
                doProcessElement(element,nameSpace,segments);
            }else
                doProcessNode(element,nameSpace, segments);
        }
    }

    @Override
    public String getValue(Map<String, Object> params, List<AbstractSegment> segments, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        clearBuilder();
        for(AbstractSegment segment:segments){
           if(segment instanceof IncludeSegment){
                List<AbstractSegment> includeSegments=segmentsMap.get(segment.getId()).getRight();
                for(AbstractSegment segment1:includeSegments){
                    builder.append(segment1.getSqlPart(params,segmentsMap));
                }
            }else {
               builder.append(segment.getSqlPart(params,segmentsMap));
           }
        }
        return null;
    }
    private void clearBuilder(){
        if(builder.length()>0){
            builder.delete(0,builder.length());
        }
    }

    private void doProcessNode(Node node, String nameSpace, List<AbstractSegment> segments){
        if(node.getNodeType()==Node.TEXT_NODE){
            segments.add(new ConstantSegment(nameSpace, "", node.getText()));
        }else {
            Element ele=(Element) node;
            doProcessElement(ele,nameSpace,segments);
        }
    }
    private void doProcessElement(Element ele,String nameSpace,List<AbstractSegment> segments){

        String type = ele.getName();
        String id = ele.attributeValue("id");

        if (ele.elements().size() == 1 && type == null) {
            segments.add(new ConstantSegment(nameSpace, id, ele.getText()));
        } else
        {
            switch (type){
                case "include":
                    segments.add(new IncludeSegment(nameSpace, id, ele.attributeValue("refid")));
                    break;
                case "script":
                    String language = ele.attributeValue("lang");
                    ScriptSegment segment = new ScriptSegment(nameSpace, id, ele.getStringValue());
                    segment.setScriptType(language);
                    segments.add(segment);
                    break;
                case "sql":
                    segments.add(new SqlSegment(nameSpace, id, ele.getStringValue()));
                    break;
                case "resultMap":
                    ResultMapperSegment segment1 = new ResultMapperSegment(nameSpace, id, null);
                    segment1.parse(ele);
                    segments.add(segment1);
                    break;
                case "select":
                case "insert":
                case "update":
                case "batch":
                    List<Node> elements=ele.content();
                    List<AbstractSegment> segments1=new ArrayList<>();
                    String resultMap=ele.attributeValue("resultMap");
                    String paramType=ele.attributeValue("parameterType");
                    for(Node node:elements){
                        doProcessNode(node,nameSpace,segments1);
                    }
                    CompositeSegment compSeg;
                    if("select".equalsIgnoreCase(type)){
                        compSeg=new SelectSegment(nameSpace,id,null,type,segments1,resultMap,paramType);
                        if(ele.attributeValue("countRef")!=null){
                            ((SelectSegment) compSeg).setCountRef(ele.attributeValue("countRef"));
                        }
                    }else if("insert".equalsIgnoreCase(type)){
                        boolean useGenerateKeys=ele.attributeValue("useGeneratedKeys")!=null? "true".equalsIgnoreCase(ele.attributeValue("useGeneratedKeys")) :false;
                        compSeg=new InsertSegment(nameSpace,id,null,type,segments1,resultMap,paramType,useGenerateKeys,ele.attributeValue("keyProperty"));
                    }else{
                        compSeg=new CompositeSegment(nameSpace,id,null,type,segments1,resultMap,paramType);
                    }
                    segments.add(compSeg);
                    break;
                default:
                    break;
            }
        }

    }


}
