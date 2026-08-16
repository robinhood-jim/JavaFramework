package com.robin.test;

import com.robin.comm.fileaccess.fs.HdfsFileSystemAccessor;
import com.robin.comm.fileaccess.writer.ParquetFileWriter;
import com.robin.comm.fileaccess.writer.ProtoBufFileWriter;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.*;

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
		DataCollectionMeta.Builder builder=DataCollectionMeta.Builder.newBuilder();
		builder.fsType("LOCAL").resPath("file:///f:/bigdata4.proto")
				.addColumn("id", Const.CLASSTYPE.LONG).addColumn("name", Const.CLASSTYPE.STRING)
				.addColumn("description", Const.CLASSTYPE.STRING).addColumn("sno", Const.CLASSTYPE.INTEGER)
				.addColumn("price", Const.CLASSTYPE.DOUBLE).addColumn("amount", Const.CLASSTYPE.INTEGER).addColumn("type", Const.CLASSTYPE.INTEGER);
		DataCollectionMeta meta=builder.build();
		Random random=new Random(123123L);
		Map<String,Object> valueMap=new HashMap<>();
		try(AbstractFileWriter writer=(AbstractFileWriter) TextFileWriterFactory.getWriterByType(meta)){
			writer.beginWrite();
			for(long i=0;i<10000L;i++){
				valueMap.put("id",i);
				valueMap.put("name", StringUtils.generateRandomChar(8));
				valueMap.put("description",StringUtils.generateRandomChar(8));
				valueMap.put("sno",random.nextInt(3)+1);
				valueMap.put("price",random.nextDouble()*100);
				valueMap.put("amout",random.nextInt(100));
				valueMap.put("type",random.nextInt(3)+1);
				writer.writeRecord(valueMap);
			}
			//writer.finishWrite();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}


}
