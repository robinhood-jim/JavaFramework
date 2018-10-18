package com.robin.core.test;

import java.util.List;

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.dbimp.DataBaseImportParam;
import com.robin.core.dbimp.OracleDataBaseImportor;

/**
 * <p>Project:  core</p>
 *
 * <p>Description:TestOracleImp.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月23日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class TestOracleImp {
	
	public static void main(String[] args) {
		try{
			DataBaseParam param=new DataBaseParam("192.168.143.189", 0, "twdb", "etl", "Etl987");
			BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("Oracle", param);
			DataBaseImportParam inparam=new DataBaseImportParam();
			List<DataBaseColumnMeta> list=DataBaseUtil.getTableMetaByTableName(SimpleJdbcDao.getConnection(meta, param), args[0], "ETL", meta);
			StringBuilder builder=new StringBuilder();
			for (DataBaseColumnMeta colmeta:list) {
				builder.append(colmeta.getColumnName()).append(",");
			}
			inparam.setTableName(args[1]);
			inparam.setFilePath(args[2]);
			inparam.setSplit(args[3]);
			inparam.setScriptPath("/tmp/run.ctl");
			inparam.setFields(builder.substring(0,builder.length()-1));
			inparam.setEncode("UTF8");
			OracleDataBaseImportor importor=new OracleDataBaseImportor();
			importor.importFromLocal(inparam, param);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
