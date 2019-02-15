package com.robin.test;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ApacheVfsResourceAccessUtil;
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
			Map<String, Object> ftpparam=new HashMap<String, Object>();
			ftpparam.put("hostName", "172.16.200.62");
			ftpparam.put("protocol", "sftp");
			ftpparam.put("port", 22);
			ftpparam.put("userName", "luoming");
			ftpparam.put("password", "123456");
			colmeta.setResourceCfgMap(ftpparam);
			colmeta.setPath("/tmp/luoming/testdata/test1.avro.gz");
			colmeta.setEncode("UTF-8");
			//ftpparam.put("schemaContent", "{\"namespace\":\"com.robin.avro\",\"name\":\"Content\",\"type\":\"record\",\"fields\":[{\"name\":\"info_id\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"content\",\"type\":\"string\"}]}");
			List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			ApacheVfsResourceAccessUtil util=new ApacheVfsResourceAccessUtil();
			//BufferedReader reader=util.getInResourceByReader(colmeta);//new BufferedReader(new FileReader(new File("e:/test1.data")));
			InputStream reader=util.getInResourceByStream(colmeta);
			AbstractFileIterator jreader=TextFileIteratorFactory.getProcessIteratorByType(Const.FILETYPE_AVRO, colmeta, reader);
			while(jreader.hasNext()){
				Map<String,Object> map=jreader.next();
				logger.info("{}",map);
				list.add(map);
			}
			System.out.println(list);
			System.out.println(list.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
