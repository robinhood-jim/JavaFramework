package com.robin.core.web.util;


import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class HttpContextUtils {
    private static final Gson gson= GsonUtil.getGson();


    private HttpContextUtils(){

    }
    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Enumeration<String> parameters = request.getParameterNames();

        Map<String, String> params = new HashMap<>();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            String value = request.getParameter(parameter);
            if (StringUtils.isNotBlank(value)) {
                params.put(parameter, value);
            }
        }

        return params;
    }

    public static String getDomain(){
        HttpServletRequest request = getHttpServletRequest();
        Assert.notNull(request,"");
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    public static String getOrigin(){
        HttpServletRequest request = getHttpServletRequest();
        Assert.notNull(request,"");
        return request.getHeader(HttpHeaders.ORIGIN);
    }



    public static String getLanguage() {

        String defaultLanguage = "zh-CN";
        //request
        HttpServletRequest request = getHttpServletRequest();
        if(request == null){
            return defaultLanguage;
        }
        defaultLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);

        return defaultLanguage;
    }
    //获取请求的参数，urlencode和json以及多文件上传模式
    public static Map<String,Object> parseRequest(HttpServletRequest request){
        Map<String,Object> retMap= Maps.newHashMap();
        try {
            if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
                byte[] requestbytes=getRequestJson(request);
                retMap.putAll(gson.fromJson(new String(requestbytes, StandardCharsets.UTF_8),new TypeToken<Map<String,Object>>(){}.getType()));
            } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                Map<String,String[]> reqMap=request.getParameterMap();
                if(!CollectionUtils.isEmpty(reqMap)){
                    reqMap.entrySet().forEach(f->retMap.put(f.getKey(),f.getValue()[0]));
                }
            } else if(StringUtils.startsWithIgnoreCase(request.getContentType(),MediaType.MULTIPART_FORM_DATA_VALUE)) {
                //文件上传,记录本地路径
                if(DefaultMultipartHttpServletRequest.class.isAssignableFrom(request.getClass())){
                    DefaultMultipartHttpServletRequest multipartRequest= (DefaultMultipartHttpServletRequest) request;
                    Map<String,MultipartFile> multiValueMap= multipartRequest.getFileMap();
                    Iterator<Map.Entry<String,MultipartFile>> iterator=multiValueMap.entrySet().iterator();
                    while(iterator.hasNext()){
                        Map.Entry<String,MultipartFile> entry=iterator.next();
                        if(!ObjectUtils.isEmpty(entry.getValue().getOriginalFilename())){
                            retMap.put(entry.getValue().getOriginalFilename(),entry.getValue().getSize());
                        }
                    }
                }else{
                    throw new UnsupportedOperationException("only spring multipart file supported!");
                }
            }else{
                Map<String,String[]> reqMap=request.getParameterMap();
                if(!CollectionUtils.isEmpty(reqMap)){
                    reqMap.entrySet().forEach(f->retMap.put(f.getKey(),f.getValue()[0]));
                }
            }
        }catch (IOException ex){
            throw new OperationNotSupportException("operating failed "+ex.getMessage());
        }
        return retMap;
    }
    public static void renderResponse(HttpServletResponse response,String content){
        try{
            PrintWriter writer=response.getWriter();
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            writer.write(content);
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }
    }
    private static byte[] getRequestJson(HttpServletRequest request) throws IOException{
        try(InputStream inputStream=request.getInputStream();
            ByteArrayOutputStream outputStream=new ByteArrayOutputStream()){
            IOUtils.copyBytes(inputStream,outputStream,1024);
            return outputStream.toByteArray();
        }
    }
}
