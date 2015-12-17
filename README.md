#
Simple Java Frame V1.0
Slightly Framework design to  support Spring based java program.

1.Introduction
	I.This project is base on Spring Framework and has four modules:
		Core: the core class include data access layer basic class(model,dao,service) and  etc.
		comm: base tool for read and write excel(support mutil-sheet and merge cell) and word,and some powerpoint tool
		hadooptool: comm tool to access to HDFS,Hbase,Hive,Mongdb and etc
		web: struts1,struts2 and springmvc support web component and required class.
   
    II. Special feature
        a.A user defined xml Query config system,similar to mybatis,but easy config.
        b.Support defined annotation or jpa annotation in JdbcDao with ORM.
        c. BaseAnnotationService can access DB with minimize code,and use transaction with annotation.
        d.A common db access meta and util,can access all kind of db.
        
 It is available under the terms of either the Apache Software License 2.0 or the Eclipse Public License 1.0.
 
 Project is still in progress.
 
 -----------------------------------------------------------------------------------------------------------------
 
 Java简易框架 V1.0
 
 本工程由4个子模块构成
	 1.core ：核心类；
 	2.comm： 通用工具类，包含简单的excel(支持多sheet和合并单元格)、word、powerpoint的生产工具；
 	3.hadooptool: 访问hadoop的通用工具类，包括访问HDFS,Hbase,Hive,Mongdb的工具；
 	4.web：支持Struts1、Struts2、和SpringMVC。
 
 特点
 	1.类似于mybatis的xml配置查询工具，配置方便；
 	2.支持jpa的注解和自定义方式的注解实现ORM操作；
 	3.方便实用的BaseAnnotationService，支持快速工程搭建；
 	4.通用的数据元和数据获取工具。
  
        
         
