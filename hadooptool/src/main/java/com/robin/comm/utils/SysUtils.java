package com.robin.comm.utils;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

public class SysUtils {
    private static OperatingSystemMXBean systemMXBean=(OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    private static DecimalFormat format=new DecimalFormat("#.##");
    private SysUtils(){
    }
    public static Double getFreeMemory(){
        return systemMXBean.getFreePhysicalMemorySize()/1024.0/1024;
    }
    public static Double getTotalMemory(){
        return systemMXBean.getTotalPhysicalMemorySize()/1024.0/1024;
    }
    public static void main(String[] args){
        System.out.println(SysUtils.getFreeMemory());
        System.out.println(SysUtils.getTotalMemory());
    }
}
