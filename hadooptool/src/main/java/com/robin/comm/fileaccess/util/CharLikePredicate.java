package com.robin.comm.fileaccess.util;

import org.apache.parquet.filter2.predicate.Statistics;
import org.apache.parquet.filter2.predicate.UserDefinedPredicate;
import org.apache.parquet.io.api.Binary;
import org.springframework.util.Assert;

import java.io.Serializable;

public class CharLikePredicate extends UserDefinedPredicate<Binary> implements Serializable {
    private String fitString;
    private Integer fitType;
    public CharLikePredicate(String fitString){
        Assert.notNull(fitString,"");
        if(fitString.startsWith("%")){
            if(fitString.endsWith("%")) {
                fitType = 3;
                this.fitString = fitString.substring(1);
            }else{
                fitType=1;
                this.fitString = fitString.substring(1);
            }
        }else if(fitString.endsWith("%")){
            fitType=2;
            this.fitString=fitString.substring(0,fitString.length()-1);
        }else{
            fitType=3;
            this.fitString=fitString;
        }
    }
    @Override
    public boolean keep(Binary binary) {
        boolean fitTag=false;
        switch (fitType){
            case 1:
                fitTag= binary.toStringUsingUTF8().endsWith(fitString);
                break;
            case 2:
                fitTag=binary.toStringUsingUTF8().startsWith(fitString);
                break;
            default:
                fitTag=binary.toStringUsingUTF8().contains(fitString);
        }
        return fitTag;
    }

    @Override
    public boolean canDrop(Statistics<Binary> statistics) {
        if(fitType==1){
            String minStr=statistics.getMin().toStringUsingUTF8().substring(0,fitString.length());
            String maxStr=statistics.getMax().toStringUsingUTF8().substring(0,fitString.length());
            return fitString.compareTo(minStr)<0 || fitString.compareTo(maxStr)>0;
        }
        return false;
    }

    @Override
    public boolean inverseCanDrop(Statistics<Binary> statistics) {
        if(fitType==1){
            String minStr=statistics.getMin().toStringUsingUTF8().substring(0,fitString.length());
            String maxStr=statistics.getMax().toStringUsingUTF8().substring(0,fitString.length());
            return fitString.compareTo(minStr)>0 && fitString.compareTo(maxStr)<0;
        }
        return false;
    }
}
