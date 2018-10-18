package com.robin.test;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.FtpResourceAccessUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.query.extractor.ResultSetOperationExtractor;

public class TestJsonGen {
	public static void main(String[] args){
		DataBaseParam param=new DataBaseParam("192.168.147.93",0,"wi","root","123456");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("MySql", param);
		Connection conn=null;
		try{
			DataCollectionMeta colmeta=new DataCollectionMeta();
			colmeta.addColumnMeta("infoId",Const.META_TYPE_BIGINT,null);
			colmeta.addColumnMeta("url",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("title",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("content",Const.META_TYPE_STRING,null);
			conn=SimpleJdbcDao.getConnection(meta, param);
			
			//List<Map<String, String>> list=SimpleJdbcDao.queryString(conn, "select info_id,url,title,content from shw_internet_info_dtl");
			FtpResourceAccessUtil util=new FtpResourceAccessUtil();
			Map<String, Object> ftpparam=new HashMap<String, Object>();
			ftpparam.put("hostName", "192.168.143.189");
			ftpparam.put("protocol", "sftp");
			ftpparam.put("port", 22);
			ftpparam.put("userName", "talkyun");
			ftpparam.put("password", "talkyun");
			ftpparam.put("schemaContent", "{\"namespace\":\"com.robin.avro\",\"name\":\"Content\",\"type\":\"record\",\"fields\":[{\"name\":\"info_id\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"content\",\"type\":\"string\"}]}");
			colmeta.setResourceCfgMap(ftpparam);
			colmeta.setPath("/home/talkyun/ggj/test1.gz");
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
						jwriter.writeRecord(map);
					}catch(Exception ex){
						ex.printStackTrace();
						throw new SQLException(ex);
					}
					return true;
				}
			};
			SimpleJdbcDao.executeOperationWithQuery(conn, "select info_id,url,title,content from shw_internet_info_dtl", extractor);
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
