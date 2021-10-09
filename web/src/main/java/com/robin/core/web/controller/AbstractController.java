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
package com.robin.core.web.controller;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.MessageUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.codeset.Code;
import com.robin.core.web.international.Translator;
import com.robin.core.web.codeset.CodeSetService;


import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


public abstract class AbstractController
{

    protected Logger log = LoggerFactory.getLogger(getClass());
    protected static final String COL_MESSAGE="message";
    protected static final String COL_SUCCESS="success";
    protected static final String COL_COED="code";
    protected MessageUtils messageUtils=SpringContextHolder.getBean(MessageUtils.class);

    protected List<Map<String, String>> convertObjToMapList(List<?> orglist)
            throws Exception
    {
        List<Map<String, String>> list = new ArrayList<>();
        if ((orglist != null) && (!orglist.isEmpty())) {
            for (Object object : orglist)
            {
                Map<String, String> map = new HashMap();
                ConvertUtil.objectToMap(map, object);
                list.add(map);
            }
        }
        return list;
    }


    protected void setCode(String codeSetNos)
    {
        if (codeSetNos == null) {
            return;
        }
        String[] codes;
        if (codeSetNos.indexOf(";") !=-1)
        {
            codes = codeSetNos.split(";");
        }
        else
        {
            if (codeSetNos.indexOf(",") !=-1)
            {
                codes = codeSetNos.split(",");
            }
            else
            {
                codes = new String[1];
                codes[0] = codeSetNos;
            }
        }
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        for (int i = 0; i < codes.length; i++)
        {
            util.getCacheCode(codes[i]);
        }
    }
    protected void setCode(String codeSetNo, List<?> codes, String label, String value) {
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        util.setCode(codeSetNo,codes,label,value);
    }


    protected void filterListByCodeSet(PageQuery query, String columnName, String codeNo,String defaultValue)
    {
        if (!query.getRecordSet().isEmpty())
        {
            for (Map<String, Object> map : query.getRecordSet())
            {
                if(map.get(columnName)!=null) {
                    String name = findCodeName(codeNo, map.get(columnName).toString());
                    if ((name != null) && (!"".equals(name))) {
                        map.put(columnName, name);
                    }
                }else{
                    if(defaultValue==null) {
                        map.put(columnName, "");
                    }else{
                        map.put(columnName,defaultValue);
                    }
                }
            }
        }
    }
    protected void fillMissingValue(PageQuery query,String columnName,String defaultValue){
        if (!query.getRecordSet().isEmpty())
        {
            for(Map<String,Object> map:query.getRecordSet()){
                if(map.get(columnName)==null || map.get(columnName).toString().isEmpty()){
                    map.put(columnName,defaultValue);
                }
            }
        }
    }

    protected void filterListByCodeSet(List<?> list, String columnName, String codeNo)
            throws Exception
    {
        if ((list != null) && (!list.isEmpty())) {
            for (Object obj : list)
            {
                Object transval = PropertyUtils.getProperty(obj, columnName);
                String name = findCodeName(codeNo, transval.toString());
                if ((name != null) && (!"".equals(name))) {
                    PropertyUtils.setProperty(obj, columnName, name);
                }
            }
        }
    }

    protected String findCodeName(String codeNo, String value)
    {
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        Map<String,String> codeMap = util.getCacheCode(codeNo);
        if (value == null) {
            return "";
        }
        if (codeMap == null || codeMap.isEmpty()) {
            return "";
        }

        if (codeMap.containsKey(value)) {
            return codeMap.get(value);
        }
        return "";
    }

    protected List<Code> findCodeSetArr(String codeSetNo){
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        return getCodeList(util.getCacheCode(codeSetNo));
    }
    protected void insertNullSelect(List<Map<String, Object>> list)
    {
        Map<String, Object> tmap = new HashMap<>();
        tmap.put("value", "");
        String message=Translator.toLocale("combo.NullDisplay");
        tmap.put("text", message);
        list.add(tmap);
    }
    protected void insertMapToSelect(List<Map<String, Object>> list,Map<String,String> codeMap)
    {
        Iterator<Map.Entry<String,String>> iterator=codeMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String> entry=iterator.next();
            Map<String, Object> tmap = new HashMap<>();
            tmap.put("value", entry.getKey());
            tmap.put("text", entry.getValue());
            list.add(tmap);
        }
    }
    protected List<Map<String,Object>> wrapYesNoCombo(boolean insertNullVal){
        List<Map<String,Object>> list=new ArrayList<>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        Map<String, Object> tmap = new HashMap<>();
        tmap.put("value", Const.VALID);
        tmap.put("text", Translator.toLocale("combo.yesDisplay"));
        list.add(tmap);
        Map<String, Object> tmap1 = new HashMap<>();
        tmap1.put("value", "0");
        tmap1.put("text", Translator.toLocale("combo.noDisplay"));
        list.add(tmap1);
        return list;
    }
    protected List<Map<String,Object>> wrapCodeSet(String codeSetNo){
        List<Map<String,Object>> list=new ArrayList<>();
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        List<Code> codeList=getCodeList(util.getCacheCode(codeSetNo));
        for(Code code:codeList){
            Map<String, Object> tmap = new HashMap<>();
            tmap.put("value", code.getValue());
            tmap.put("text", code.getCodeName());
            list.add(tmap);
        }
        return list;
    }
    protected List<Code> getCodeList(Map<String,String> codeMap){
        List<Code> retlist=new ArrayList<>();
        Iterator<Map.Entry<String,String>> iter= codeMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            retlist.add(new Code(entry.getValue(),entry.getKey()));
        }
        return retlist;
    }
    protected void wrapResponse(Map<String,Object> retmap,Exception ex){
        if(ex!=null){
            wrapFailed(retmap,ex);
        }else
        {
            wrapSuccess(retmap,"success");
        }
    }


    protected void wrapSuccess(Map<String, Object> retMap)
    {
        retMap.put(COL_SUCCESS, true);
        retMap.put(COL_COED, 0);
    }

    protected void wrapFailed( Map<String, Object> retMap, Exception ex)
    {
        if(ex instanceof ServiceException){
            retMap.put(COL_COED,((ServiceException)ex).getRetCode());
            retMap.put(COL_MESSAGE,MessageUtils.getMessage(((ServiceException)ex).getRetCode()));
        }
        else if(ex instanceof WebException){
            retMap.put(COL_COED,((WebException)ex).getRetCode());
            retMap.put(COL_MESSAGE,MessageUtils.getMessage(((WebException)ex).getRetCode()));
        }else {
            retMap.put(COL_SUCCESS, false);
            retMap.put(COL_MESSAGE, ex.getMessage());
        }
    }
    protected void wrapFailed( Map<String, Object> retMap, String message)
    {
        retMap.put(COL_SUCCESS, false);
        retMap.put(COL_MESSAGE, message);
    }


    protected Map<String, Object> wrapSuccess(String displayMsg)
    {
        Map<String, Object> retmap = new HashMap<>();
        retmap.put(COL_SUCCESS, true);
        retmap.put(COL_MESSAGE, displayMsg);
        return retmap;
    }

    protected void wrapSuccess(Map<String, Object> retmap, String displayMsg)
    {
        retmap.put(COL_SUCCESS, true);
        retmap.put(COL_MESSAGE, displayMsg);
    }
    protected void  wrapError(Map<String, Object> retmap,String message)
    {
        retmap.put(COL_SUCCESS, false);
        retmap.put(COL_MESSAGE, message);
    }
    protected Map<String, Object> wrapObject(Object object)
    {
        Map<String, Object> retmap = new HashMap<>();
        retmap.put(COL_SUCCESS, true);
        retmap.put("data", object);
        return retmap;
    }

    protected Map<String, Object> wrapError(Exception ex)
    {
        Map<String, Object> retmap = new HashMap<>();
        retmap.put(COL_SUCCESS, false);
        retmap.put(COL_MESSAGE, ex.getMessage());
        return retmap;
    }

    public static Map<String, String> wrapRequest(HttpServletRequest request)
    {
        Map<String, String> map = new HashMap<>();
        Iterator<String> iter = request.getParameterMap().keySet().iterator();
        while (iter.hasNext())
        {
            String key = iter.next();
            map.put(key, request.getParameter(key));
        }
        return map;
    }
    protected PageQuery wrapPageQuery(Map<String,Object> paramMap){
        PageQuery query = new PageQuery();
        try
        {
            ConvertUtil.mapToObject(query, paramMap);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return query;
    }


    protected PageQuery wrapPageQuery(HttpServletRequest request)
    {
        PageQuery query = new PageQuery();
        Map map = request.getParameterMap();
        Iterator<String> iter = map.keySet().iterator();
        Map<String, Object> tmpmap = new HashMap<>();
        while (iter.hasNext())
        {
            String key = iter.next();
            if (key.startsWith("query.")) {
                tmpmap.put(key.substring(6), request.getParameter(key));
            }
        }
        try
        {
            ConvertUtil.mapToObject(query, tmpmap);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return query;
    }
    public static Map<String,Object> wrapSuccessMsg(String message){
        Map<String,Object> retmap=new HashMap<>();
        retmap.put(COL_SUCCESS, true);
        if(message!=null && !message.trim().isEmpty()){
            retmap.put(COL_MESSAGE,message);
        }
        return retmap;
    }
    public static Map<String,Object> wrapFailedMsg(String message){
        Map<String,Object> retmap=new HashMap<>();
        retmap.put(COL_SUCCESS, false);
        if(message!=null && !message.trim().isEmpty()){
            retmap.put(COL_MESSAGE,message);
        }
        return retmap;
    }
    public static Map<String,Object> wrapFailedMsg(Exception ex){
        Map<String,Object> retmap=new HashMap<>();
        retmap.put(COL_SUCCESS, false);
        if(ex!=null){
            retmap.put(COL_MESSAGE,ex.getMessage());
        }
        return retmap;
    }

}
