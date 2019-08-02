package com.robin.comm.test;

import com.robin.comm.util.xls.*;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.*;

public class TestExcelReader {

    @Test
    public void testGenerate() throws Exception{
        Long startTs=System.currentTimeMillis()-3600*24*1000;
        String str= StringUtils.generateRandomChar(32);
        ExcelSheetProp prop=new ExcelSheetProp();
        prop.setFileext("xlsx");
        prop.setStartCol(1);
        prop.setStartRow(1);
        prop.setSheetName("test");
        prop.addColumnProp(new ExcelColumnProp("name","name",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("time","time",Const.META_TYPE_TIMESTAMP,false));
        prop.addColumnProp(new ExcelColumnProp("intcol","intcol",Const.META_TYPE_INTEGER,false));
        prop.addColumnProp(new ExcelColumnProp("dval","dval",Const.META_TYPE_DOUBLE,false));
        TableHeaderProp header=new TableHeaderProp();
        header.setContainrow(1);
        List<Map<String,String>> list=new ArrayList<Map<String, String>>();
        Random random=new Random(12312321321312L);
        for(int i=0;i<1000;i++){
            Map<String,String> map=new HashMap<String, String>();
            map.put("name",StringUtils.generateRandomChar(12));
            map.put("time",String.valueOf(startTs+i*1000));
            map.put("intcol",String.valueOf(random.nextInt(1000)));
            map.put("dval",String.valueOf(random.nextDouble()*1000));
            list.add(map);
        }
        prop.setColumnList(list);
        Workbook wb=ExcelGenerator.GenerateExcelFile(prop,header);
        FileOutputStream out=new FileOutputStream("d:/test1.xlsx");
        wb.write(out);
        out.close();
    }
    @Test
    public void testWithQuery(){
        ExcelSheetProp prop=new ExcelSheetProp();
        prop.setFileext("xlsx");
        prop.setStartCol(1);
        prop.setStartRow(1);
        prop.setSheetName("TestSheet1");
        prop.addColumnProp(new ExcelColumnProp("唯一键","uuid",Const.META_TYPE_BIGINT,false));
        prop.addColumnProp(new ExcelColumnProp("企业id","corpId",Const.META_TYPE_BIGINT,false));
        prop.addColumnProp(new ExcelColumnProp("车牌号","carNum",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("车架号","vin",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("品牌","brand",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("修改时间","modifier",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("颜色","color",Const.META_TYPE_BIGINT,false));
        prop.addColumnProp(new ExcelColumnProp("生产者","maufactor",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("model","model",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("engine","engine",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("img","img",Const.META_TYPE_STRING,false));

        TableHeaderProp header=new TableHeaderProp();
        header.setContainrow(1);
        
        Connection conn=null;
        String sql="select uuid,corp_id as corpId,car_num as carNum,vin,car_brand as brand,time_modified as modifier,car_color as color,manufacturer as maufactor,car_model as model,engine_type as engine,registcert_img1 as img from t_zhcx_car_vehiclelicence";
        try {
            //System.in.read();
            System.out.println("start");
            DataBaseParam param=new DataBaseParam("localhost",3306,"test","test","test");
            BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL,param);
            conn= SimpleJdbcDao.getConnection(meta,param);
            Workbook wb=ExcelGenerator.GenerateExcelFile(prop, header,conn,sql,null,new ExcelRsExtractor(prop,header));
            FileOutputStream out=new FileOutputStream("d:/test.xlsx");
            wb.write(out);
            out.close();
            System.out.println("--finish--");

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {

        }

    }
    @Test
    public void processReadFile(){
        String filePath="";
        ExcelSheetProp prop=new ExcelSheetProp();
        prop.addColumnProp(new ExcelColumnProp("id","id", Const.META_TYPE_BIGINT,false));
        prop.addColumnProp(new ExcelColumnProp("sno","sno",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("car_num","car_num",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("sendtime","sendtime",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("gpstime","gpstime",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("status","status",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("desc","desc",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("speed","speed",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("forbidespeed","forbidespeed",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("pos","pos",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("longitude","longitude",Const.META_TYPE_DOUBLE,false));
        prop.addColumnProp(new ExcelColumnProp("latitude","latitude",Const.META_TYPE_DOUBLE,false));
        prop.addColumnProp(new ExcelColumnProp("alarm","alarm",Const.META_TYPE_STRING,false));
        prop.addColumnProp(new ExcelColumnProp("other","other",Const.META_TYPE_STRING,false));

        try {
            ExcelGenerator.ReadExcelFile(filePath, prop);
            List<Map<String,String>> list=prop.getColumnList();
            System.out.println(list);
        }catch (Exception ex){

        }
    }
}
