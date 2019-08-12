#
Simple Java Frame V1.0
Slightly Framework design to  support Spring based java or Bigdata program.

1.Introduction

	I.This project is base on Spring Framework and has four modules:
		Core: the core class include data access layer basic class(model,dao,service) and  etc.
		comm: FileSystem Access tool(local/vfs),support FileFormat(csv/xml/json/avro/parquet/protobuf),
		      support Compress Format(gzip/bzip2/snappy/lzo/zip/lzma/lz4)
		      ,read and write excel,read word or PowerPoint
		hadooptool:FileSystem Access tool(hdfs), comm tool to access to HDFS,Hbase,Hive,Mongdb and etc
		web: struts1,struts2 and springmvc support web component and required class.
   
    II. Special feature
        a.A user defined xml Query config system,similar to mybatis,but easy config.
        b.Support defined annotation or jpa annotation in JdbcDao with ORM.
        c. BaseAnnotationService can access DB with minimize code,and use transaction with annotation.
        d.A common db access meta and util,can access all kind of db.
        
 It is available under the terms of either the Apache Software License 2.0 or the Eclipse Public License 1.0.
 
 2.Development
 
    I.Model Layer:Simple ORM tool, Support JAVA JPA or my BaseObject Annotation
        Demostration：(support Composite primary key) 
        1.Model Class
            exmaple: core/src/test/java/com/robin/core/test/model
            TestJPaModel Model annotation with java JPA
            TestModel model annotation with My model definition
            TestLob  support for clob and blob
        usage：
            1.create class extends BaseObject;
            2.create parameter mapping DB cloumns
            using annotation
            @MappingEnity 
                 -------------------------------------------------------------------
                |parameter           |reference                                     |
                |table               |tableName                                     |
                |schema              |specify schema                                |
                |jdbcDao             |Specify JdbcDao(can switch datasource)        |                       |
                --------------------------------------------------------------------
            @MappingField
                ----------------------------------------------------------------------------------------
                |parameter           |reference                                                         |
                |field               |DB column name,if java param same as columnName,can unsign        |
                |primary             |if column is primary,set "1"                                      |
                |increment           |if column is autoincrement,set "1"(MySql/SqlServcer/Postgre/DB2)  |
                |sequenceName        |column insert with sequence,set sequenceName                      |
                |required            |column is not null                                                |
                |datatype            |if column is clob or blob,set "clob" or "blob"                    |
                |precise             |precise                                                           |
                |scale               |scale                                                             |
                |length              |length                                                            |
                ----------------------------------------------------------------------------------------
             when save or update,Entity will verify use Annotation
             
             Composite Primary Key,see example core/src/test/java/com/robin/core/test/model/TestMutiPK
                
        2.Dao Layer:
            use com.robin.core.base.dao.JdbcDao,no need to Generate new Class
            
        3.Service Layer
            exmaple: core/src/test/java/com/robin/core/test/service 
            base class:
            main function
                ---------------------------------------------------------------------------------
                |function name           |description                                           |
                |saveEntity              |insert to DB                                           |
                |updateEntity            |update to DB                                           |
                |deleteEntity            |delete by key array                                    |
                |getEntity               |select by id                                           |
                |queryByField            |query with specify column and value                    |
                |queryBySelectId         |query with config query                                |
                ---------------------------------------------------------------------------------    
        
        4.Query Configuration XML        
            4.1 Config Spring Bean
                using Config file                
                    <bean id="queryFactory" class="com.robin.core.query.util.QueryFactory" autowire="byName">
                        	<property name="xmlConfigPath" value="classpath:query"></property>
                    </bean>
                    
                using config class
                    @Bean(name="queryFactory")
                    public QueryFactory getQueryFactory(){
                         QueryFactory factory=new QueryFactory();
                         factory.setXmlConfigPath(queryConfigPath);
                         return factory;
                    }
            4.1 config XML File
                in  xmlConfigPath Path ,add xml file
                content
                    <SQLSCRIPT ID="$_GETCODESET">
                        <FROMSQL>from t_sys_code a,t_sys_codeset b where a.CS_ID=b.ID and ${queryString}</FROMSQL>
                    	<FIELD>a.ITEM_NAME as ITEMNAME,a.ITEM_VALUE as ITEMVALUE</FIELD>
                    </SQLSCRIPT>
                SQLSCRIPT ID refrer to   queryBySelectId selectId.example see core test.    
                
        4.Controller layer
            under development
            
        
    
 
 #
 Java简易框架 V1.0
 
 本工程由4个子模块构成
 
    1.core ：核心类；
 	2.comm：通用文件系统访问工具，文件系统支持本地、VFS和HDFS，文件格式支持csv/xml/json/avro/parquet/protobuf，
 	        压缩格式支持gzip/bzip2/snappy/lzo/zip/lzma/lz4。通用工具：包含简单的excel、word、powerpoint的工具；
 	3.hadooptool: 访问hadoop的通用工具类，包括访问HDFS,Hbase,Hive,Mongdb的工具；
 	4.web：支持Struts1、Struts2、和SpringMVC。
 
 特点
 
 	1.类似于mybatis的xml配置查询工具，配置方便；
 	2.支持jpa的注解和自定义方式的注解实现ORM操作；
 	3.方便实用的BaseAnnotationService，支持快速工程搭建；
 	4.通用的数据元和数据获取工具。
  
#
开发        
         
