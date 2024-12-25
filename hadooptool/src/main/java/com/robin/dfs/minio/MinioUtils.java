package com.robin.dfs.minio;


import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ResourceBundle;

@Slf4j
public class MinioUtils {
    private MinioUtils(){

    }
    public static boolean bucketExists(MinioAsyncClient client,String bucketName){
        boolean found=false;
        try{
            found=client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()).get();
        }catch (Exception e){
            log.error("{}",e);
        }
        return found;
    }

    public static boolean putBucket(MinioAsyncClient client,String bucketName, String objectName, InputStream inputStream,long fileSize,String contentType){
        try{
            PutObjectArgs args= PutObjectArgs.builder().bucket(bucketName).object(objectName)
                    .stream(inputStream,fileSize,-1).contentType(contentType).build();
            client.putObject(args);
        }catch (Exception ex){
            log.error("{}",ex);
            return false;
        }
        return true;
    }
    public static boolean exists(MinioAsyncClient client,String bucketName,String key){
        try{
            client.statObject(StatObjectArgs.builder().bucket(bucketName).object(key).build());
            return true;
        }catch (Exception ex){
            return false;
        }
    }
    public static long size(MinioAsyncClient client,String bucketName,String key){
        try{
            StatObjectResponse response=client.statObject(StatObjectArgs.builder().bucket(bucketName).object(key).build()).get();
            return response.size();
        }catch (Exception ex){
            return 0L;
        }
    }
    public static InputStream getObject(MinioAsyncClient client,String bucketName,String objectName) throws IOException{
        try {
            GetObjectArgs args = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
            return client.getObject(args).get();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    public static boolean download(MinioAsyncClient client,String bucketName,String objectName, OutputStream outputStream){
        boolean executeOk=false;
        GetObjectArgs args= GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
        try(GetObjectResponse response1=client.getObject(args).get()){
            IOUtils.copy(response1,outputStream);
            executeOk=true;
        }catch (Exception ex){
            log.error("{}",ex);
        }
        return executeOk;
    }
    public static void download(MinioAsyncClient client,String bucketName,String objectName,String contentType,  HttpServletResponse response) throws Exception{
        GetObjectArgs args= GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
        try(GetObjectResponse response1=client.getObject(args).get()){
            int pos=objectName.lastIndexOf("/");
            String fileName=objectName.substring(pos);
            response.setCharacterEncoding("utf-8");
            response.setContentType(contentType);
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copy(response1,response.getOutputStream());
            response.getOutputStream().flush();
        }catch (Exception ex){
            throw ex;
        }
    }
    public static void main(String[] args){
        ResourceBundle bundle=ResourceBundle.getBundle("application");
        MinioClient.Builder builder= MinioClient.builder().endpoint(bundle.getString("minio.endpoint")).credentials(bundle.getString("minio.accessKey"),bundle.getString("minio.secretKey"));
        MinioClient client=builder.build();
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        try(FileInputStream inputStream=new FileInputStream("e:/SF000010595188.pdf");){
            //download(client,"20240510/SF000010715871.pdf",outputStream);
            File file=new File("e:/SF000010595188.pdf");

        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println(outputStream);
    }

}
