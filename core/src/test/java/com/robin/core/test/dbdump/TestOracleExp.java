package com.robin.core.test.dbdump;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.dbexp.DataBaseExportParam;
import com.robin.core.dbexp.OracleDataBaseExportor;

public class TestOracleExp {
	public static void main(String[] args) {
		try{
			DataBaseParam param=new DataBaseParam("192.168.143.189", 0, "twdb", "etl", "Etl987");
			BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("Oracle", param);
			DataBaseExportParam exParam=new DataBaseExportParam();
			exParam.setDumpPath(args[0]);
			exParam.setExecuteSql(args[1]);
			exParam.setSplit(args[2]);
			exParam.setRows(10000);
			exParam.setEncode("UTF8");
			OracleDataBaseExportor exportor=new OracleDataBaseExportor();
			exportor.exportToLocal(exParam, param);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
}
