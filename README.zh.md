# SimpleJavaFrame

[![Build Status](https://travis-ci.org/robinhood-jim/JavaFramework.svg?branch=master)](https://travis-ci.org/robinhood-jim/JavaFramework)
[![CircleCI](https://circleci.com/gh/robinhood-jim/JavaFramework.svg?style=svg)](https://circleci.com/gh/robinhood-jim/JavaFramework)

#### 介绍
基于SpringBoot 和Spring2 与3两种模式的简易框架，ORM包含自定义的类似SpringData Jpa的ORM实现与类似Mybatis的可配置式SQL查询工具，hadoop工具支持基本的大数据工具包，统一的数据定义和数据访问接口，支持数据文件格式(CSV/JSON/XML/AVRO/PARQUET/PROTOBUF),数据源支持（本地/HDFS/APACHE VFS/）

#### 软件架构
软件架构说明

目前框架由4个模块组成

    1.Core：核心包，包含Model/DAO/Service及ORM的基础类及DB Dialect。数据库Dump和Import的工具包等
    2.Common：包含统一资源访问的接口定义和Apache VFS与本地资源的实现
    3.hadooptool：包含大数据相关的基本工具类和统一资源访问的大数据实现
    4.web：基于strut2和SpringMVC的基础类及Spring message的基础类（支持多国语言）

core与common工程由ProGuard进行混淆

#### 安装教程

1.在根目录下运行 mvn clean install -Dmaven.test.skip=true -e -U


#### 使用说明

开发说明
    
    I.Model:支持JPA和自定义的Annotation,可支持符合主键，自增长字段保存后自动回填相应字段
        
        1.Example:
            包: core/src/test/java/com/robin/core/test/model
            TestJPaModel 基于JPA的modelmodel
            TestModel 基于自定义的Annotation
            TestLob  支持lob操作的modelmodel
        开发：
            1.创建类继承BaseObject;
            2.对应字段用MappingField标注
            实体标识说明
            @MappingEnity 
                 -------------------------------------------------------------------
                |参数                 |说明                                           |
                |table               |表名                                           |
                |schema              |schema                                         | 
                |jdbcDao             |多数据源支持(详见core的testcase)                 |
                --------------------------------------------------------------------
            字段标识说明
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
             在新增和修改的时候，实体会根据以上的类型和required标识进行验证
             
             符合主键,见例子 core/src/test/java/com/robin/core/test/model/TestMutiPK
                
    II.DAO层:
            使用 com.robin.core.base.dao.JdbcDao,没有特殊业务需要，无需另外创建
            1.配置式SQL查询     
            类似于mybatis的配置式查询，简化了相应的配置，去掉了Map转Model的操作，可支持PreparedStatement与NamedPreparedStatment，
            及最简单的替换SQL(有注入攻击风险)   
            使用
            1.1 配置SpringSpring
                在配置文件中添加                
                    <bean id="queryFactory" class="com.robin.core.query.util.QueryFactory" autowire="byName">
                        	<property name="xmlConfigPath" value="classpath:query"></property>
                    </bean>
                    
                springboot配置
                    @Bean(name="queryFactory")
                    public QueryFactory getQueryFactory(){
                         QueryFactory factory=new QueryFactory();
                         factory.setXmlConfigPath(queryConfigPath);
                         return factory;
                    }
            1.2 配置文件
                在xmlConfigPath 目录下，添加任意xml文件
                内容如下
                    <SQLSCRIPT ID="$_GETCODESET">
                        <FROMSQL>from t_sys_code a,t_sys_codeset b where a.CS_ID=b.ID and ${queryString}</FROMSQL>
                    	<FIELD>a.ITEM_NAME as ITEMNAME,a.ITEM_VALUE as ITEMVALUE</FIELD>
                    </SQLSCRIPT>
                调用Service的queryBySelectId，传入ID和参数，可以实现查询功能
            
            2.不同数据库的支持
            配置方言使用的工具类
                在配置文件中添加
                <bean id="sqlGen" class="com.robin.core.sql.util.MysqlSqlGen" autowire="byName"></bean>
                支持的数据库包括
                MysqlSqlGen                 Mysql
                OracleSqlGen                Oracle
                Db2SqlSqlGen                Db2
                SqlServcer2005Gen           SqlServcer2005
                PostgreSqlGen               Postgre
                SybaseSqlGen                Sybase

            
    III.Service层
            实例: core/src/test/java/com/robin/core/test/service 
            主要方法
                ---------------------------------------------------------------------------------
                |function name           |description                                           |
                |saveEntity              |insert to DB                                           |
                |updateEntity            |update to DB                                           |
                |deleteEntity            |delete by key array                                    |
                |getEntity               |select by id                                           |
                |queryByField            |query with specify column and value                    |
                |queryBySelectId         |query with config query                                |
                ---------------------------------------------------------------------------------    
        以上设计参照JPA标准，提供的方法进行了扩充

        
                  
                
    IV.Controller层
            开发中

    V.统一资源访问

        通过封装，提供一致的接口，提供支持ApcheVFS/HDFS/LOCAL数据源 对应数据文件(支持CSV/XML/JSON/AVRO/PARQUET/PROTOBUF，支持压缩)的统一读取和写入，目前不支持文件切分处理
        实例代码:详见common工程的testcase com.robin.comm.test.TestJsonGen TestJsonRead
                hadooptool：com.robin.test.TestResourceGen TestParquetWriter

