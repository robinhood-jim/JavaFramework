package com.robin.core.dbimp;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.shell.CommandLineExecutor;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.template.util.FreeMarkerUtil;

public class OracleDataBaseImportor extends BaseDataBaseImportor {
    public OracleDataBaseImportor() {
        init();
    }

    @Override
    public int importFromLocal(DataBaseImportParam inparam, DataBaseParam param) {
        int ret = -1;
        String ctlFile = null;

        //generate Oracle control File
        FreeMarkerUtil util = new FreeMarkerUtil("");
        ctlFile = tmpPath + "run" + System.currentTimeMillis() + ".ctl";
        try (PrintWriter writer = new PrintWriter(new File(ctlFile), "UTF-8")) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("filePath", inparam.getFilePath());
            map.put("tableName", inparam.getTableName());
            map.put("spilt", inparam.getSplit());
            map.put("columns", inparam.getFields());
            map.put("encode", inparam.getEncode());
            util.process("oraclectl.ftl", map, writer);
            writer.close();
            List<String> cmdList = new ArrayList<String>();
            cmdList.add("sh");
            cmdList.add("-c");
            cmdList.add(userPath + Const.ORACLEIMP_SCRIPTS);
            cmdList.add(param.getUserName());
            cmdList.add(param.getPasswd());
            cmdList.add(ctlFile);
            cmdList.add(String.valueOf(inparam.getRows()));
            //cmdList.add("userid="+param.getUserName()+"/"+param.getPasswd());
            //cmdList.add("control='"+inparam.getScriptPath()+"'");
            //cmdList.add("bindsize=256000000");
            //cmdList.add("rows=10000");
            CommandLineExecutor.getInstance().executeCmd(cmdList);
            ret = 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = -1;
        } finally {
            if (ctlFile != null && !ctlFile.isEmpty()) {
                FileUtils.deleteQuietly(new File(ctlFile));
            }
        }
        return ret;

    }
}
