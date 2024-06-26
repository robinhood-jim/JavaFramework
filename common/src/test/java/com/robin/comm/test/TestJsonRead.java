package com.robin.comm.test;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
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
			ftpparam.put("hostName", "localhost");
			ftpparam.put("protocol", "sftp");
			ftpparam.put("port", 22);
			ftpparam.put("userName", "root");
			ftpparam.put("password", "root");
			colmeta.setResourceCfgMap(ftpparam);
			colmeta.setPath("/tmp/robin/testdata/test1.avro.gz");
			colmeta.setEncode("UTF-8");
			colmeta.setResType(ResourceConst.ResourceType.TYPE_SFTPFILE.getValue());
			//ftpparam.put("schemaContent", "{\"namespace\":\"com.robin.avro\",\"name\":\"Content\",\"type\":\"record\",\"fields\":[{\"name\":\"info_id\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"content\",\"type\":\"string\"}]}");
			List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			AbstractFileSystemAccessor util= FileSystemAccessorFactory.getResourceAccessorByType(colmeta.getResType());
			//BufferedReader reader=util.getInResourceByReader(colmeta);//new BufferedReader(new FileReader(new File("e:/test1.data")));
			InputStream reader=util.getInResourceByStream(colmeta,colmeta.getPath() );
			IResourceIterator jreader=TextFileIteratorFactory.getProcessIteratorByType(colmeta, reader);
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
