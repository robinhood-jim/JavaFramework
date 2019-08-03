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
import com.robin.core.web.util.CodeSetUtil;
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
        CodeSetUtil util= (CodeSetUtil) SpringContextHolder.getBean(CodeSetUtil.class);
        for (int i = 0; i < codes.length; i++)
        {
            util.getCacheCode(codes[i]);
        }
    }
    protected void setCode(String codeSetNo, List codes, String label, String value) {
        CodeSetUtil util= (CodeSetUtil) SpringContextHolder.getBean(CodeSetUtil.class);
        util.setCode(codeSetNo,codes,label,value);
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
        CodeSetUtil util= (CodeSetUtil) SpringContextHolder.getBean(CodeSetUtil.class);
        List<Code> list = util.getCacheCode(codeNo);
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
    protected List<Code> findCodeSetArr(String codeSetNo){
        CodeSetUtil util= (CodeSetUtil) SpringContextHolder.getBean(CodeSetUtil.class);
        return util.getCacheCode(codeSetNo);
    }

}
