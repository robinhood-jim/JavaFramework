package com.robin.comm.calculator.math;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class Round extends AbstractCalculator {
    int scale=0;
    public Round(){
        functionName="ROUND";
    }
    public Round(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="ROUND";
        if(parameters.length==2){
            scale=Integer.parseInt(parameters[1]);
        }
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Object retValue=null;
        Const.CLASSTYPE retType= Const.CLASSTYPE.INTEGER;
        switch (metaType){
            case FLOAT:
                retValue=Math.round(Float.parseFloat(getValue(parameters[0]).toString()));
                break;
            case DOUBLE:
                retValue=Math.round(Double.parseDouble(getValue(parameters[0]).toString()));
                retType= Const.CLASSTYPE.LONG;
                break;
            default:
                throw new OperationNotSupportException("column type not suppoerted!");
        }
        return Pair.of(retType,retValue);
    }
    //public static String getFunctionName(){return "ROUND";}
}
