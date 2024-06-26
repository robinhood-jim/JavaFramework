package com.robin.test;

import com.robin.comm.fileaccess.fs.HdfsFileSystemAccessor;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  hadooptool</p>
 *
 * <p>Description:TestResourceGen.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月30日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class TestResourceGen {
	public static void main(String[] args){
		DataBaseParam param=new DataBaseParam("192.168.147.12",0,"etlcloud_test","root","123456");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("MySql", param);
		DataBaseParam param1=new DataBaseParam("192.168.147.93",0,"wi","root","123456");
		BaseDataBaseMeta meta1=DataBaseMetaFactory.getDataBaseMetaByType("MySql", param1);
		Connection conn=null;
		Pair<BufferedWriter, OutputStream> pair=null;
		try{
			DataCollectionMeta colmeta=new DataCollectionMeta();
			colmeta.addColumnMeta("infoId",Const.META_TYPE_BIGINT,null);
			colmeta.addColumnMeta("url",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("title",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("content",Const.META_TYPE_STRING,null);
			conn=SimpleJdbcDao.getConnection(meta);
			List<Map<String, Object>> list=SimpleJdbcDao.queryString(conn, "select config_name as name,config_value as value from t_hadoop_cluster_config where cluster_id=4");
			conn=SimpleJdbcDao.getConnection(meta1);
			List<Map<String, Object>> resultlist=SimpleJdbcDao.queryString(conn, "select info_id,url,title,content from shw_internet_info_dtl");
			HdfsFileSystemAccessor util=new HdfsFileSystemAccessor();
			Map<String, Object> hdfsparam=new HashMap<String, Object>();
			for (Map<String, Object> tmap:list) {
				hdfsparam.put(tmap.get("name").toString(), tmap.get("value"));
			}
			colmeta.setResourceCfgMap(hdfsparam);
			colmeta.setPath("/testdata/test1.gz");
			colmeta.setEncode("UTF-8");
			pair=util.getOutResourceByWriter(colmeta, colmeta.getPath());
			colmeta.setFileFormat(Const.FILETYPE_JSON);
			AbstractFileWriter jwriter=(AbstractFileWriter) TextFileWriterFactory.getWriterByType(colmeta, pair.getKey());
			System.out.println(new Date());
			jwriter.beginWrite();
			for (Map<String, Object> map:resultlist) {
				jwriter.writeRecord(map);
			}
			jwriter.flush();
			jwriter.finishWrite();
			jwriter.close();
			System.out.println(new Date());
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
			try {
				if (!ObjectUtils.isEmpty(pair)) {
					pair.getKey().close();
					pair.getValue().close();
				}
			}catch (IOException ex){

			}
		}
	}

}
