package com.robin.dataming.spark.algorithm;


import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.util.TextBasedRecordParser;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.util.ObjectUtils;
import scala.Serializable;
import scala.Tuple2;

import java.util.Map;

import static org.apache.spark.sql.types.DataTypes.*;

public abstract class AbstractSparkModeler implements Serializable {
    private SparkConf sparkConf;

    private SparkSession session;
    public AbstractSparkModeler(Map<String,String> configMap){
        sparkConf=new SparkConf();
        if(!ObjectUtils.isEmpty(configMap.get("masterUrl"))){
            sparkConf.setMaster(configMap.get("masterUrl"));
        }
        if(!ObjectUtils.isEmpty(configMap.get("appName"))){
            sparkConf.setAppName(configMap.get("appName"));
        }
        SparkSession.Builder builder=SparkSession.builder().config(sparkConf).appName(configMap.get("appName"));
        if("true".equalsIgnoreCase(configMap.get("enableHive"))) {
            builder = builder.enableHiveSupport();
        }
        session=builder.getOrCreate();
    }
    public JavaRDD<LabeledPoint> getInstances(DataCollectionMeta collectionMeta,int classNum) throws RuntimeException{

        if(ResourceConst.IngestType.TYPE_HDFS.getValue().equals(collectionMeta.getSourceType())) {
            Dataset<Row> ds=null;
            if (ResourceConst.FileFormat.TYPE_AVRO.toString().equals(collectionMeta.getFileFormat())) {
                ds = session.read().format("avro").load(ResourceUtil.getProcessPath(collectionMeta.getPath()));
            } else if (ResourceConst.FileFormat.TYPE_CSV.toString().equals(collectionMeta.getFileFormat())) {
                ds = session.read().option("sep", collectionMeta.getSplit()).schema(getSchema(collectionMeta)).csv(ResourceUtil.getProcessPath(collectionMeta.getPath()));
            } else if (ResourceConst.FileFormat.TYPE_JSON.toString().equals(collectionMeta.getFileFormat())) {
                ds = session.read().schema(getSchema(collectionMeta)).json(ResourceUtil.getProcessPath(collectionMeta.getPath()));
            } else if (ResourceConst.FileFormat.TYPE_ORC.toString().equals(collectionMeta.getFileFormat())) {
                ds = session.read().orc(ResourceUtil.getProcessPath(collectionMeta.getPath()));
            } else if (ResourceConst.FileFormat.TYPE_PARQUET.toString().equals(collectionMeta.getFileFormat())) {
                ds = session.read().parquet(ResourceUtil.getProcessPath(collectionMeta.getPath()));
            }else{
                throw new MissingConfigException("this file format does not support now!");
            }
            if(!ObjectUtils.isEmpty(ds)) {
                return returnRdd(ds,collectionMeta,classNum);
            }
            return null;
        }else if(ResourceConst.IngestType.TYPE_FTP.getValue().equals(collectionMeta.getSourceType()) || ResourceConst.IngestType.TYPE_SFTP.getValue().equals(collectionMeta.getSourceType())){
            //ftp use sparkContext wholeTextFiles,do not support binary format files
            if(ResourceConst.FileFormat.TYPE_AVRO.toString().equals(collectionMeta.getFileFormat()) || ResourceConst.FileFormat.TYPE_ORC.toString().equals(collectionMeta.getFileFormat()) || ResourceConst.FileFormat.TYPE_PARQUET.toString().equals(collectionMeta.getFileFormat())){
                throw new MissingConfigException("when using ftp or sftp,can not read binary files!");
            }
            JavaRDD<Tuple2<String,String>> rdd=session.sparkContext().wholeTextFiles(collectionMeta.constructUrl(),1).toJavaRDD();
            return rdd.map(pair->construct(collectionMeta,pair._2,classNum));
        }else if(ResourceConst.IngestType.TYPE_DB.getValue().equals(collectionMeta.getSourceType())){
            BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(collectionMeta.getDbType(),collectionMeta.getParam());
            String jdbcUrl=meta.getUrl();
            String tableName=!ObjectUtils.isEmpty(meta.getParam().getSchema())?meta.getParam().getSchema()+"."+collectionMeta.getTableName():collectionMeta.getTableName();
            Dataset<Row> ds=session.read().format("jdbc").option("url",jdbcUrl)
                    .option("dbtable",tableName).option("user",meta.getParam().getUserName())
                    .option("password",meta.getParam().getPasswd()).load();
            return returnRdd(ds,collectionMeta,classNum);
        }else if(ResourceConst.IngestType.TYPE_LOCAL.getValue().equals(collectionMeta.getSourceType())){
            if(ResourceConst.FileFormat.TYPE_AVRO.toString().equals(collectionMeta.getFileFormat()) || ResourceConst.FileFormat.TYPE_ORC.toString().equals(collectionMeta.getFileFormat()) || ResourceConst.FileFormat.TYPE_PARQUET.toString().equals(collectionMeta.getFileFormat())){
                throw new MissingConfigException("when using local file,can not read binary files!");
            }
            JavaRDD<String>  rdd=session.sparkContext().textFile("file://"+ResourceUtil.getProcessPath(collectionMeta.getPath()),1).toJavaRDD();
            return rdd.map(f->construct(collectionMeta,f,classNum));
        }
        else {
            throw new MissingConfigException("this mode can not support now!");
        }
    }
    private LabeledPoint construct(DataCollectionMeta collectionMeta,String value,int classNum){
        Map<String,Object> valueMap= TextBasedRecordParser.parseTextStream(collectionMeta, value);
        double[] densval = new double[collectionMeta.getColumnList().size() - 1];
        double labelVal = 0.0;
        for (int i = 0; i < collectionMeta.getColumnList().size(); i++) {
            DataSetColumnMeta columnMeta = collectionMeta.getColumnList().get(i);
            if(!ObjectUtils.isEmpty(valueMap.get(columnMeta.getColumnName()))){
                if (i < classNum) {
                    densval[i] = Double.parseDouble(valueMap.get(columnMeta.getColumnName()).toString());
                } else if (i == classNum) {
                    if(!ObjectUtils.isEmpty(columnMeta.getNominalValues())){
                        String originVal = valueMap.get(columnMeta.getColumnName()).toString();
                        int pos = 0;
                        if (columnMeta.getNominalValues().contains(originVal)) {
                            pos = columnMeta.getNominalValues().indexOf(originVal);
                        }
                        labelVal=Double.parseDouble(String.valueOf(pos));
                    }else {
                        labelVal = Double.parseDouble(valueMap.get(columnMeta.getColumnName()).toString());
                    }
                } else {
                    densval[i - 1] = Double.parseDouble(valueMap.get(columnMeta.getColumnName()).toString());
                }
            }
        }
        return new LabeledPoint(labelVal, Vectors.dense(densval));
    }
    private StructType getSchema(DataCollectionMeta collectionMeta){
        StructType type=new StructType();
        for(int i=0;i<collectionMeta.getColumnList().size();i++){
            DataSetColumnMeta setColumnMeta=collectionMeta.getColumnList().get(i);
            if(Const.META_TYPE_BIGINT.equals(setColumnMeta.getColumnType()) ){
                type.add(new StructField(setColumnMeta.getColumnName(), LongType,true, Metadata.empty()));
            }else if(Const.META_TYPE_INTEGER.equals(setColumnMeta.getColumnType())){
                type.add(new StructField(setColumnMeta.getColumnName(),IntegerType,true,Metadata.empty()));
            }else if(Const.META_TYPE_DOUBLE.equals(setColumnMeta.getColumnType()) || Const.META_TYPE_NUMERIC.equals(setColumnMeta.getColumnType())){
                type.add(new StructField(setColumnMeta.getColumnName(),DoubleType,true,Metadata.empty()));
            }else if(Const.META_TYPE_FLOAT.equals(setColumnMeta.getColumnType())){
                type.add(new StructField(setColumnMeta.getColumnName(),FloatType,true,Metadata.empty()));
            }else if(Const.META_TYPE_SHORT.equals(setColumnMeta.getColumnType())){
                type.add(new StructField(setColumnMeta.getColumnName(),ShortType,true,Metadata.empty()));
            }else if(Const.META_TYPE_DATE.equals(setColumnMeta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(setColumnMeta.getColumnType())){
                type.add(new StructField(setColumnMeta.getColumnName(),TimestampType,true,Metadata.empty()));
            }else{
                type.add(new StructField(setColumnMeta.getColumnName(),StringType,true,Metadata.empty()));
            }
        }
        return type;
    }
    private JavaRDD<LabeledPoint> returnRdd(Dataset<Row> ds,DataCollectionMeta collectionMeta,int classNum){
        return ds.toJavaRDD().map(f -> {
            double[] densval = new double[collectionMeta.getColumnList().size() - 1];
            double labelVal = 0.0;
            for (int i = 0; i < collectionMeta.getColumnList().size(); i++) {
                DataSetColumnMeta columnMeta = collectionMeta.getColumnList().get(i);

                if (!ObjectUtils.isEmpty(f.getAs(columnMeta.getColumnName()))) {
                    if (i < classNum) {
                        densval[i] = Double.parseDouble(f.get(i).toString());
                    } else if (i == classNum) {
                        labelVal = Double.parseDouble(f.get(i).toString());
                    } else {
                        densval[i - 1] = Double.parseDouble(f.get(i).toString());
                    }
                }
            }
            return new LabeledPoint(labelVal, Vectors.dense(densval));
        });
    }
}
