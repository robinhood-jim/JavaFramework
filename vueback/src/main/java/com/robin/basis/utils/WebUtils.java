package com.robin.basis.utils;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.MessageUtils;
import com.robin.core.query.util.PageQuery;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class WebUtils {
    protected static final String COL_MESSAGE="message";
    protected static final String COL_SUCCESS="success";
    protected static final String COL_COED="code";
    protected static final String COL_DATA="data";
    public static   Map<String, Object> doQuery(IBaseAnnotationJdbcService service, Map<String,String> params, PageQuery query) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            if (query.getParameters().isEmpty() && params!=null) {
                query.setParameters(params);
            }
            service.queryBySelectId(query);
            retMap.put("records",query.getRecordSet());
            retMap.put("total",query.getTotal());
            retMap.put("current",query.getCurrentPage());
            retMap.put("pages",query.getPageCount());
            retMap.put("size",query.getPageSize());
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    protected static void wrapFailed( Map<String, Object> retMap, Exception ex)
    {
        if(ex instanceof ServiceException){
            retMap.put(COL_COED,((ServiceException)ex).getRetCode());
            retMap.put(COL_MESSAGE, MessageUtils.getMessage(((ServiceException)ex).getRetCode()));
        }
        else if(ex instanceof WebException){
            retMap.put(COL_COED,((WebException)ex).getRetCode());
            retMap.put(COL_MESSAGE,MessageUtils.getMessage(((WebException)ex).getRetCode()));
        }else {
            retMap.put(COL_SUCCESS, false);
            retMap.put(COL_MESSAGE, ex.getMessage());
        }
    }
}
