use test;
DROP TABLE IF EXISTS `t_test`;
CREATE TABLE `t_test` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(32) default NULL,
  `code_desc` varchar(32) default NULL,
  `cs_id` int not null
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

INSERT INTO `t_test` VALUES ('1', 'user', 'code1',1);
INSERT INTO `t_test` VALUES ('2', 'type', 'code1',1);
INSERT INTO `t_test` VALUES ('3', 'gender', 'code1',1);
INSERT INTO `t_test` VALUES ('4', 'nation', 'code1',1);
INSERT INTO `t_test` VALUES ('5', 'country', 'code2',1);
INSERT INTO `t_test` VALUES ('6', 'city', 'code2',1);
INSERT INTO `t_test` VALUES ('7', 'district', 'code2',1);
INSERT INTO `t_test` VALUES ('8', 'street', 'code2',1);
INSERT INTO `t_test` VALUES ('9', 'floor', 'code2',1);



DROP TABLE IF EXISTS `testtablob`;
CREATE TABLE `testtablob` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(32) default NULL,
  `lob1` text,
  `lob2` longblob,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
