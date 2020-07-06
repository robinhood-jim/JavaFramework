package com.robin.comm.util.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Http Client Utils
 *
 * @author robinjim
 * @version 1.0
 */
@Slf4j
public class HttpUtils {
    static PoolingHttpClientConnectionManager manager=new PoolingHttpClientConnectionManager();

    static {
        manager.setMaxTotal(50);
        manager.setDefaultMaxPerRoute(20);
    }
    @Data
    public static class Response{
        private int statusCode;
        private String responseData;
        private byte[] responseBytes;
        public Response(int statusCode, String respData){
            this.statusCode = statusCode;
            this.responseData = respData;
        }
        public Response(int statusCode, byte[] respData){
            this.statusCode = statusCode;
            this.responseBytes = respData;
        }
    }
    public static final Response doPost(String url,String data, String charset, Map<String,String> headerMap){
        CloseableHttpClient httpClient= HttpClients.custom().setConnectionManager(manager).build();
        HttpPost post=new HttpPost(url);
        fillHeader(post,headerMap);
        try {
            if (data != null && !"".equals(data)) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(data, charset);
                } catch (UnsupportedCharsetException e) {
                    throw new RuntimeException("Create entity error: UnsupportedCharsetException for " + charset);
                }
                post.setEntity(entity);
            }
            return getResponseData(httpClient, post, charset);
        }catch (Exception ex){
            log.error("{}",ex);
        }finally {
            post.releaseConnection();
        }
        return null;
    }
    public static final Response doPost(String url,String charset,Map<String,String> postData,Map<String,String> headerMap){
        CloseableHttpClient httpClient= HttpClients.custom().setConnectionManager(manager).build();
        HttpPost post=new HttpPost(url);
        fillHeader(post,headerMap);
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if (postData != null && !postData.isEmpty()) {
                Iterator<Map.Entry<String, String>> iter = postData.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                post.setEntity(new UrlEncodedFormEntity(params, charset));
            }
            return getResponseData(httpClient,post,charset);
        }catch (Exception ex){
            log.error("{}",ex);
        }finally {
            post.releaseConnection();
        }
        return null;
    }
    public static final Response doGet(String url,String charset,Map<String,String> headerMap){
        CloseableHttpClient httpClient= HttpClients.custom().setConnectionManager(manager).build();
        HttpGet get=new HttpGet(url);
        fillHeader(get,headerMap);
        try {
            return  getResponseData(httpClient,get,charset);
        }catch (Exception ex){
            log.error("{}",ex);
        }finally {
            get.releaseConnection();
        }
        return null;
    }
    public static final Response doPut(String url,String data,String charset,Map<String,String> headerMap){
        CloseableHttpClient httpClient= HttpClients.custom().setConnectionManager(manager).build();
        HttpPut put=new HttpPut(url);
        fillHeader(put,headerMap);
        try {
            if (data != null && !"".equals(data)) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(data, charset);
                } catch (UnsupportedCharsetException e) {
                    throw new RuntimeException("Create entity error: UnsupportedCharsetException for " + charset);
                }
                put.setEntity(entity);
            }
            return getResponseData(httpClient, put, charset);
        }catch (Exception ex){
            log.error("{}",ex);
        }finally {
            put.releaseConnection();
        }
        return null;
    }
    public static final Response doDelete(String url,String charset,Map<String,String> headerMap){
        CloseableHttpClient httpClient= HttpClients.custom().setConnectionManager(manager).build();
        HttpDelete delete=new HttpDelete(url);
        fillHeader(delete,headerMap);
        try {
            return getResponseData(httpClient, delete, charset);
        }catch (Exception ex){
            log.error("{}",ex);
        }finally {
            delete.releaseConnection();
        }
        return null;
    }
    private static void fillHeader(HttpRequestBase requestBase,Map<String,String> headerMap){
        if(headerMap!=null && !headerMap.isEmpty()){
            for (Map.Entry<String,String> headerKey : headerMap.entrySet()){
                requestBase.addHeader(headerKey.getKey(), headerKey.getValue());
            }
        }
    }
    private static Response getResponseData(CloseableHttpClient httpClient,HttpRequestBase requestBase,String charset){
        HttpResponse response = null;
        try {
            response = httpClient.execute(requestBase);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("Execute post error");
        } catch (IOException e) {
            throw new RuntimeException("Execute post error");
        }catch (Exception ex){
            throw ex;
        }
        finally {
        }
        String responseData = null; //response string
        if (response.getStatusLine().getStatusCode()==200){//response ok
            HttpEntity respEntity = response.getEntity();
            try {
                if (!StringUtils.isEmpty(charset)){
                    responseData = EntityUtils.toString(respEntity, charset);
                }else{
                    responseData = EntityUtils.toString(respEntity);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            responseData = "Encounter error Status："+response.getStatusLine().getStatusCode();
        }
        return new Response(response.getStatusLine().getStatusCode(), responseData);
    }

}
