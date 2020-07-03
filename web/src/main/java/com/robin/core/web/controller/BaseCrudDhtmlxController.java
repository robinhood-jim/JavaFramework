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

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.web.codeset.Code;
import com.robin.core.web.codeset.CodeSetService;
import com.robin.core.web.international.Translator;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * use Chinese Alter msg.Later will change to i18n
 */
public abstract class BaseCrudDhtmlxController<O extends BaseObject, P extends Serializable, S extends IBaseAnnotationJdbcService<O,P>> extends BaseCrudController<O, P, S> {


    protected Map<String, Object> wrapComobo(List<Map<String, Object>> rsList, String keyColumn, String valueColumn, boolean insertNullVal) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        for (Map<String, Object> tmap : rsList) {
            Map<String, Object> rmap = new HashMap();
            if (tmap.containsKey(keyColumn)) {
                rmap.put("text", tmap.get(valueColumn).toString());
                rmap.put("value", tmap.get(keyColumn).toString());
                list.add(rmap);
            }
        }
        map.put("options", list);
        return map;
    }

    protected Map<String, Object> wrapComoboWithCode(List<Code> rsList, boolean insertNullVal) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        for (Code code : rsList) {
            Map<String, Object> rmap = new HashMap();
            rmap.put("text", code.getCodeName());
            rmap.put("value", code.getValue());
            list.add(rmap);
        }
        map.put("options", list);
        return map;
    }



    protected Map<String, Object> wrapDhtmlxGridOutputWithNoCheck(List<Map<String, String>> list, String queryKeys, String idColumn) {
        return wrapDhtmlxGridOutputWithCheck(list, queryKeys, idColumn, false);
    }

    private Map<String, Object> wrapDhtmlxGridOutputWithCheck(List<Map<String, String>> list, String queryKeys, String idColumn, boolean withcheck) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            String[] fieldNames = queryKeys.split(",");
            List<Map<String, Object>> retList = new ArrayList<>();
            PageQuery tquery = new PageQuery();
            tquery.setRecordCount(list.size());
            tquery.setPageSize(0);
            for (Map<String, String> map : list) {
                Map<String, Object> tmap = new HashMap<String, Object>();
                List<String> tmpList = new ArrayList<String>();
                for (String key : fieldNames) {
                    if (withcheck && key.equals(idColumn)) {
                        tmpList.add("0");
                    } else if (map.containsKey(key) && map.get(key)!=null) {
                        tmpList.add(map.get(key));
                    }
                }
                tmap.put("data", tmpList);
                tmap.put("id", map.get(idColumn));

                retList.add(tmap);
            }
            retMap.put("rows", retList);
            wrapPageQuery(tquery,retMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retMap;
    }

    protected void wrapObjectWithRequest(HttpServletRequest request, BaseObject obj) throws Exception {
        ConvertUtil.mapToObject(obj, wrapRequest(request));
    }

    protected Map<String, Object> returnCodeSetDhtmlxCombo(String codeSetNo, boolean allowNulls) {
        setCode(codeSetNo);
        Map<String, Object> retmap = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        CodeSetService util = SpringContextHolder.getBean(CodeSetService.class);
        List<Code> codeList =getCodeList(util.getCacheCode(codeSetNo));
        if (codeList != null) {
            if (allowNulls) {
                list.add(addNullSelection());
            }
            for (Code set : codeList) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("value", set.getValue());
                map.put("text", set.getCodeName());
                list.add(map);
            }
        }
        retmap.put("options", list);
        return retmap;
    }

    private Map<String, String> addNullSelection() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("value", "");
        map.put("text", Translator.toLocale("message.NullDisplay"));
        return map;
    }

    protected Map<String, Object> wrapDhtmlxGridOutput(PageQuery query) {
        List<Map<String, Object>> list = query.getRecordSet();
        Map<String, Object> retMap = new HashMap<String, Object>();
        PageQuery tquery = query;
        tquery.setRecordSet(null);
        tquery.getParameters().clear();
        try {
            QueryFactory factory = (QueryFactory) SpringContextHolder.getBean("queryFactory");
            QueryString queryObj = factory.getQuery(query.getSelectParamId());
            String field = queryObj.getField();
            String[] arr = field.split(",");
            List<String> fieldName = new ArrayList<String>();
            String idColumn = null;
            for (int i = 0; i < arr.length; i++) {
                String colname = null;
                if (arr[i].contains(" as")) {
                    String[] arr1 = arr[i].split(" as");
                    colname = arr1[1].trim();
                } else if (arr[i].contains(" AS")) {
                    String[] arr1 = arr[i].split(" AS");
                    colname = arr1[1].trim();
                } else {
                    colname = arr[i].trim();
                }
                if (i == 0) {
                    idColumn = colname;
                }
                fieldName.add(colname);
            }
            List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> map : list) {
                Map<String, Object> tmap = new HashMap<String, Object>();
                List<String> tmpList = new ArrayList<String>();
                for (String key : fieldName) {
                    if (key.equals(idColumn)) {
                        tmpList.add("0");
                    } else if (map.containsKey(key)) {
                        if (map.get(key)!=null)
                            tmpList.add(map.get(key).toString());
                        else
                            tmpList.add("");
                    }
                }
                tmap.put("data", tmpList);
                tmap.put("id", map.get(idColumn));
                retList.add(tmap);
            }
            retMap.put("rows", retList);
            wrapPageQuery(query,retMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retMap;
    }
    private void wrapPageQuery(PageQuery pageQuery,Map<String,Object> retMap){

        if(pageQuery!=null){
            retMap.put("pageSize",pageQuery.getPageSize());
            retMap.put("pageNumber",pageQuery.getPageNumber());
            retMap.put("pageCount",pageQuery.getPageCount());
            retMap.put("recordCount",pageQuery.getRecordCount());
            retMap.put("order",pageQuery.getOrder());
            retMap.put("orderDirection",pageQuery.getOrderDirection());
        }
    }


}
