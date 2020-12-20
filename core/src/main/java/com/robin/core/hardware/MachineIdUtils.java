package com.robin.core.hardware;

import com.google.common.collect.Lists;
import com.robin.core.base.shell.CommandLineExecutor;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class MachineIdUtils {
    private static String osName = System.getProperty("os.name").toLowerCase();
    public static final String DUBS_PATH = "/var/lib/dbus/machine-id";
    public static final String DUBS_PATH_EXT = "/etc/machine-id";
    public static final List<String> EXEC_DARWIN = Lists.newArrayList(new String[]{"ioreg", "-rd1", "-c", "IOPlatformExpertDevice"});

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

            }
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
        System.out.println(val);
        long ts = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).getEpochSecond();
        System.out.println(ts);

        val = val.subtract(BigInteger.valueOf(ts));

        System.out.println(val);
    }


}
