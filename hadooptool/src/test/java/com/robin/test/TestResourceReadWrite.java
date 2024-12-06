package com.robin.test;

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
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
@Slf4j
public class TestResourceReadWrite extends TestCase {
	@Test
	public void testWrite(){
		DataBaseParam param=new DataBaseParam("127.0.0.1",3316,"test","root","root");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
		try(Connection conn=SimpleJdbcDao.getConnection(meta)){
			DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
			builder.addColumn("id",Const.META_TYPE_BIGINT,null);
			builder.addColumn("line_code",Const.META_TYPE_STRING,null);
			builder.addColumn("line_name",Const.META_TYPE_STRING,null);
			builder.addColumn("tdate",Const.META_TYPE_BIGINT,null);

			builder.resourceCfg("hostName", "127.0.0.1").resourceCfg("protocol", "ftp")
					.resourceCfg("port", 21).resourceCfg("userName", "test").resourceCfg("password", "test").fileFormat(Const.FILEFORMATSTR.PARQUET.getValue())
							.resPath("/tmp/test1.parquet.snappy").protocol(Const.VFS_PROTOCOL.FTP.getValue()).fsType(Const.FILESYSTEM.VFS.getValue());

			DataCollectionMeta colmeta=builder.build();
			final AbstractFileWriter jwriter=(AbstractFileWriter) TextFileWriterFactory.getWriterByType(colmeta);

			System.out.println(new Date());
			jwriter.beginWrite();
			ResultSetOperationExtractor extractor=new ResultSetOperationExtractor() {

				@Override
				public boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd)
						throws SQLException {
					try{
						map.put("tdate",((Timestamp)map.get("start_time")).getTime());
						jwriter.writeRecord(map);
					}catch(Exception ex){
						ex.printStackTrace();
						throw new SQLException(ex);
					}
					return true;
				}
			};
			SimpleJdbcDao.executeOperationWithQuery(conn, "select id,line_code,line_name,start_time from test_line",false, extractor);

			jwriter.flush();
			jwriter.finishWrite();
			jwriter.close();
			System.out.println(new Date());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@Test
	public void testRead(){
		DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();
		//builder.addColumn("id", Const.META_TYPE_BIGINT, null);
		//builder.addColumn("line_code", Const.META_TYPE_STRING, null);
		//builder.addColumn("line_name", Const.META_TYPE_STRING, null);
		//builder.addColumn("tdate", Const.META_TYPE_BIGINT, null);

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
