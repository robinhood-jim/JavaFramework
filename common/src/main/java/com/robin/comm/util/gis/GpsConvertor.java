package com.robin.comm.util.gis;

import com.esri.core.geometry.Point;
import com.robin.core.base.exception.MissingConfigException;
import org.springframework.util.Assert;

@SuppressWarnings("unused")
public class GpsConvertor {
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;
    private static final double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    public static boolean gpsInChina(Point point){
        Assert.notNull(point,"point must not be null");
        double latitude=point.getY();
        double longtitude=point.getX();
        boolean inFlag= !(longtitude < 72.004) && !(longtitude > 137.8347);
        if(latitude<0.8293 || latitude>55.8271){
            inFlag=false;
        }
        if ((119.962 < longtitude && longtitude < 121.750) && (21.586 < latitude && latitude < 25.463)){
            inFlag=false;
        }
        return inFlag;
    }
    public static Point BAIDU_to_WGS84(Point point){
        if(!gpsInChina(point)){
            throw new MissingConfigException("gps not in china range! can not convert");
        }
        double x = point.getX() - 0.0065;
        double y = point.getY() - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        return GCJ02_to_WGS84(new Point(z * Math.cos(theta),z * Math.sin(theta)));
    }
    public static Point WGS84_to_BAIDU(Point point){
        if(!gpsInChina(point)){
            throw new MissingConfigException("gps not in china range! can not convert");
        }
        double x = point.getX();
        double y = point.getY();

        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new Point(bdLng,bdLat);
    }
    public static Point GCJ02_to_WGS84(Point point){
        if(!gpsInChina(point)){
            throw new MissingConfigException("gps not in china range! can not convert");
        }
        Point tmpLatLng = WGS84_to_GCJ02(point);
        double tmpLat = 2 * point.getY()- tmpLatLng.getY();
        double tmpLng = 2 * point.getX() - tmpLatLng.getX();

        //第二次纠偏
        tmpLatLng = WGS84_to_GCJ02(point);
        tmpLat = 2 * tmpLat - tmpLatLng.getY();
        tmpLng = 2 * tmpLng - tmpLatLng.getX();
        return new Point(tmpLng,tmpLat);
    }
    private static Point WGS84_to_GCJ02(Point point){
        if(!gpsInChina(point)){
            throw new MissingConfigException("gps not in china range! can not convert");
        }
        double dLat = transformLat(point.getX() - 105.0, point.getY() - 35.0);
        double dLon = transformLon(point.getX() - 105.0, point.getY() - 35.0);
        double radLat = point.getY() / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        return new Point(point.getX()+(dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI),point.getY()+(dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI));
    }
    private static double transformLat(Double x, Double y){
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(Double x, Double y){
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
}
