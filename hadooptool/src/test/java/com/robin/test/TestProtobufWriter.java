package com.robin.test;


import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class TestProtobufWriter extends TestCase {
    private Logger logger = LoggerFactory.getLogger(TestProtobufWriter.class);
    @Test
    public void testInsert(){

        DataBaseParam param=new DataBaseParam("localhost",0,"nrd_server_dev","root","root");
        BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
        //AbstractFileWriter jwriter=null;
        try(Connection connection= SimpleJdbcDao.getConnection(meta)) {
            //List<DataBaseColumnMeta> columnMetaList= DataBaseUtil.getTableMetaByTableName(connection,"t_sys_code","nrd_server_dev",BaseDataBaseMeta.TYPE_MYSQL);
            DataCollectionMeta colmeta = new DataCollectionMeta();
            colmeta.addColumnMeta("id", Const.META_TYPE_INTEGER,null);
            colmeta.addColumnMeta("cs_id",Const.META_TYPE_INTEGER,null);
            colmeta.addColumnMeta("item_name",Const.META_TYPE_STRING,null);
            colmeta.addColumnMeta("item_value",Const.META_TYPE_STRING,null);
            colmeta.setEncode("UTF-8");
            colmeta.setFileFormat(Const.FILESUFFIX_PROTOBUF);

            colmeta.setPath("d:/tmp/luoming/1.proto.gz");
            AbstractFileSystemAccessor util= FileSystemAccessorFactory.getResourceAccessorByType(Const.FILESYSTEM.LOCAL.getValue());

            final AbstractFileWriter jwriter= (AbstractFileWriter) TextFileWriterFactory.getOutputStreamByType(colmeta,util.getOutResourceByStream(colmeta,colmeta.getPath()));
            jwriter.beginWrite();
            SimpleJdbcDao.executeOperationWithQuery(connection, "select id,cs_id,item_name,item_value from t_sys_code", null, false, new ResultSetOperationExtractor() {
                @Override
                public boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd) throws SQLException {
                    try{
                        jwriter.writeRecord(map);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    return false;
                }
            });
            try {
                if(jwriter!=null) {
                    //jwriter.flush();
                    jwriter.finishWrite();
                    jwriter.close();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Test
    public void testRead(){
        DataCollectionMeta colmeta = new DataCollectionMeta();
        colmeta.addColumnMeta("id", Const.META_TYPE_INTEGER,null);
        colmeta.addColumnMeta("cs_id",Const.META_TYPE_INTEGER,null);
        colmeta.addColumnMeta("item_name",Const.META_TYPE_STRING,null);
        colmeta.addColumnMeta("item_value",Const.META_TYPE_STRING,null);
        colmeta.setPath("d:/tmp/luoming/1.proto");
        try{
            IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByPath(colmeta,new FileInputStream("d:/tmp/luoming/1.proto"));
            while(iterator.hasNext()){
                logger.info("{}",iterator.next());
            }
        }catch (Exception ex){
            logger.error("{}",ex);
        }
    }

}
