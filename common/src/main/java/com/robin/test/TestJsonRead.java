package com.robin.test;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.FtpResourceAccessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJsonRead {
	public static void main(String[] args){
		Logger logger=LoggerFactory.getLogger(TestJsonRead.class);
		try{
			DataCollectionMeta colmeta=new DataCollectionMeta();
			colmeta.addColumnMeta("infoId",Const.META_TYPE_BIGINT,null);
			colmeta.addColumnMeta("url",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("title",Const.META_TYPE_STRING,null);
			colmeta.addColumnMeta("content",Const.META_TYPE_STRING,null);
			Map<String, Object> ftpparam=new HashMap<String, Object>();
			ftpparam.put("hostName", "192.168.143.189");
			ftpparam.put("protocol", "sftp");
			ftpparam.put("port", 22);
			ftpparam.put("userName", "talkyun");
			ftpparam.put("password", "talkyun");
			colmeta.setResourceCfgMap(ftpparam);
			colmeta.setPath("/home/talkyun/ggj/test1.bz2");
			colmeta.setEncode("UTF-8");
			ftpparam.put("schemaContent", "{\"namespace\":\"com.robin.avro\",\"name\":\"Content\",\"type\":\"record\",\"fields\":[{\"name\":\"info_id\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"content\",\"type\":\"string\"}]}");
			List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			FtpResourceAccessUtil util=new FtpResourceAccessUtil();
			//BufferedReader reader=util.getInResourceByReader(colmeta);//new BufferedReader(new FileReader(new File("e:/test1.data")));
			InputStream reader=util.getInResourceByStream(colmeta);
			AbstractFileIterator jreader=TextFileIteratorFactory.getProcessIteratorByType(Const.FILETYPE_AVRO, colmeta, reader);
			while(jreader.hasNext()){
				Map<String,Object> map=jreader.next();
				logger.info("",map);
				list.add(map);
			}
			System.out.println(list.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
