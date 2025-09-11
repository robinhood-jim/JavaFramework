package com.robin.core.hardware;

import cn.hutool.core.util.StrUtil;
import com.robin.core.base.shell.CommandLineExecutor;
import com.robin.core.base.util.Const;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MachineIdUtils {
    private static String osName = System.getProperty("os.name").toLowerCase();
    public static final String DUBS_PATH = "/var/lib/dbus/machine-id";
    public static final String DUBS_PATH_EXT = "/etc/machine-id";
    public static final List<String> EXEC_DARWIN = Arrays.asList(new String[]{"ioreg", "-rd1", "-c", "IOPlatformExpertDevice"});

    public static String getMachineId() throws RuntimeException {
        String machineId = null;
        BufferedReader reader = null;
        try {
            if (isWindows()) {
                disableAccessWarnings();
                machineId = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Cryptography", "MachineGuid");
            } else if (isLinux()) {

                File file = new File(DUBS_PATH);
                if (!file.exists()) {
                    file = new File(DUBS_PATH_EXT);
                    if (!file.exists()) {
                        throw new RuntimeException("can not found suitable machineId");
                    }
                }
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                machineId = reader.readLine();
            } else if (isMacosName() || isMacosNameX()) {
                String output = CommandLineExecutor.getInstance().executeCmd(EXEC_DARWIN);
                reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes())));
                String line;
                int pos = -1;
                while ((line = reader.readLine()) != null) {
                    pos = line.indexOf("IOPlatformUUID");
                    if (pos != -1) {
                        pos = line.indexOf("\" = \"");
                        machineId = line.substring(pos + 5, line.length()).trim();
                    }
                }
            }
            return machineId;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile =
                    unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long)staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
        } catch (Exception ignored) {
        }
    }

    public static String getCPUSerial() throws RuntimeException{
        String serial="";
        BufferedReader reader=null;
        try {
            if (isWindows()) {
                //windows get disk serial
                serial = CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("powershell.exe","Get-WmiObject","-Class","Win32_Processor","|","Select-Object","ProcessorId"), 2);
            }else if(isLinux()){
                //serial=CommandLineExecutor.getInstance().executeCmd("dmidecode -s baseboard-serial-number");
                serial = CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("dmidecode", "-t", "4", "|", "grep", "\"ID\""), "ID:");
                if("Not Specified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("system_profiler","SPHardwareDataType"),"Serial Number (system):");
                if("Not Specified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }
            if(serial.contains("\n") || serial.contains("\r\n")) {
                reader = new BufferedReader(new StringReader(serial));
                serial=reader.readLine();
            }
            return !StrUtil.isBlank(serial)?serial.trim():"";
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public static String getSystemSerial() throws RuntimeException{
        String serial="";

        try {
            if (isWindows()) {
                //windows get disk serial
                serial = CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("powershell.exe", "Get-WmiObject", "-class","win32_bios","|","Select-Object","SerialNumber"), 2);
            }else if(isLinux()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("sudo","dmidecode","-t","system"),"Serial Number:");
                if(StrUtil.isBlank(serial) || Const.INVALID.equals(serial) || "Not Specified".equals(serial)){
                    serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("sudo","dmidecode","-t","system"),"UUID:");
                }
                if("Not Specified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("system_profiler","SPHardwareDataType"),"Serial Number (system):");
                if("Not Specified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }
            return !StrUtil.isBlank(serial)?serial.trim():"";
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static String getHardDiskSerial() throws RuntimeException{
        String serial=null;
        BufferedReader reader=null;
        try{
            if(isWindows()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("powershell.exe","Get-WmiObject","-Class","Win32_DiskDrive","|","Select-Object","SerialNumber,DeviceId"),2);
                reader=new BufferedReader(new StringReader(serial));
                String tmpStr;
                String selSerial=null;
                int deviceId=10000;
                while((tmpStr=reader.readLine())!=null){
                    String[] arr=tmpStr.split(" ");
                    int currentdevice=Integer.parseInt(arr[1].substring(arr[1].length()-1));
                    if(currentdevice<deviceId){
                        selSerial=arr[0].trim();
                        deviceId=currentdevice;
                    }
                }
                serial=selSerial;
            }else if(isLinux()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("bash","-c","sudo lsblk -o SERIAL"),1);

            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("bash","-c","sudo system_profiler SPStorageDataType"),"Volume UUID");
            }
            return serial;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }



    public static boolean isLinux() {
        return osName.indexOf("linux") >= 0;
    }

    public static boolean isMacosName() {
        return osName.indexOf("mac") >= 0 && osName.indexOf("osName") > 0 && osName.indexOf("x") < 0;
    }

    public static boolean isMacosNameX() {
        return osName.indexOf("mac") >= 0 && osName.indexOf("osName") > 0 && osName.indexOf("x") > 0;
    }

    public static boolean isWindows() {
        return osName.indexOf("windows") >= 0;
    }

    public static boolean isosName2() {
        return osName.indexOf("osName/2") >= 0;
    }

    public static boolean isSolaris() {
        return osName.indexOf("solaris") >= 0;
    }

    public static boolean isSunosName() {
        return osName.indexOf("sunosName") >= 0;
    }

    public static boolean isMPEiX() {
        return osName.indexOf("mpe/ix") >= 0;
    }

    public static boolean isHPUX() {
        return osName.indexOf("hp-ux") >= 0;
    }

    public static boolean isAix() {
        return osName.indexOf("aix") >= 0;
    }

    public static boolean isosName390() {
        return osName.indexOf("osName/390") >= 0;
    }

    public static boolean isFreeBSD() {
        return osName.indexOf("freebsd") >= 0;
    }

    public static boolean isIrix() {
        return osName.indexOf("irix") >= 0;
    }

    public static boolean isDigitalUnix() {
        return osName.indexOf("digital") >= 0 && osName.indexOf("unix") > 0;
    }

    public static boolean isNetWare() {
        return osName.indexOf("netware") >= 0;
    }

    public static boolean isosNameF1() {
        return osName.indexOf("osNamef1") >= 0;
    }

    public static boolean isOpenVMS() {
        return osName.indexOf("openvms") >= 0;
    }
    public static String getOsName(){
        return osName;
    }
    public static String getSystemTag(){
        StringBuilder builder=new StringBuilder();

        String machineId = MachineIdUtils.getMachineId();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append("MID_"+machineId);
        }
        String systemSerial=MachineIdUtils.getCPUSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append("_CPU_"+systemSerial);
        }
        String hardDsSerial=MachineIdUtils.getHardDiskSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append("_DISK_"+hardDsSerial);
        }
        builder.append("_SYS_"+MachineIdUtils.getOsName());
        return builder.toString();
    }
    public static String getSystemTagNum(){
        StringBuilder builder=new StringBuilder();

        String machineId = MachineIdUtils.getMachineId();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(machineId);
        }
        String systemSerial=MachineIdUtils.getCPUSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(systemSerial);
        }
        String hardDsSerial=MachineIdUtils.getHardDiskSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(hardDsSerial);
        }
        return builder.toString();
    }


    public static void main(String[] args) {
        String machineId = MachineIdUtils.getMachineId();
        //String[] strs=caculateMachineSerail(machineId,System.currentTimeMillis()+3600*24*365*1000);

        StringBuilder builder=new StringBuilder();

        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(machineId);
        }
        String systemSerial=MachineIdUtils.getCPUSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(systemSerial);
        }
        String hardDsSerial=MachineIdUtils.getHardDiskSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(hardDsSerial);
        }
        String password= builder.toString().replace("-","");
        System.out.println(password);
        String tag=MachineIdUtils.getSystemTag();
        System.out.println(tag);
    }


}
