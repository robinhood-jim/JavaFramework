package com.robin.test;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;

import com.robin.core.fileaccess.util.ApacheVfsResourceAccessUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.query.extractor.ResultSetOperationExtractor;

public class TestJsonGen {
	public static void main(String[] args){
		DataBaseParam param=new DataBaseParam("172.16.200.218",0,"wisdombus2.0_basedata","wisdombus","MiCUWcYcJI2EcM1k");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("MySql", param);
		Connection conn=null;
		try{
			DataCollectionMeta colmeta=new DataCollectionMeta();
			colmeta.addColumnMeta("id",Const.META_TYPE_BIGINT,null);
			colmeta.addColumnMeta("line_code",Const.META_TYPE_INTEGER,null);
			colmeta.addColumnMeta("line_name",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("tdate",Const.META_TYPE_TIMESTAMP,null);
			//colmeta.addColumnMeta("up_line_mile",Const.META_TYPE_DOUBLE,null);

			conn=SimpleJdbcDao.getConnection(meta, param);
			
			//List<Map<String, String>> list=SimpleJdbcDao.queryString(conn, "select info_id,url,title,content from shw_internet_info_dtl");
			ApacheVfsResourceAccessUtil util=new ApacheVfsResourceAccessUtil();
			Map<String, Object> ftpparam=new HashMap<String, Object>();
			ftpparam.put("hostName", "172.16.200.62");
			ftpparam.put("protocol", "sftp");
			ftpparam.put("port", 22);
			ftpparam.put("userName", "luoming");
			ftpparam.put("password", "123456");
			//ftpparam.put(Const.AVRO_SCHEMA_CONTENT_PARAM, "{\"namespace\":\"com.robin.avro\",\"name\":\"Content\",\"type\":\"record\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"content\",\"type\":\"string\"}]}");
			colmeta.setResourceCfgMap(ftpparam);
			colmeta.setPath("/tmp/luoming/testdata/test1.avro.gz");
			colmeta.setEncode("UTF-8");
			
			//LocalResourceAccessUtils util=new LocalResourceAccessUtils();
			//BufferedWriter writer=util.getOutResourceByWriter(colmeta);
			OutputStream stream=util.getOutResourceByStream(colmeta);
			//GsonFileWriter jwriter=new GsonFileWriter(colmeta, writer);
			final AbstractFileWriter jwriter=TextFileWriterFactory.getFileWriterByType(Const.FILETYPE_AVRO, colmeta,stream);
			System.out.println(new Date());
			jwriter.beginWrite();
			ResultSetOperationExtractor extractor=new ResultSetOperationExtractor() {
				@Override
				public void init() {
					
				}
				@Override
				public boolean executeAddtionalOperation(Map<String, Object> map,
						String[] columnName, String[] typeName, String[] className)
						throws SQLException {
					try{
						map.put("tdate",((Timestamp)map.get("eff_start_time")).getTime());
						jwriter.writeRecord(map);
					}catch(Exception ex){
						ex.printStackTrace();
						throw new SQLException(ex);
					}
					return true;
				}
			};
			SimpleJdbcDao.executeOperationWithQuery(conn, "select uuid as id,line_code,line_name,eff_start_time,up_line_mile from comm_line", extractor);
			/*for (Map<String, String> map:list) {
				jwriter.writeRecord(map);
			}*/
			jwriter.flush();
			jwriter.finishWrite();
			jwriter.close();
			System.out.println(new Date());
			System.in.read();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
