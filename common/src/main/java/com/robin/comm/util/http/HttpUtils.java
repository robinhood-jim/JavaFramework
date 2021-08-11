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
package com.robin.comm.util.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
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
    static PoolingHttpClientConnectionManager manager;
    static HttpRequestRetryHandler handler;
    static {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(50);
        manager.setDefaultMaxPerRoute(20);
        handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 3){
                    //重试超过3次,放弃请求
                    log.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException){
                    //服务器没有响应,可能是服务器断开了连接,应该重试
                    log.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException){
                    // SSL握手异常
                    log.error("SSL hand shake exception");
                    return false;
                }
                if (e instanceof InterruptedIOException){
                    //超时
                    log.error("InterruptedIOException");
                    return false;
                }
                if (e instanceof UnknownHostException){
                    // 服务器不可达
                    log.error("server host unknown");
                    return false;
                }
                if (e instanceof ConnectTimeoutException){
                    // 连接超时
                    log.error("Connection Time out");
                    return false;
                }
                if (e instanceof SSLException){
                    log.error("SSLException");
                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)){
                    //如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }
        };
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
    public static CloseableHttpClient createHttpClient(){
        return HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
    }

    public static final Response doPost(String url,String data, String charset, Map<String,String> headerMap){
        CloseableHttpClient httpClient= createHttpClient();
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
        CloseableHttpClient httpClient= createHttpClient();
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
        CloseableHttpClient httpClient= createHttpClient();
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
        CloseableHttpClient httpClient= createHttpClient();
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
        CloseableHttpClient httpClient= createHttpClient();
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
