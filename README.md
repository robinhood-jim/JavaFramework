#
Simple Java Frame V1.0
Slightly Framework design to  support Spring based java or Bigdata program.

[![Build Status](https://travis-ci.org/robinhood-jim/JavaFramework.svg?branch=master)](https://travis-ci.org/robinhood-jim/JavaFramework)
[![CircleCI](https://circleci.com/gh/robinhood-jim/JavaFramework.svg?style=svg)](https://circleci.com/gh/robinhood-jim/JavaFramework)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)

1.Introduction

	I.This project is base on Spring Framework and has four modules:
		|----------------------------------------------------------------------------------------------------|
		| Module   | Description                                                                             |
		|----------------------------------------------------------------------------------------------------|
		| Core     | the core class include data access layer basic class(model,dao,service) and  etc.       |
		|----------------------------------------------------------------------------------------------------|
		| Comm     | FileSystem Access tool(local/vfs),support FileFormat(csv/xml/json/avro/parquet/protobuf)|
		|          | ,support Compress Format(gzip/bzip2/snappy/lzo/zip/lzma/lz4)                            |
		|          | ,read and write excel,read word or PowerPoint                                           |
		|----------------------------------------------------------------------------------------------------|
		|Hadooptool|FileSystem Access tool(hdfs), comm tool to access to HDFS,Hbase,Hive,Mongdb and etc      |
		|----------------------------------------------------------------------------------------------------|
		|Example   |springmvc config based and spring boot based Example;                                    |
		|----------------------------------------------------------------------------------------------------|
		|Web       |struts1,struts2 and springmvc support web component and required class.                  |
		|----------------------------------------------------------------------------------------------------|
		|Webui     |Spring Boot with Oauth2 Thymeleaf Example;                                               |
		|----------------------------------------------------------------------------------------------------|
		|Estool    | ElasticSearch Comm Query tool                                                           |
		|----------------------------------------------------------------------------------------------------|
		|Tracer    | Zipkin Brave tracing，Can trace All Database and Record parameters                      |
		|----------------------------------------------------------------------------------------------------|
   
    II. Special feature
        a.A user defined xml Query config system,similar to mybatis,but easy config.
        b.Support defined annotation or jpa annotation in JdbcDao with ORM.
        c. BaseAnnotationService can access DB with minimize code,and use transaction with annotation.
        d.A common db access meta and util,can access all kind of db.
		e.Spring cloud based WebUI
		f.support Hadoop plateform
        
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
                xmlConfigPath support three input
                    I.if not assign value,read config xml from classpath:queryConfig
                    II. classpath:query  
                    III.jarpath:query    read config from jar base Path with relative path
                    
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
		5.mybatis like QueryMapper
			<mapper namespace="com.robin.test.query1">
			<resultMap id="rsMap1" type="com.robin.core.test.model.TestModel">
				<result column="id" property="id" jdbcType="BIGINT" />
				<result column="name" property="name" jdbcType="VARCHAR" />
				<result column="code_desc" property="description" jdbcType="VARCHAR" />
				<result column="cs_id" property="csId" jdbcType="BIGINT" />
				<result column="create_time" property="createTime" jdbcType="TIMESTAMP" />
			</resultMap>
			<sql id="sqlpart1">
				id,name,code_desc,cs_id,create_time
			</sql>
			<sql id="sqlpart2">
				name,code_desc,cs_id,create_time
			</sql>
			<select id="select1" resultMap="rsMap1">
				select
				<include refid="sqlpart1" />
				 from t_test where 1=1
				<script lang="js" id="test1" resultMap="rsMap1">
					var returnstr="";
					if(name!=null){
						returnstr+=" and name like :name";
					}
					if(description!=null){
						returnstr+=" and code_desc like :description";
					}
					if(csId!=null){
						returnstr+=" and cs_id=:csId";
					}
					returnstr;
				</script>
			</select>
			<insert id="insert1" resultMap="rsMap1" parameterType="com.robin.core.test.model.TestModel" useGeneratedKeys="true" keyProperty="id">
				insert into t_test (
				<script lang="js" id="test2">
					var returnstr="";
					if(name!=null){
						returnstr+="name,"
					}
					if(description!=null){
						returnstr+="code_desc,"
					}
					if(csId!=null){
						returnstr+="cs_id,"
					}
					returnstr+="create_time";
				</script>
				) values (
				<script lang="js" id="test3">
					var returnstr="";
					if(name!=null){
						returnstr+=":name,";
					}
					if(description!=null){
						returnstr+=":description,";
					}if(csId!=null){
						returnstr+=":csId,";
					}
					returnstr+="sysdate()";
				</script>
				)
			</insert>
			<batch id="batch1" resultMap="rsMap1" parameterType="com.robin.core.test.model.TestModel">
				insert into t_test
				<include refid="sqlpart2" />
				values (:name,:description,:csId,sysdate())
			</batch>
			<update id="update1" resultMap="rsMap1" parameterType="com.robin.core.test.model.TestModel">
				update t_test set
				<script lang="js" id="test4">
					var returnstr="";
					if(name!=null){
						returnstr+="name=:name,";
					}
					if(description!=null){
						returnstr+="code_desc=:description,";
					}
					if(csId!=null){
						returnstr+="cs_id=:csId,";
					}
					returnstr.substr(0,returnstr.length-1);
				</script>
				  where id=:id
			</update>
			<delete id="del1" parameterType="">

			</delete>
		</mapper>
		no need to generate Mapper class,Only need QueryMapper xml file
		exmaple: core/src/test/java/com/robin/core/test/db/JdbcDaoTest  testQueryAndInsertMapper
		
		insert update delete segement support script rather than ognl
                
        4.Controller layer
            BaseCrudController basic Single Model base controller, exmaple see example/config-exmaple
			BaseCrudDhtmlxController dhtmlxGrid base controller,make it easy to develop web and controller.
			
		upon feature aim to simplify the work to develop standard MVC java code.
            
    II. Bigdata supprot
		hadooptool: 
			HDFS tool: com.robin.hadoop.hdfs  can access HDFS with kerberos security
			Hbase tool: com.robin.hadoop.hbase hbase tool 
			Cassandra tool : CassandraUtils
			
	III. Spring cloud support
	WebUI simple webui base on dhtmlxGrid 5.1 with spring boot native 
	related project in my another project microservices
   
         
