/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.meta.service.resource;

import com.robin.comm.fileaccess.iterator.ParquetFileIterator;
import com.robin.comm.fileaccess.util.HdfsResourceAccessUtil;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.*;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.AvroFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import com.robin.core.fileaccess.util.ApacheVfsResourceAccessUtil;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.hadoop.hdfs.HDFSUtil;
import com.robin.meta.explore.SourceFileExplorer;
import com.robin.meta.model.resource.GlobalResource;
import com.robin.meta.model.resource.ResourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.commons.dbutils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Description:Get all Available MetaData information from all kind Input source </p>
 *
 */
@Component(value="globalResourceService")
@Slf4j
public class GlobalResourceService extends BaseAnnotationJdbcService<GlobalResource, Long> {
    @Autowired
    private HadoopClusterDefService service;
    @Autowired
    private ResourceConfigService resourceConfigService;

    @Cacheable(value = "schemaCache",key = "#sourceId.toString()")
    public String getDataSourceSchemaDesc(DataCollectionMeta colmeta,Long sourceId, String sourceParamInput,int maxReadLines){
        return getDataSourceSchema(colmeta,sourceId,sourceParamInput,maxReadLines).toString(true);
    }
    @CacheEvict(value = "schemaCache",key="#sourceId.toString()")
    public void evictSourceCache(Long sourceId){

    }
    @Cacheable(value = "metaCache",key = "#sourceId")
    public DataCollectionMeta getResourceMetaDef(Long sourceId,String resPath){

        GlobalResource resource=getEntity(sourceId);

        DataCollectionMeta colmeta=new DataCollectionMeta();
        Connection connection=null;
        try {
            colmeta.setResType(resource.getResType());
            if (resource.getResType().equals(ResourceConst.ResourceType.TYPE_DB.getValue())) {
                String[] tableNames=resPath.split("\\.");
                String tableName=tableNames.length==1?tableNames[0]:tableNames[1];
                String schema=tableNames.length==1?resource.getSchema():tableNames[0];
                DataBaseParam param = new DataBaseParam(resource.getHostName(), 0, schema, resource.getUserName(), resource.getPassword());
                BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(resource.getDbType(), param);
                connection=SimpleJdbcDao.getConnection(meta, param);
                List<DataBaseColumnMeta> columnList= DataBaseUtil.getTableMetaByTableName(connection, tableName, schema, meta.getDbType());
                for(DataBaseColumnMeta columnmeta:columnList) {
                    colmeta.addColumnMeta(columnmeta.getColumnName(),columnmeta.getColumnType().toString(),null);
                }
                List<String> pkList=DataBaseUtil.getAllPrimaryKeyByTableName(connection,tableName,schema);
                colmeta.setPkColumns(pkList);
            }else if (resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_HDFSFILE.toString()))) {
                colmeta.setResourceCfgMap(service.getResourceCfg(sourceId));
            }else if (resource.getResType().equals(ResourceConst.ResourceType.TYPE_FTPFILE.getValue()) || resource.getResType().equals(ResourceConst.ResourceType.TYPE_SFTPFILE.getValue())) {
                Map<String, Object> ftpparam = colmeta.getResourceCfgMap();
                ftpparam.put("hostName", resource.getHostName());
                if (resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_FTPFILE.toString()))) {
                    ftpparam.put("protocol", "ftp");
                    ftpparam.put("port", resource.getPort() == 0 ? 21 : resource.getPort());
                } else {
                    ftpparam.put("protocol", "sftp");
                    ftpparam.put("port", resource.getPort() == 0 ? 22 : resource.getPort());
                }
                ftpparam.put("userName", resource.getUserName());
                ftpparam.put("password", resource.getPassword());
            }else {
                List<ResourceConfig> configs=resourceConfigService.queryByField("resourceId", BaseObject.OPER_EQ,sourceId);
                Map<String, Object> cfgMap = colmeta.getResourceCfgMap();
                for(ResourceConfig rsConfig:configs){
                    cfgMap.put(rsConfig.getParamKey(),rsConfig.getParamValue());
                }
            }

        }catch (Exception ex){
            log.error("",ex);
        }finally {
            if(connection!=null){
                DbUtils.closeQuietly(connection);
            }
        }
        return colmeta;
    }




    public Schema getDataSourceSchema(DataCollectionMeta colmeta,Long sourceId, String sourceParamInput,int maxReadLines){
        GlobalResource resource=getEntity(sourceId);
        Schema schema=null;

        try {
            if (resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_DB.toString()))) {
                schema=AvroUtils.getSchemaFromMeta(colmeta);
            } else if (resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_HDFSFILE.toString()))) {
                HdfsResourceAccessUtil util=new HdfsResourceAccessUtil();
                HDFSUtil dfsutil=new HDFSUtil(colmeta);
                List<String> fileList=dfsutil.listFile(sourceParamInput);
                colmeta.setPath(fileList.get(0));
                schema=getFileSchema(util,colmeta,resource,maxReadLines);
            } else if (resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_FTPFILE.toString())) || resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_SFTPFILE.toString()))) {
                ApacheVfsResourceAccessUtil util=new ApacheVfsResourceAccessUtil();
                ApacheVfsResourceAccessUtil.VfsParam param=util.returnFtpParam(resource.getHostName(),resource.getPort(),resource.getUserName(),resource.getPassword(),resource.getProtocol());
                String inputPath=sourceParamInput.endsWith("/")?sourceParamInput:sourceParamInput+"/";
                List<String> fileList=util.listFilePath(param,inputPath);
                if(!fileList.isEmpty()) {
                    colmeta.setPath( inputPath+fileList.get(0));
                }
                schema=getFileSchema(util,colmeta,resource,maxReadLines);
            }else if(resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_LOCALFILE.toString()))){


            }else if(resource.getResType().equals(Long.valueOf(ResourceConst.ResourceType.TYPE_REDIS.toString()))){

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return schema;
    }


    public static String getTableClassName(String tableName){
        String trimName= tableName.replaceAll("_","").replaceAll("-","");
        return trimName.substring(0,1).toUpperCase()+trimName.substring(1,trimName.length());
    }
    private Schema getFileSchema(AbstractResourceAccessUtil util, DataCollectionMeta meta, GlobalResource resource,int maxReadLines) throws Exception{
        Schema schema=null;
        List<String> suffixList=new ArrayList<String>();
        FileUtils.parseFileFormat(meta.getPath(),suffixList);
        String fileFormat=suffixList.get(0);
        int columnPos=0;
        //read Header 10000 Line
        int readLines=maxReadLines>0?maxReadLines:10000;
        BufferedReader reader=util.getInResourceByReader(meta);

        if(fileFormat.equalsIgnoreCase(Const.FILESUFFIX_CSV)){
            SourceFileExplorer.exploreCsv(reader,meta,resource.getRecordContent()==null?null:resource.getRecordContent().split(","),readLines);
            schema=AvroUtils.getSchemaFromMeta(meta);
        }else if(fileFormat.equalsIgnoreCase(Const.FILESUFFIX_JSON)){
            SourceFileExplorer.exploreJson(reader,meta,readLines);
            schema=AvroUtils.getSchemaFromMeta(meta);
        }else if(fileFormat.equalsIgnoreCase(Const.FILESUFFIX_XML)){

        }
        else if(fileFormat.equalsIgnoreCase(Const.FILESUFFIX_PARQUET)){
            AbstractFileIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(Const.FILETYPE_PARQUET,meta,reader);
            if(iterator.hasNext()){
                schema=((ParquetFileIterator)iterator).getSchema();
            }
        }else if(fileFormat.equalsIgnoreCase(Const.FILESUFFIX_AVRO)){
            AbstractFileIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(Const.FILETYPE_AVRO,meta,reader);
            if(iterator.hasNext()){
                schema=((AvroFileIterator)iterator).getSchema();
            }
        }
        return schema;
    }



}

