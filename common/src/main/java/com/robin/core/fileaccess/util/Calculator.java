package com.robin.core.fileaccess.util;

import com.robin.comm.sql.CommSqlParser;
import com.robin.comm.sql.SqlSegment;
import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.sql.SqlNode;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

@Setter
@Getter
public class Calculator implements Closeable {
    private Object leftValue;
    private Object rightValue;
    private Boolean runValue;
    private String cmpColumn;
    private String columnName;
    private SqlSegment segment;
    private CommSqlParser.ValueParts valueParts;
    private Map<String,Object> inputRecord;
    private Map<String,Object> outputRecord;
    protected boolean busyTag=false;

    public Calculator(){

    }
    public boolean doCompare(SqlNode node){
        SqlContentResolver.doCompare(this,node);
        return runValue;
    }
    public boolean doCalculate(CommSqlParser.ValueParts valueParts){
        return SqlContentResolver.doCalculate(this,valueParts);
    }
    public boolean walkTree(SqlNode node){
        return SqlContentResolver.walkTree(this,node);
    }
    public void clear(){
        leftValue=null;
        rightValue=null;
        runValue=null;
        columnName=null;
    }



    @Override
    public void close() throws IOException {
        leftValue=null;
        rightValue=null;
        runValue=null;
        columnName=null;
        inputRecord.clear();
        outputRecord.clear();
        inputRecord=null;
        outputRecord=null;
        segment=null;
        valueParts=null;
        setBusyTag(false);
    }
    public void finish(){
        setBusyTag(false);
    }


    public void setBusyTag(boolean tag){
        this.busyTag=tag;
    }

    public boolean isBusyTag() {
        return busyTag;
    }
}
