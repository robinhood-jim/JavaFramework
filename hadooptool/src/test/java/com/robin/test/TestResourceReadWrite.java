package com.robin.test;

import com.qiniu.storage.Region;
import com.robin.comm.fileaccess.fs.QiniuFileSystemAccessor;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.query.extractor.ResultSetExtractorUtils;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
public class TestResourceReadWrite extends TestCase {
	@Test
	public void testWrite(){
		DataBaseParam param=new DataBaseParam("127.0.0.1",3316,"test","root","root");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
		try(Connection conn=SimpleJdbcDao.getConnection(meta)){
			DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
			builder.addColumn("param_sn",Const.META_TYPE_INTEGER,null);
			builder.addColumn("weixin_order_id",Const.META_TYPE_STRING,null);
			builder.addColumn("open_id",Const.META_TYPE_STRING,null);
			builder.addColumn("total_money",Const.META_TYPE_INTEGER,null);
			builder.addColumn("subscribe_id",Const.META_TYPE_STRING,null);
			builder.addColumn("product_name",Const.META_TYPE_STRING,null);
			//builder.addColumn("start_time",Const.META_TYPE_TIMESTAMP,null);

			builder.resourceCfg("hostName", "127.0.0.1").resourceCfg("protocol", "ftp")
					.resourceCfg("file.useAvroEncode","true")
					.resourceCfg("port", 21).resourceCfg("userName", "test").resourceCfg("password", "test").fileFormat(Const.FILEFORMATSTR.PROTOBUF.getValue())
							.resPath("tmp/test2.proto.lzma").protocol(Const.VFS_PROTOCOL.FTP.getValue());
			QiniuFileSystemAccessor.Builder builder1=new QiniuFileSystemAccessor.Builder();
			ResourceBundle bundle=ResourceBundle.getBundle("qiniu");

			builder1.domain(bundle.getString("domain")).region(Region.autoRegion()).bucket(bundle.getString("bucket"))
					.accessKey(bundle.getString("accessKey")).secretKey(bundle.getString("secretKey"));
			QiniuFileSystemAccessor accessor=builder1.build();
			DataCollectionMeta colmeta=builder.build();
			final AbstractFileWriter jwriter=(AbstractFileWriter) TextFileWriterFactory.getWriterByType(colmeta,accessor);

			System.out.println(new Date());
			jwriter.beginWrite();

			SimpleJdbcDao.executeOperationWithHandler(conn,"select param_sn,weixin_order_id,open_id,total_money,subscribe_id,product_name from t_business_bill_detail",false, rs -> {
				int pos=0;
				try {
					Map<String, Object> map = new HashMap<>();
					while (rs.next()) {
						map.clear();
						ResultSetExtractorUtils.wrapResultSetToMap(rs, "UTF-8", map);
						//map.put("tdate",((Timestamp)map.get("start_time")).getTime());
						jwriter.writeRecord(map);
						pos++;
					}
				}catch (IOException | OperationNotSupportedException ex){
					log.error("{}",ex);
				}
				return pos;
			});
			//SimpleJdbcDao.executeOperationWithQuery(conn, "select param_sn,weixin_order_id,open_id,total_money,subscribe_id,product_name from t_business_bill_detail",false, extractor);

			jwriter.flush();
			jwriter.finishWrite();
			jwriter.close();
			System.out.println(new Date());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@Test
	public void testReadFromQiniu(){

	}
	@Test
	public void testRead(){
		DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();

		builder.resourceCfg("hostName", "127.0.0.1").resourceCfg("protocol", "ftp")
				.resourceCfg("port", 21).resourceCfg("userName", "test").resourceCfg("password", "test").fileFormat(Const.FILEFORMATSTR.PARQUET.getValue())
				.resPath("/tmp/test1.parquet.snappy").protocol(Const.VFS_PROTOCOL.FTP.getValue()).fsType(Const.FILESYSTEM.VFS.getValue());

		DataCollectionMeta colmeta = builder.build();
		try(IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(colmeta)){
			while (iterator.hasNext()){
				log.info("get record {}",iterator.next());
			}
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}

}
