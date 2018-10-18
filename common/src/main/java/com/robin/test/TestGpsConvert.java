package com.robin.test;

import com.esri.core.geometry.*;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.query.extractor.ResultSetOperationExtractor;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.test</p>
 * <p>
 * <p>Copyright: Copyright (c) 2017 create at 2017年12月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class TestGpsConvert {
    public final static double a = 6378245.0;
    public final static double ee = 0.00669342162296594323;
    public static void main(String[] args){
        TestGpsConvert convert=new TestGpsConvert();
        convert.doTransform();


    }
    public void doTransform(){
        DataBaseParam param=new DataBaseParam("172.16.102.124",0,"china","osmuser","pass");
        BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_PGSQL,param);
        final Connection conn= SimpleJdbcDao.getConnection(meta,param);
        final String sql="update rhunan_polyline set geom1=? where gid=?";
        try{
            final List<Object[]> list=new ArrayList<Object[]>();
            final int batchSize=10000;
            SimpleJdbcDao.executeOperationWithQuery(conn, "select ST_AsBinary(geom) as geom,gid from rhunan_polyline where geom1 is null", new ResultSetOperationExtractor() {
                @Override
                public boolean executeAddtionalOperation(Map<String, Object> map, String[] strings, String[] strings1, String[] strings2) throws SQLException {
                    if(list.size()>=batchSize){
                        SimpleJdbcDao.simpleBatch(conn,sql,list);
                        list.clear();
                    }else{
                        byte[] bytes=(byte[])map.get("geom");
                        Polyline geometry = (Polyline) OperatorImportFromWkb.local().execute(WkbImportFlags.wkbImportDefaults, Geometry.Type.Polyline, ByteBuffer.wrap(bytes), null);
                        //Polyline newline=new Polyline();
                        for(int i=0;i<geometry.getPointCount();i++){
                            Point point=geometry.getPoint(i);
                            BDLocation location=GCJ02_to_WGS84(new BDLocation(point.getY(),point.getX()));
                            geometry.setPoint(i,new Point(location.getLongitude(),location.getLatitude()));
                        }
                        ByteBuffer buffer=OperatorExportToWkb.local().execute(WkbExportFlags.wkbExportDefaults,geometry,null);
                        list.add(new Object[]{buffer.array(),map.get("gid")});
                    }
                    return true;
                }

                @Override
                public void init() {

                }
            });
            if(!list.isEmpty()){
                SimpleJdbcDao.simpleBatch(conn,sql,list);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if(conn!=null){
                try {
                    conn.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
    public BDLocation GCJ02_to_WGS84(BDLocation bdLocation) {

        BDLocation tmpLocation = new BDLocation(bdLocation.getLatitude(),bdLocation.getLongitude());
        BDLocation tmpLatLng = WGS84_to_GCJ02(tmpLocation);
        double tmpLat = 2 * bdLocation.getLatitude() - tmpLatLng.getLatitude();
        double tmpLng = 2 * bdLocation.getLongitude()
                - tmpLatLng.getLongitude();


        tmpLocation.setLatitude(bdLocation.getLatitude());
        tmpLocation.setLongitude(bdLocation.getLongitude());
        tmpLatLng = WGS84_to_GCJ02(tmpLocation);
        tmpLat = 2 * tmpLat - tmpLatLng.getLatitude();
        tmpLng = 2 * tmpLng - tmpLatLng.getLongitude();

        bdLocation.setLatitude(tmpLat);
        bdLocation.setLongitude(tmpLng);
        return bdLocation;
    }

    public BDLocation WGS84_to_GCJ02(BDLocation bdLocation) {

        double dLat = transformLat(bdLocation.getLongitude() - 105.0, bdLocation.getLatitude() - 35.0);
        double dLon = transformLon(bdLocation.getLongitude() - 105.0, bdLocation.getLatitude() - 35.0);
        double radLat = bdLocation.getLatitude() / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0)  / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        bdLocation.setLatitude(bdLocation.getLatitude() + dLat);
        bdLocation.setLongitude(bdLocation.getLongitude() + dLon);
        return bdLocation;
    }
    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
                * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0
                * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y
                * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
                * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0
                * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x
                / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
    public class BDLocation {
        private double longitude;

        private double latitude;
        public BDLocation(double latitude,double longitude){
            this.latitude=latitude;
            this.longitude=longitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }


}
