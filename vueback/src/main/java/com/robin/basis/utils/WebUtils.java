package com.robin.basis.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.service.IMybatisBaseService;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.MessageUtils;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.query.util.PageQuery;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WebUtils {
    protected static final String COL_MESSAGE="message";
    protected static final String COL_SUCCESS="success";
    protected static final String COL_COED="code";
    protected static final String COL_DATA="data";
    public static   Map<String, Object> doQuery(IMybatisBaseService service, Map<String,String> params, PageQuery query, Function<Map<String,Object>,?> mapFunction) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            if (query.getParameters().isEmpty() && params!=null) {
                query.setParameters(params);
            }
            service.queryBySelectId(query);
            retMap.put("records",!ObjectUtils.isEmpty(mapFunction)?query.getRecordSet().stream().map(mapFunction).collect(Collectors.toList()):query.getRecordSet());
            retMap.put("total",query.getTotal());
            retMap.put("current",query.getCurrentPage());
            retMap.put("pages",query.getPageCount());
            retMap.put("size",query.getPageSize());
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    public static <T,O> Map<String,Object> toPageVO(IPage<T> page, Function<T,O> function){
        List<O> list=page.getRecords().stream().map(function).collect(Collectors.toList());
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("pages",page.getPages());
        retMap.put("total",page.getTotal());
        retMap.put("records",list);
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
    public static void returnOSSResource(HttpServletResponse response, @NonNull AbstractFileSystemAccessor accessor, String ossPath) throws IOException {
        FileUtils.FileContent content= FileUtils.parseFile(ossPath);
        try(InputStream inputStream=accessor.getInResourceByStream(ossPath)) {
            response.setHeader("content-type", content.getContentType());
            IOUtils.copyBytes(inputStream, response.getOutputStream());
        }catch (IOException ex){
            throw ex;
        }
    }

}
