package com.robin.basis.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.service.IMybatisBaseService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.MessageUtils;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.LocalFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.query.util.PageQuery;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyyMMdd");
    public static  Map<String, Object> doQuery(IMybatisBaseService service, Map<String,String> params, PageQuery query, Function<Map<String,Object>,?> mapFunction) {
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
        List list=page.getRecords();
        if(function!=null) {
            list = page.getRecords().stream().map(function).collect(Collectors.toList());
        }
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("pages",page.getPages());
        retMap.put("total",page.getTotal());
        retMap.put("records",list);
        return retMap;
    }
    public static  Map<String,Object> toEmptyPageVO(){
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("pages",1);
        retMap.put("total",0);
        retMap.put("records", Lists.newArrayList());
        return retMap;
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
    public static String uploadToOss(MultipartFile file,@NonNull AbstractFileSystemAccessor accessor) throws IOException{
        String path=file.getOriginalFilename();
        FileUtils.FileContent content=FileUtils.parseFile(path);
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String ossPath=null;
        String fileName=null;
        if(LocalFileSystemAccessor.class.isAssignableFrom(accessor.getClass()) && environment.containsProperty("oss.startPath")){
            if(SecurityUtils.isLoginUserSystemAdmin()) {
                fileName="/system/"+formatter.format(LocalDateTime.now())+"/"+ System.currentTimeMillis() + "." + content.getFileFormat();
                ossPath = environment.getProperty("oss.startPath") + fileName;
            }else{
                fileName="/tenant"+SecurityUtils.getLoginUser().getTenantId()+"/"+formatter.format(LocalDateTime.now())+"/"+ System.currentTimeMillis() + "." + content.getFileFormat();
                ossPath =environment.getProperty("oss.startPath")+fileName;
            }
        }else{
            if(SecurityUtils.isLoginUserSystemAdmin()){
                ossPath="/"+formatter.format(LocalDateTime.now())+"/"+ System.currentTimeMillis() + "." + content.getFileFormat();
                fileName=ossPath;
            }else{
                ossPath="/"+formatter.format(LocalDateTime.now())+"/"+ System.currentTimeMillis() + "." + content.getFileFormat();
                fileName=ossPath;
            }

        }
        try(OutputStream outputStream=accessor.getRawOutputStream(ossPath)){
            IOUtils.copyBytes(file.getInputStream(),outputStream);
            accessor.finishWrite(outputStream);
        }catch (IOException ex){
            throw ex;
        }
        return fileName;
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
    public static void main(String[] args){
        try{
            DataCollectionMeta collectionMeta=DataCollectionMeta.fromYamlConfig("classpath:qiniu.yaml");
            System.out.println(collectionMeta);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
