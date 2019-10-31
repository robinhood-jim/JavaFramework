CREATE DATABASE IF NOT EXISTS test DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

use test;
DROP TABLE IF EXISTS `t_test`;
CREATE TABLE `t_test` (
  `id` bigint(20) NOT NULL,
  `name` varchar(32) default NULL,
  `code_desc` varchar(32) default NULL,
  `cs_id` int not null,
  create_time TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_test` VALUES ('1', 'user', 'code1',1),('2', 'type', 'code1',1),('3', 'gender', 'code1',1),('4', 'nation', 'code1',1),('5', 'country', 'code2',1),('6', 'city', 'code2',1),('7', 'district', 'code2',1),('8', 'street', 'code2',1),('9', 'floor', 'code2',1);


DROP TABLE IF EXISTS `testtablob`;
CREATE TABLE `testtablob` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(32) default NULL,
  `lob1` text,
  `lob2` longblob,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
CREATE TABLE `t_test_pkvarchar` (
  `name` varchar(32) NOT NULL,
  `code` varchar(32) default NULL,
  `tid` int(11) default NULL,
  `ts` timestamp NULL default NULL on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `t_test_mutilkey` (
  `id` bigint(20) NOT NULL auto_increment,
  `tname` varchar(32) NOT NULL,
  `tcode` int(11) NOT NULL,
  `outputval` double default NULL,
  `time` timestamp NULL default NULL on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`,`tname`,`tcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

use framset;
CREATE TABLE `t_sys_user_info` (
  `ID` int(11) NOT NULL auto_increment,
  `USER_ACCOUNT` varchar(64) default NULL,
  `USER_PASSWORD` varchar(64) default NULL,
  `USER_NAME` varchar(64) default NULL,
  `ACCOUNT_TYPE` char(1) default NULL,
  `USER_STATUS` char(1) default NULL,
  `ORDER_NO` int(11) default NULL,
  `DEPT_ID` int(11) default NULL,
  `ORG_ID` int(11) default NULL,
  `REMARK` varchar(256) default NULL,
  PRIMARY KEY  (`ID`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `t_sys_user_info`(USER_ACCOUNT,USER_PASSWORD,USER_NAME,ACCOUNT_TYPE,USER_STATUS,ORDER_NO) VALUES("admin","E10ADC3949BA59ABBE56E057F20F883E","adminstrator",1,1,1),("guest","E10ADC3949BA59ABBE56E057F20F883E","guest",2,1,1)
