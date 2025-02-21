package com.robin.comm.fileaccess.shuffle;

import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordMapShuffler {
    private AbstractFileIterator iterator;
    private AbstractFileSystemAccessor accessor;
    private DataCollectionMeta colmeta;
    private Map<String,List<Map<String,Object>>>  recordMap=new HashMap<>();
    private Map<String, DataSetColumnMeta> metaMap;
    private List<Pair<DataSetColumnMeta,Object>> sortColumns;
    private List<DataSetColumnMeta> selectColumns;
    private int spits=10;


    private RecordMapShuffler(){

    }
    public void RecordShuffler(DataCollectionMeta colmeta,AbstractFileIterator iterator,AbstractFileSystemAccessor accessor){
        this.colmeta=colmeta;
        this.iterator=iterator;
        this.accessor=accessor;
        this.metaMap=colmeta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnName, Function.identity()));
    }

    public void shuffle(String shuffleOutputPath){
        StringBuilder builder=new StringBuilder();

        while(iterator.hasNext()){
            Map<String,Object> map=iterator.next();
            if(sortColumns.size()==1){

            }else {
                for (Pair<DataSetColumnMeta,Object> pair : sortColumns) {

                }
            }
        }
    }
    public static class Builder{
        private static RecordMapShuffler shuffler=new RecordMapShuffler();
        private Builder(){

        }
        public static Builder newBuilder(){
            return new Builder();
        }
        public Builder withColumnMeta(DataCollectionMeta colmeta){
            shuffler.colmeta=colmeta;
            return this;
        }
        public Builder withFileIterator(AbstractFileIterator iterator){
            shuffler.iterator=iterator;
            return this;
        }
        public Builder withFileSystemAccessor(AbstractFileSystemAccessor accessor){
            shuffler.accessor=accessor;
            return this;
        }
        public Builder groupColumns(List<Pair<DataSetColumnMeta,Object>> sortColumns){
            shuffler.sortColumns=sortColumns;
            return this;
        }
        public Builder selectColumns(List<DataSetColumnMeta> columns){
            shuffler.selectColumns=columns;
            return this;
        }
        public Builder spilts(int splits){
            shuffler.spits=splits;
            return this;
        }
        public RecordMapShuffler build(){
            return shuffler;
        }

    }

}
