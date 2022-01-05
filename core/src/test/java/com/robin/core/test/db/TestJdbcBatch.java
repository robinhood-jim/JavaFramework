package com.robin.core.test.db;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
public class TestJdbcBatch extends TestCase {
    @Test
    public void doTestBatch(){

        int maxrow=500000;
        DataCollectionMeta collectionMeta=new DataCollectionMeta();
        collectionMeta.addColumnMeta("name", Const.META_TYPE_STRING,null);
        collectionMeta.addColumnMeta("code",Const.META_TYPE_STRING,null);
        collectionMeta.addColumnMeta("amount",Const.META_TYPE_DOUBLE,null);
        collectionMeta.addColumnMeta("creator",Const.META_TYPE_INTEGER,null);
        collectionMeta.addColumnMeta("time",Const.META_TYPE_TIMESTAMP,null);

        Iterator<Map<String,String>> iterator=new Iterator<Map<String, String>>() {
            Map<String,String> rowMap=new HashMap<>();
            int pos=0;
            Random random=new Random(123123123L);
            NumberFormat format=new DecimalFormat("#.##");
            @Override
            public boolean hasNext() {
                return pos<maxrow;
            }

            @Override
            public Map<String, String> next() {
                mockRow();
                pos++;
                return rowMap;
            }
            private void mockRow(){
                rowMap.clear();
                //rowMap.put("id",String.valueOf(pos));
                rowMap.put("name", StringUtils.generateRandomChar(6));
                rowMap.put("code",StringUtils.generateRandomChar(6));
                rowMap.put("amount",format.format(random.nextDouble()*1000));
                rowMap.put("time",String.valueOf(System.currentTimeMillis()));
                rowMap.put("creator",String.valueOf(random.nextInt(100)));
            }
        };
        JdbcDao dao=SpringContextHolder.getBean(JdbcDao.class);

        dao.batchUpdateWithRowIterator("insert into t_batch_test (name,code,amount,creator,time) values (?,?,?,?,?)",iterator,collectionMeta,10000);

    }
}
