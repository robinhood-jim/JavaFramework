package com.robin.test;

import com.robin.comm.fileaccess.fs.MinioFileSystemAccessor;
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

        builder.resourceCfg("file.useAvroEncode","true").fileFormat(Const.FILEFORMATSTR.PARQUET.getValue())
                .resPath("tmp/bigdata2.parquet.gz")
                //.resourceCfg(ResourceConst.USEASYNCUPLOAD,"true")
                .resourceCfg(ResourceConst.DEFAULTCACHEOFFHEAPSIZEKEY,1000*1000*6);
        DataCollectionMeta colmeta=builder.build();
        MinioFileSystemAccessor.Builder builder1=new MinioFileSystemAccessor.Builder();
        MinioFileSystemAccessor accessor=builder1.accessKey("jeason").secretKey("Jeason@1234").endpoint("http://36.158.32.29:18889")
                .bucket("test").build();
		/*QiniuFileSystemAccessor.Builder builder1=new QiniuFileSystemAccessor.Builder();
		ResourceBundle bundle=ResourceBundle.getBundle("qiniu");

		builder1.domain(bundle.getString("domain")).region(Region.autoRegion()).bucket(bundle.getString("bucket"))
				.accessKey(bundle.getString("accessKey")).urlPrefix(bundle.getString("urlPrefix")).secretKey(bundle.getString("secretKey"));
		QiniuFileSystemAccessor accessor=builder1.build();*/

        try (AbstractFileWriter jwriter = (AbstractFileWriter) TextFileWriterFactory.getWriterByType(colmeta, accessor)){
            System.out.println(new Date());
            jwriter.beginWrite();
            Map<String, Object> recMap = new HashMap<>();
            Random random = new Random(123123123123L);
            for (int i = 0; i < 5000; i++) {
                recMap.put("id", i);
                recMap.put("name", StringUtils.generateRandomChar(32));
                recMap.put("description", StringUtils.generateRandomChar(128));
                recMap.put("sno",random.nextInt(3000000));
                recMap.put("price",random.nextDouble()*1000);
                //System.out.println(recMap.get("price"));
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

        builder.resourceCfg("file.useAvroEncode","true")
                .resourceCfg(ResourceConst.STORAGEFILTERSQL,"(id>4097 and price>500)")
                .fileFormat(Const.FILEFORMATSTR.PARQUET.getValue())
                .resPath("tmp/bigdata2.parquet.gz");
        DataCollectionMeta colmeta=builder.build();
        MinioFileSystemAccessor.Builder builder1=new MinioFileSystemAccessor.Builder();
        MinioFileSystemAccessor accessor=builder1.accessKey("jeason").secretKey("Jeason@1234").endpoint("http://36.158.32.29:18888")
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
}
