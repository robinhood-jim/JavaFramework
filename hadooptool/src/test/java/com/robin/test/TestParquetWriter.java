package com.robin.test;

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.test</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年09月18日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class TestParquetWriter {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(TestParquetWriter.class);
        DataBaseParam param=new DataBaseParam("172.16.200.202",0,"mileage","hive","hive");
        BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
        Connection conn=null;
        try {
            DataCollectionMeta colmeta = new DataCollectionMeta();
            colmeta.addColumnMeta("id", Const.META_TYPE_BIGINT, null);
            colmeta.addColumnMeta("device_id", Const.META_TYPE_STRING, null);
            colmeta.addColumnMeta("route_no", Const.META_TYPE_BIGINT, null);
            colmeta.addColumnMeta("from_station", Const.META_TYPE_INTEGER, null);
            colmeta.addColumnMeta("to_station", Const.META_TYPE_INTEGER, null);
            colmeta.addColumnMeta("predict_value", Const.META_TYPE_INTEGER, null);
            colmeta.addColumnMeta("predict_time", Const.META_TYPE_TIMESTAMP, null);
            colmeta.setPath("/tmp/luoming/out.parquet.snappy");
            conn= SimpleJdbcDao.getConnection(meta);
           // LocalResourceAccessUtils util=new LocalResourceAccessUtils();
            //OutputStream stream=util.getOutResourceByStream(colmeta);
            final AbstractFileWriter jwriter= TextFileWriterFactory.getFileWriterByType(Const.FILETYPE_PARQUET, colmeta,new FileOutputStream("/tmp/luoming/1.txt"));

            jwriter.beginWrite();
            ResultSetOperationExtractor extractor=new ResultSetOperationExtractor() {
                @Override
                public void init() {

                }
                public boolean executeAdditionalOperation(Map<String, Object> map,
                                                         ResultSetMetaData rsmd)
                        throws SQLException {
                    try{
                        Timestamp t=(Timestamp) map.get("predict_time");
                        map.put("predict_time",t.getTime());
                        jwriter.writeRecord(map);
                    }catch(Exception ex){
                        ex.printStackTrace();
                        throw new SQLException(ex);
                    }
                    return true;
                }
            };
            SimpleJdbcDao.executeOperationWithQuery(conn, "select id,device_id,route_no,from_station,to_station,predict_value,predict_time from t_vehicle_arrive_new", extractor);
			/*for (Map<String, String> map:list) {
				jwriter.writeRecord(map);
			}*/
            jwriter.flush();
            jwriter.finishWrite();
            jwriter.close();

        }catch (Exception ex){
            ex.printStackTrace();

        }
    }
}
