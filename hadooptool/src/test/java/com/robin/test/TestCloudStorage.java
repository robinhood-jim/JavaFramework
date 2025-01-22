package com.robin.test;

import com.robin.comm.fileaccess.fs.MinioFileSystemAccessor;
import com.robin.comm.sql.FilterSqlParser;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.dfs.minio.MinioUtils;
import io.minio.MinioAsyncClient;
import io.minio.RemoveObjectArgs;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestCloudStorage extends TestCase {
    @Test
    public void testDeleteMinion() throws Exception{
        MinioAsyncClient.Builder builder=MinioAsyncClient.builder().endpoint("http://36.158.32.29:18888")
                .credentials("jeason","Jeason@1234");
        MinioAsyncClient client=builder.build();
        client.removeObject(RemoveObjectArgs.builder().bucket("test").object("tmp/bigdata5.parquet.gz").build()).join();



    }
    @Test
    public void testWriteToMinio(){
        DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
        builder.addColumn("id", Const.META_TYPE_BIGINT,null);
        builder.addColumn("name",Const.META_TYPE_STRING,null);
        builder.addColumn("description",Const.META_TYPE_STRING,null);
        builder.addColumn("sno",Const.META_TYPE_INTEGER,null);
        builder.addColumn("price",Const.META_TYPE_DOUBLE,null);
        builder.addColumn("amount",Const.META_TYPE_INTEGER,null);
        builder.addColumn("type",Const.META_TYPE_INTEGER,null);

        builder.resourceCfg(ResourceConst.PARQUETFILEFORMAT,ResourceConst.PARQUETSUPPORTFORMAT.PROTOBUF.getValue()).fileFormat(Const.FILEFORMATSTR.CSV.getValue())
                .resPath("/tmp/bigdata3.csv.gz").resourceCfg("protocol",Const.VFS_PROTOCOL.FTP.getValue())
                .resourceCfg("hostName","127.0.0.1").resourceCfg("userName","test").resourceCfg("password","test").protocol(Const.VFS_PROTOCOL.FTP.getValue()).fsType(Const.FILESYSTEM.VFS.getValue())
                //.resourceCfg(ResourceConst.USEASYNCUPLOAD,"true")
                .resourceCfg(ResourceConst.DEFAULTCACHEOFFHEAPSIZEKEY,1000*1000*6);
        ResourceBundle bundle=ResourceBundle.getBundle("minio");
        DataCollectionMeta colmeta=builder.build();
        /*MinioFileSystemAccessor.Builder builder1=new MinioFileSystemAccessor.Builder();
        MinioFileSystemAccessor accessor=builder1.accessKey(bundle.getString("minio.accessKey")).secretKey(bundle.getString("minio.secretKey")).endpoint(bundle.getString("minio.endpoint"))
                .bucket("test").build();*/


        try (AbstractFileWriter jwriter = (AbstractFileWriter) TextFileWriterFactory.getWriterByType(colmeta)){
            System.out.println(new Date());
            jwriter.beginWrite();
            Map<String, Object> recMap = new HashMap<>();
            Random random = new Random(123123123123L);
            Map<Integer,Double> priceMap=new HashMap<>();
            for (int i=1;i<11;i++){
                priceMap.put(i,i*10.0);
            }

            for (int i = 0; i < 10000; i++) {
                recMap.put("id", Long.valueOf(i));
                recMap.put("name", StringUtils.generateRandomChar(32));
                recMap.put("description", StringUtils.generateRandomChar(32));
                recMap.put("sno",random.nextInt(10)+1);
                recMap.put("amount",random.nextInt(50)+1);
                recMap.put("price",priceMap.get((Integer) recMap.get("sno")));
                recMap.put("type",random.nextInt(2));
                jwriter.writeRecord(recMap);
            }
            jwriter.flush();
            jwriter.finishWrite();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void testReadFromMinio(){
        DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
        builder.addColumn("id",Const.META_TYPE_BIGINT,null);
        builder.addColumn("name",Const.META_TYPE_STRING,null);
        builder.addColumn("description",Const.META_TYPE_STRING,null);
        builder.addColumn("sno",Const.META_TYPE_INTEGER,null);
        builder.addColumn("price",Const.META_TYPE_DOUBLE,null);
        builder.addColumn("amount",Const.META_TYPE_INTEGER,null);
        builder.addColumn("type",Const.META_TYPE_INTEGER,null);

        builder.resourceCfg(ResourceConst.PARQUETFILEFORMAT,ResourceConst.PARQUETSUPPORTFORMAT.AVRO.getValue())
                //.resourceCfg(ResourceConst.STORAGEFILTERSQL,"select name,sno,type,price*amount as totalFee from test where price*amount>500 and sno<7 and name like 'A%'")
                .resourceCfg(ResourceConst.STORAGEFILTERSQL,"select type,sno,sum(price*amount) as totalFee from test where price*amount>500 group by sno,type having sum(price*amount)>100000.0")
                .fileFormat(Const.FILEFORMATSTR.CSV.getValue()).tableName("test")
                .resPath("tmp/bigdata3.csv.gz");
        ResourceBundle bundle=ResourceBundle.getBundle("minio");
        DataCollectionMeta colmeta=builder.build();
        MinioFileSystemAccessor.Builder builder1=new MinioFileSystemAccessor.Builder();
        MinioFileSystemAccessor accessor=builder1.accessKey(bundle.getString("minio.accessKey")).secretKey(bundle.getString("minio.secretKey")).endpoint(bundle.getString("minio.endpoint"))
                .bucket("test").build();
        try(AbstractFileIterator iterator=(AbstractFileIterator) TextFileIteratorFactory.getProcessIteratorByType(colmeta,accessor)){
            int pos=0;
            while (iterator.hasNext()){
                System.out.println(iterator.next());
                pos+=1;
            }
            System.out.println("total rows "+pos);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Test
    public void uploadFile(){
        ResourceBundle bundle=ResourceBundle.getBundle("minio");
        MinioAsyncClient.Builder builder= MinioAsyncClient.builder().endpoint(bundle.getString("minio.endpoint")).credentials(bundle.getString("minio.accessKey"),bundle.getString("minio.secretKey"));
        MinioAsyncClient client=builder.build();
        try(FileInputStream inputStream=new FileInputStream("e:/SF000010595188.pdf");){
            //download(client,"20240510/SF000010715871.pdf",outputStream);
            MinioUtils.putBucket(client,"test","tmp/SF000010595188.pdf",inputStream, Files.size(Paths.get("e:/SF000010595188.pdf")),"application/pdf");

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    @Test
    public void test1(){
        DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
        builder.addColumn("id",Const.META_TYPE_BIGINT,null);
        builder.addColumn("name",Const.META_TYPE_STRING,null);
        builder.addColumn("description",Const.META_TYPE_STRING,null);
        builder.addColumn("sno",Const.META_TYPE_INTEGER,null);
        builder.addColumn("price",Const.META_TYPE_DOUBLE,null);
        FilterSqlParser.FilterSqlResult result= FilterSqlParser.doParse(builder.build(),"id>4097 and price*4>500 and name like 'A%'");
        Map<String,Object> valueMap=new HashMap<>();
        valueMap.put("id",5000);
        valueMap.put("name","A888");
        valueMap.put("price",200.0);
        FilterSqlParser.walkTree(result,result.getRootNode(),valueMap);

    }
}
