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

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.web.codeset.Code;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseContorller
{

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected List<Map<String, String>> convertObjToMapList(List<?> orglist)
            throws Exception
    {
        List<Map<String, String>> list = new ArrayList();
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
        if (codeSetNos.indexOf(";") > 0)
        {
            codes = codeSetNos.split(";");
        }
        else
        {
            if (codeSetNos.indexOf(",") > 0)
            {
                codes = codeSetNos.split(",");
            }
            else
            {
                codes = new String[1];
                codes[0] = codeSetNos;
            }
        }
        for (int i = 0; i < codes.length; i++)
        {
            getCacheCode(codes[i]);
        }
    }
    @CachePut(value = "codeSetCache",key = "#codeSetNo")
    public List<Code> getCacheCode(String codeSetNo) throws DAOException {
        return getCodeSetDefault(codeSetNo);
    }
    protected List<Code> getCodeSetDefault(String codeSetNo){
        JdbcDao jdbcDao= (JdbcDao) SpringContextHolder.getBean(JdbcDao.class);
        QueryFactory factory= (QueryFactory) SpringContextHolder.getBean(QueryFactory.class);
        List<Code> codeList=new ArrayList<>();
        if(factory.isSelectIdExists("$_GETCODESET")){
            PageQuery query=new PageQuery();
            query.setPageSize("0");
            query.setSelectParamId("$_GETCODESET");
            query.getParameters().put("queryString"," and codeSetNo=?");
            query.setParameterArr(new Object[]{codeSetNo});
            jdbcDao.queryBySelectId(query);
            if(!query.getRecordSet().isEmpty()){
                for(Map<String,Object> map:query.getRecordSet()){
                    codeList.add(new Code(map.get("ITEMNAME").toString(),map.get("ITEMVALUE").toString()));
                }
            }
            return codeList;
        }else{
            throw new DAOException(" Query Parameter $_GETCODESET not config,Please config queryConfig xml");
        }
    }


    @CachePut(value = "codeSetCache",key = "#codeSetNo")
    public List<Code> setCode(String codeSetNo, List<?> codes, String label, String value)
    {
        if (codes == null) {
            return null;
        }
        List<Code> al = new ArrayList();
        for (int i = 0; i < codes.size(); i++)
        {
            Code code = new Code();
            Object objtmp = codes.get(i);
            Object ol ;
            Object ov;
            try
            {
                ol = PropertyUtils.getProperty(objtmp, label);
                ov = PropertyUtils.getProperty(objtmp, value);
            }
            catch (Exception e)
            {
                ol = null;
                ov = null;
            }
            if ((ol != null) && (ov != null))
            {
                code.setCodeName(ol.toString());
                code.setValue(ov.toString());
                al.add(code);
            }
        }
        return al;
    }

    protected void filterListByCodeSet(PageQuery query, String columnName, String codeNo)
    {
        if (!query.getRecordSet().isEmpty())
        {
            List<Map<String, Object>> list = query.getRecordSet();
            for (Map<String, Object> map : list)
            {
                String name = findCodeName(codeNo, map.get(columnName).toString());
                if ((name != null) && (!"".equals(name))) {
                    map.put(columnName, name);
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
        List<Code> list = getCacheCode(codeNo);
        if (value == null) {
            return "";
        }
        if (list == null) {
            return "";
        }
        for (int i = 0; i < list.size(); i++)
        {
            Code code = (Code)list.get(i);
            if (value.equals(code.getValue())) {
                return code.getCodeName();
            }
        }
        return "";
    }


}
