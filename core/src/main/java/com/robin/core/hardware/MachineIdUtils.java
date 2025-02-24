package com.robin.core.hardware;

import com.robin.core.base.shell.CommandLineExecutor;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    public static String getCPUSerial() throws RuntimeException{
        String serial="";
        try {
            if (isWindows()) {
                //windows get disk serial
                serial = CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("wmic", "cpu", "get", "ProcessorId"), 1);
            }else if(isLinux()){
                //serial=CommandLineExecutor.getInstance().executeCmd("dmidecode -s baseboard-serial-number");
                serial = CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("dmidecode", "-t", "4", "|", "grep", "\"ID\""), "ID:");
                if("Not Sepcified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("system_profiler","SPHardwareDataType"),"Serial Number (system):");
                if("Not Sepcified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }
            return serial;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static String getSystemSerial() throws RuntimeException{
        String serial="";
        try {
            if (isWindows()) {
                //windows get disk serial
                serial = CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("wmic", "diskdrive", "get", "serialnumber"), 1);
            }else if(isLinux()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("dmidecode","-t","system"),"Serial Number:");
                if("Not Sepcified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("system_profiler","SPHardwareDataType"),"Serial Number (system):");
                if("Not Sepcified".equalsIgnoreCase(serial)){
                    serial="";
                }
            }
            return serial;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static String getHardDiskSerial() throws RuntimeException{
        String serial=null;
        try{
            if(isWindows()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnAfterRow(Arrays.asList("wmic","path","win32_physicalmedia","get","serialnumber"),1);
            }else if(isLinux()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("sudo","lshw","-class","disk","|","grep","serial"),"serial:");
            }else if(isMacosName() || isMacosNameX()){
                serial=CommandLineExecutor.getInstance().executeCmdReturnSpecifyKey(Arrays.asList("system_profiler","SPStorageDataType"),"Volume UUID");
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

    public static void main(String[] args) {
        String machineId = MachineIdUtils.getMachineId();
        //String[] strs=caculateMachineSerail(machineId,System.currentTimeMillis()+3600*24*365*1000);
        BigInteger val = new BigInteger(machineId.replaceAll("-", ""), 16);
        System.out.println(machineId);
        System.out.println("cpu "+MachineIdUtils.getCPUSerial());
        System.out.println("system "+MachineIdUtils.getSystemSerial());
        System.out.println("hardware "+MachineIdUtils.getHardDiskSerial());
        //System.out.println(val);
        long ts = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).getEpochSecond();
        //System.out.println(ts);

        val = val.subtract(BigInteger.valueOf(ts));

        //System.out.println(val);
    }


}
