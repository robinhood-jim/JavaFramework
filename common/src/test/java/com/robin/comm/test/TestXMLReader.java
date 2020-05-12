package com.robin.comm.test;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.LocalResourceAccessUtils;

import java.io.BufferedReader;


public class TestXMLReader {
    public static void main(String[] args){
        DataCollectionMeta colmeta=new DataCollectionMeta();
        colmeta.addColumnMeta("col1",Const.META_TYPE_BIGINT,null);
        colmeta.addColumnMeta("col2",Const.META_TYPE_STRING,null);
        colmeta.addColumnMeta("id",Const.META_TYPE_STRING,null);
        colmeta.addColumnMeta("key",Const.META_TYPE_STRING,null);
        colmeta.setEncode("UTF-8");
        colmeta.setPath("f:/test.xml");
        colmeta.setFileFormat(Const.FILESUFFIX_XML);
        BufferedReader reader=null;
        try {
            LocalResourceAccessUtils utils = new LocalResourceAccessUtils();
            AbstractFileIterator iter= TextFileIteratorFactory.getProcessIteratorByType(colmeta);
            iter.beforeProcess(colmeta.getPath());
            while(iter.hasNext()){
                System.out.println(iter.next());
            }
            iter.afterProcess();
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
