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
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.codeset.Code;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *use Chinese Alter msg.Later will change to i18n
 */
public abstract class BaseCrudDhtmlxController<O extends BaseObject,P extends Serializable,S extends BaseAnnotationJdbcService> extends BaseCrudController<O,P,S> {
    public Map<String, Object> wrapYesNoComobo(boolean insertNullVal)
    {
        Map<String, Object> map = new HashMap();
        List<Map<String, Object>> list = new ArrayList();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        Map<String, Object> tmap = new HashMap();
        tmap.put("value", "1");
        tmap.put("text", "是");
        list.add(tmap);
        Map<String, Object> tmap1 = new HashMap();
        tmap.put("value", "0");
        tmap.put("text", "否");
        list.add(tmap1);
        map.put("options", list);
        return map;
    }

    public Map<String, Object> wrapComobo(List<Map<String, Object>> rsList, String keyColumn, String valueColumn, boolean insertNullVal)
    {
        Map<String, Object> map = new HashMap();
        List<Map<String, Object>> list = new ArrayList();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        for (Map<String, Object> tmap : rsList)
        {
            Map<String, Object> rmap = new HashMap();
            if (tmap.containsKey(keyColumn))
            {
                rmap.put("text", tmap.get(valueColumn).toString());
                rmap.put("value", tmap.get(keyColumn).toString());
                list.add(rmap);
            }
        }
        map.put("options", list);
        return map;
    }

    public Map<String, Object> wrapComoboWithCode(List<Code> rsList, boolean insertNullVal)
    {
        Map<String, Object> map = new HashMap();
        List<Map<String, Object>> list = new ArrayList();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        for (Code code : rsList)
        {
            Map<String, Object> rmap = new HashMap();
            rmap.put("text", code.getCodeName());
            rmap.put("value", code.getValue());
            list.add(rmap);
        }
        map.put("options", list);
        return map;
    }

    public void insertNullSelect(List<Map<String, Object>> list)
    {
        Map<String, Object> tmap = new HashMap();
        tmap.put("value", "");
        tmap.put("text", "--请选择--");
        list.add(tmap);
    }

    public void wrapStatusBar(PageQuery query)
    {
        String str = "";
        int pageNo = Integer.parseInt(query.getPageNumber());
        int totalCount = Integer.parseInt(query.getRecordCount());
        int totalPage = Integer.parseInt(query.getPageCount());
        int prevPage = pageNo > 1 ? pageNo - 1 : 1;
        int nextPage = pageNo + 1 >= totalPage ? totalPage : pageNo + 1;

        str = str + "<div class='dhx_toolbar_material dhxtoolbar_icons_18 dhx_toolbar_shadow'>";
        str = str + "<table width=\"100%\" height=\"20\" cellpadding=\"0\" cellspacing=\"0\" >";
        str = str + "<td align='left' width='70%'>共&nbsp;" + totalCount + "&nbsp;条,第&nbsp;" + pageNo + "页/共&nbsp;" + totalPage + " 页&nbsp;<input type=\"textbox\" size=3 align=\"right\" class=\"pTextStyle\" name=\"pageSize\" id=\"pageSize\" value=\"" + query.getPageSize() + "\" onKeyPress=\"javascript:setpagesize();\">" + "条/页&nbsp;</td>";
        str = str + " <td align=\"right\" width=\"30%\" ><div style=\"text-align: right;overflow: hidden;\">";
        if (pageNo <= 1)
        {
            str = str + "<span class='greyleftPageMore'>首页</span><span class='greyleftPage'>上一页</span>";
        }
        else
        {
            str = str + "<span><a class='leftPageMore' href='javascript:goFirstPage()'>首页</a></span>";
            str = str + "<span><a class='leftPage' href='javascript:goPreviousPage()'>上一页</a></span>";
        }
        if ((totalPage == pageNo) || (totalPage == 0))
        {
            str = str + "<span class='greyrightPage'>下一页</span><span class='greyrightPageMore'>尾页</span>";
        }
        else
        {
            str = str + "<span><a class='rightPage' href='javascript:goNextPage()'>下一页</a></span>";
            str = str + "<span><a class='rightPageMore' href='javascript:goLasePage()'>尾页</a></span>";
        }
        str = str + "<span>跳转到<input type='text' name='jumpNum' id='jumpNum' value='" + pageNo + "' size='2' maxlength='9'>" + "页";
        str = str + "<input type='button' name='jumpPage' value='GO' onclick='goPage()' class='dhxform_btn_txt' ></span>";

        str = str + "</div></td></table>";
        str = str + "</div>";
        if (totalCount == 0) {
            str = "";
        }
        query.setPageToolBar(str);
    }

}
