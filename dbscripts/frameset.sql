

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_base_codetemplate
-- ----------------------------
DROP TABLE IF EXISTS `t_base_codetemplate`;
CREATE TABLE `t_base_codetemplate`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `template_path` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_codetemplate
-- ----------------------------
INSERT INTO `t_base_codetemplate` VALUES (1, 'custommodelclass', 'customValueObject.ftl');
INSERT INTO `t_base_codetemplate` VALUES (2, 'customdaoclass', 'annotationDao.ftl');
INSERT INTO `t_base_codetemplate` VALUES (3, 'jpamodelclass', 'jpaValueObject.ftl');
INSERT INTO `t_base_codetemplate` VALUES (4, 'customserviceclass', 'annotationService.ftl');
INSERT INTO `t_base_codetemplate` VALUES (5, 'dhtmlxinitpack', '/etc/dhtmlxinit.zip');
INSERT INTO `t_base_codetemplate` VALUES (6, 'struts2class', 'struts2Action.ftl');
INSERT INTO `t_base_codetemplate` VALUES (7, 'dhtmlxaddjsp', 'dhtmlxjspAdd.ftl');
INSERT INTO `t_base_codetemplate` VALUES (8, 'dhtmlxlistjsp', 'dhtmlxjspList.ftl');
INSERT INTO `t_base_codetemplate` VALUES (9, 'dhtmlxeditjsp', 'dhtmlxjspEdit.ftl');
INSERT INTO `t_base_codetemplate` VALUES (10, 'mvcclass', 'springmvc.ftl');
INSERT INTO `t_base_codetemplate` VALUES (11, 'maven', 'mavenconfig.ftl');

-- ----------------------------
-- Table structure for t_base_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_base_datasource`;
CREATE TABLE `t_base_datasource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `db_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '1- mysql\r\n            2-oracle\r\n            3- db2\r\n            4- pohoniex',
  `driver_id` bigint(20) NULL DEFAULT NULL,
  `host_ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `port` int(11) NULL DEFAULT NULL,
  `database_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `encode` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `conn_url` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_datasource
-- ----------------------------
INSERT INTO `t_base_datasource` VALUES (1, 'test', 'MySql', 1, '172.16.102.111', 3388, 'activiti', 'UTF-8', 'jdbc:mysql://node8:3306/cdm?useUnicode=true&amp;characterEncoding=UTF-8', 'dev', 'dev123');
INSERT INTO `t_base_datasource` VALUES (2, 'test1', 'Oracle', 2, '192.168.143.189', 1521, 'etl', 'UTF-8', 'jdbc:oracle:thin:@192.168.143.189:1521:etl', 'etl', 'Etl987');
INSERT INTO `t_base_datasource` VALUES (3, 'hnbastest', 'DB2', 3, '192.168.140.142', 50000, 'HNBASDB', 'UTF-8', 'jdbc:db2://192.168.140.142:50000/HNBASDB', NULL, NULL);
INSERT INTO `t_base_datasource` VALUES (4, 'local', 'MySql', 1, '127.0.0.1', 3316, 'cms', 'UTF-8', NULL, 'root', 'root');

-- ----------------------------
-- Table structure for t_base_dbdriver
-- ----------------------------
DROP TABLE IF EXISTS `t_base_dbdriver`;
CREATE TABLE `t_base_dbdriver`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `db_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `driver_class` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `connurl` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jars` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `default_port` int(11) NULL DEFAULT NULL,
  `driver_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `maven_tag` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `maven_version` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_dbdriver
-- ----------------------------
INSERT INTO `t_base_dbdriver` VALUES (1, '1', 'com.mysql.jdbc.Driver', 'jdbc:mysql://${HOST}:${PORT}/${DB}?useUnicode=true&amp;characterEncoding=${ENCODE}', 'mysql.jar', 3306, 'MySql5', NULL, '5.1.20');
INSERT INTO `t_base_dbdriver` VALUES (2, '2', NULL, 'jdbc:oracle:thin:@${HOST}:${PORT}:${DB}', 'ojdbc14.jar', 1521, 'Oracle10', NULL, '10.0');
INSERT INTO `t_base_dbdriver` VALUES (3, '4', NULL, 'jdbc:db2://${HOST}:${PORT}/${DB}', 'db2.jar', 50000, 'DB2 9.0', NULL, NULL);

-- ----------------------------
-- Table structure for t_base_entitymapping
-- ----------------------------
DROP TABLE IF EXISTS `t_base_entitymapping`;
CREATE TABLE `t_base_entitymapping`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proj_id` bigint(20) NULL DEFAULT NULL,
  `source_id` bigint(20) NULL DEFAULT NULL,
  `db_schema` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `entity_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `description` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `java_class` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `spring_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dao_packagename` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `model_packagename` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `service_packagename` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `web_packagename` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dao_configpath` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `service_configpath` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `web_configpath` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `web_path` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `page_path` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `pk_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `gen_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sequence_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_entitymapping
-- ----------------------------
INSERT INTO `t_base_entitymapping` VALUES (2, 1, 1, 'cdm', 'sys_dic', '字典', NULL, 'SysDict', 'sysDict', 'Sysdic', 'cn.com.talkweb.test2.model', 'cn.com.talkweb.test2.service', 'cn.com.talkweb.test2.web', '/config/spring/applicationContext-sysdao.xml', '/config/spring/applicationContext-sysservice.xml', NULL, '/system/sysdic.action', '/pages/system/', '1', '3', NULL);
INSERT INTO `t_base_entitymapping` VALUES (3, 1, 1, 'cdm', 'sys_user', 'tt', NULL, 'test3', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '3', NULL);
INSERT INTO `t_base_entitymapping` VALUES (6, 1, 4, 'cms', 't_cms_querydatameta', '查询元数据', NULL, 'QueryDataMeta', 'queryDataMeta', NULL, 'com.robin.cms.model', 'com.robin.cms.service', 'com.robin.cms.web', NULL, NULL, NULL, '/back/querymeta', '/back/querymeta/', '2', '3', NULL);

-- ----------------------------
-- Table structure for t_base_fieldmapping
-- ----------------------------
DROP TABLE IF EXISTS `t_base_fieldmapping`;
CREATE TABLE `t_base_fieldmapping`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proj_id` bigint(20) NULL DEFAULT NULL,
  `source_id` bigint(20) NULL DEFAULT NULL,
  `entity_id` bigint(20) NULL DEFAULT NULL,
  `field_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `data_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mapping_field` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mapping_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_primary` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_genkey` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_nullable` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_sequence` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sequence_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `display_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'true',
  `show_in_grid` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'true',
  `show_in_query` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'true',
  `is_editable` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'true',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_fieldmapping
-- ----------------------------
INSERT INTO `t_base_fieldmapping` VALUES (21, 1, 1, 2, 'typeid', '1', 'type_id', '', '1', '0', '0', NULL, NULL, '分类ID', '1', '0', '0', '0');
INSERT INTO `t_base_fieldmapping` VALUES (22, 1, 1, 2, 'itemid', '1', 'item_id', '', '1', '0', '0', NULL, NULL, '项目ID', '1', '0', '0', '0');
INSERT INTO `t_base_fieldmapping` VALUES (23, 1, 1, 2, 'itemname', '1', 'item_name', '', '0', '0', '0', NULL, NULL, '项目名称', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (24, 1, 1, 2, 'description', '1', 'description', '', '0', '0', '1', NULL, NULL, '描述', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (25, 1, 1, 2, 'sortid', '4', 'sort_id', '', '0', '0', '1', NULL, NULL, '排序号', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (36, 1, 4, 6, 'id', '4', 'id', '2', '1', '1', '0', NULL, NULL, '主键ID', '1', '0', '0', '0');
INSERT INTO `t_base_fieldmapping` VALUES (37, 1, 4, 6, 'metaEname', '1', 'metaEname', '7', '0', '0', '0', NULL, NULL, '数据元英文名', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (38, 1, 4, 6, 'metaType', '4', 'metaType', '2', '0', '0', '0', NULL, NULL, '数据元类型', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (39, 1, 4, 6, 'metaLength', '4', 'metaLength', '2', '0', '0', '0', NULL, NULL, '长度', '1', '1', '1', '1');
INSERT INTO `t_base_fieldmapping` VALUES (40, 1, 4, 6, 'remark', '1', 'remark', '7', '0', '0', '1', NULL, NULL, '备注', '1', '1', '1', '1');

-- ----------------------------
-- Table structure for t_base_jar
-- ----------------------------
DROP TABLE IF EXISTS `t_base_jar`;
CREATE TABLE `t_base_jar`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `maven_group` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `maven_artifact` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_jar
-- ----------------------------
INSERT INTO `t_base_jar` VALUES (1, 'comm Collect', '3.2.1', 'commons-collections-3.2.1.jar', 'commons-lang', 'commons-lang');
INSERT INTO `t_base_jar` VALUES (2, 'comm BeanUtil', '1.8.0', 'commons-beanutils-1.8.0.jar', 'commons-beanutils', 'commons-beanutils');
INSERT INTO `t_base_jar` VALUES (3, 'comm Codec', '1.4', 'commons-codec-1.4.jar', 'commons-codec', 'commons-codec');
INSERT INTO `t_base_jar` VALUES (4, 'comm Dbcp', '1.1', 'commons-dbcp.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (5, 'comm Digest', '1.8', 'commons-digester-1.8.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (6, 'comm IO', '2.1', 'commons-io-2.1.jar', 'commons-io', 'commons-io');
INSERT INTO `t_base_jar` VALUES (7, 'comm lang', '2.5', 'commons-lang-2.5.jar', 'commons-lang', 'commons-lang');
INSERT INTO `t_base_jar` VALUES (8, 'comm logging', '1.1.1', 'commons-logging-1.1.1.jar', 'commons-logging', 'commons-logging');
INSERT INTO `t_base_jar` VALUES (9, 'comm pool', '1.4', 'commons-pool-1.4.jar', 'commons-pool', 'commons-pool');
INSERT INTO `t_base_jar` VALUES (10, 'comm math', '3.2', 'commons-math3-3.2.jar', 'org.apache.commons', 'commons-math3');
INSERT INTO `t_base_jar` VALUES (11, 'comm net', '1.4.1', 'commons-net-1.4.1.jar', 'commons-net', 'commons-net');
INSERT INTO `t_base_jar` VALUES (12, 'log4j', '1.2.17', 'log4j-1.2.17.jar', 'log4j', 'log4j');
INSERT INTO `t_base_jar` VALUES (13, 'Spring aop', '4.3.18', 'springaop.jar', 'org.springframework', 'spring-aop');
INSERT INTO `t_base_jar` VALUES (14, 'Spring Beans', '4.3.18', 'springbeans.jar', 'org.springframework', 'spring-beans');
INSERT INTO `t_base_jar` VALUES (15, 'Spring Core', '4.3.18', 'springcore.jar', 'org.springframework', 'spring-core');
INSERT INTO `t_base_jar` VALUES (16, 'Spring Aspect', '4.3.18', 'springaspect.jar', 'org.springframework', 'spring-aspect');
INSERT INTO `t_base_jar` VALUES (17, 'Spring context', '4.3.18', 'springcontext.jar', 'org.springframework', 'spring-context');
INSERT INTO `t_base_jar` VALUES (18, 'Spring JDBC', '4.3.18', 'springjdbc.jar', 'org.springframework', 'spring-jdbc');
INSERT INTO `t_base_jar` VALUES (19, 'Spring Expression', '4.3.18', 'springexpression.jar', 'org.springframework', 'spring-expression');
INSERT INTO `t_base_jar` VALUES (20, 'Spring JMS', '4.3.18', 'springjms.jar', 'org.springframework', 'spring-jms');
INSERT INTO `t_base_jar` VALUES (21, 'Spring Asm', '4.3.18', 'springasm.jar', 'org.springframework', 'spring-asm');
INSERT INTO `t_base_jar` VALUES (22, 'Spring web', '4.3.18', 'springweb.jar', 'org.springframework', 'spring-web');
INSERT INTO `t_base_jar` VALUES (23, 'Spring Transaction', '4.3.18', 'springtx.jar', 'org.springframework', 'spring-tx');
INSERT INTO `t_base_jar` VALUES (24, 'Spring context Support', '4.3.18', 'springcontextsupport.jar', 'org.springframework', 'spring-context-support');
INSERT INTO `t_base_jar` VALUES (25, 'Spring ORM', '4.3.18', 'springorm.jar', 'org.springframework', 'spring-orm');
INSERT INTO `t_base_jar` VALUES (26, 'Spring OXM', '4.3.18', 'springoxm.jar', 'org.springframework', 'spring-oxm');
INSERT INTO `t_base_jar` VALUES (27, 'Spring Test', '4.3.18', 'springtest.jar', 'org.springframework', 'spring-test');
INSERT INTO `t_base_jar` VALUES (28, 'Spring security', '4.3.18', 'springsecurity.jar', 'org.springframework', 'spring-security');
INSERT INTO `t_base_jar` VALUES (29, 'TWFRAME', '1.0-SNAPSHOT', 'twmtfrm.jar', 'com.robin', 'core');
INSERT INTO `t_base_jar` VALUES (30, 'Strust Core', '2.2', 'struts2-core-2.2.1.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (31, 'Struts Spring', '2.2', 'struts2-spring-plugin-2.2.1.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (32, 'Struts conversion', '2.2', 'struts2-convention-plugin-2.2.1.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (33, 'OgnL', '2.6.11', 'ognl-2.6.11.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (34, 'ezmorph', '1.0.6', 'ezmorph-1.0.6.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (35, 'freeMarker', '2.3.8', 'freemarker-2.3.8.jar', 'freemarker', 'freemarker');
INSERT INTO `t_base_jar` VALUES (36, 'xwork', '2.2.1', 'xwork-core-2.2.1.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (37, 'neethi', '2.0.4', 'neethi-2.0.4.jar', NULL, NULL);
INSERT INTO `t_base_jar` VALUES (38, 'Spring WebMVC', '3.2.5', 'springwebmvc.jar', 'org.springframework', 'spring-mvc');

-- ----------------------------
-- Table structure for t_base_javalibrary
-- ----------------------------
DROP TABLE IF EXISTS `t_base_javalibrary`;
CREATE TABLE `t_base_javalibrary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `library_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `zip_file` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_javalibrary
-- ----------------------------
INSERT INTO `t_base_javalibrary` VALUES (1, 'Apache Comm', '1', 'apache.zip');
INSERT INTO `t_base_javalibrary` VALUES (2, 'Spring', '3.2.5', 'spring.zip');
INSERT INTO `t_base_javalibrary` VALUES (3, 'TWFrame', '1.0', 'twfrm.zip');
INSERT INTO `t_base_javalibrary` VALUES (4, 'Struts', '2.2', 'struts2.zip');

-- ----------------------------
-- Table structure for t_base_javalibrary_r
-- ----------------------------
DROP TABLE IF EXISTS `t_base_javalibrary_r`;
CREATE TABLE `t_base_javalibrary_r`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `library_id` bigint(20) NULL DEFAULT NULL,
  `jar_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_javalibrary_r
-- ----------------------------
INSERT INTO `t_base_javalibrary_r` VALUES (1, 1, 1);
INSERT INTO `t_base_javalibrary_r` VALUES (2, 1, 2);
INSERT INTO `t_base_javalibrary_r` VALUES (6, 1, 6);
INSERT INTO `t_base_javalibrary_r` VALUES (7, 1, 7);
INSERT INTO `t_base_javalibrary_r` VALUES (8, 1, 8);
INSERT INTO `t_base_javalibrary_r` VALUES (9, 1, 9);
INSERT INTO `t_base_javalibrary_r` VALUES (10, 1, 10);
INSERT INTO `t_base_javalibrary_r` VALUES (11, 1, 11);
INSERT INTO `t_base_javalibrary_r` VALUES (12, 1, 12);
INSERT INTO `t_base_javalibrary_r` VALUES (13, 2, 13);
INSERT INTO `t_base_javalibrary_r` VALUES (14, 2, 14);
INSERT INTO `t_base_javalibrary_r` VALUES (15, 2, 15);
INSERT INTO `t_base_javalibrary_r` VALUES (16, 2, 16);
INSERT INTO `t_base_javalibrary_r` VALUES (17, 2, 17);
INSERT INTO `t_base_javalibrary_r` VALUES (18, 2, 18);
INSERT INTO `t_base_javalibrary_r` VALUES (19, 2, 19);
INSERT INTO `t_base_javalibrary_r` VALUES (20, 2, 20);
INSERT INTO `t_base_javalibrary_r` VALUES (21, 2, 21);
INSERT INTO `t_base_javalibrary_r` VALUES (22, 2, 22);
INSERT INTO `t_base_javalibrary_r` VALUES (23, 2, 23);
INSERT INTO `t_base_javalibrary_r` VALUES (24, 2, 24);
INSERT INTO `t_base_javalibrary_r` VALUES (25, 2, 25);
INSERT INTO `t_base_javalibrary_r` VALUES (26, 2, 26);
INSERT INTO `t_base_javalibrary_r` VALUES (27, 2, 27);
INSERT INTO `t_base_javalibrary_r` VALUES (29, 3, 29);
INSERT INTO `t_base_javalibrary_r` VALUES (35, 4, 35);
INSERT INTO `t_base_javalibrary_r` VALUES (38, 3, 38);

-- ----------------------------
-- Table structure for t_base_projectinfo
-- ----------------------------
DROP TABLE IF EXISTS `t_base_projectinfo`;
CREATE TABLE `t_base_projectinfo`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proj_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `proj_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `proj_type` bigint(20) NULL DEFAULT NULL,
  `description` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `company` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `use_springmvc` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0',
  `struts_version` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `spring_version` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `presist_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '1- hibernate\r\n            2- jpa\r\n            3- default',
  `dao_configfile` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `project_basepath` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `service_configfile` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `src_basepath` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `web_basepath` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `use_annotation` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `annotation_package` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `use_maven` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `webframe_id` bigint(20) NULL DEFAULT NULL,
  `datasource_id` bigint(20) NULL DEFAULT NULL,
  `author` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `team_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `team_url` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jar_man_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `credential_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_projectinfo
-- ----------------------------
INSERT INTO `t_base_projectinfo` VALUES (1, '测试工程', 'test', 2, 'dddd', 'TW', '1', '2.2', NULL, '3', NULL, 'E:/javarun/test2/', NULL, 'src', '', '1', 'com.robin.cms', '0', 1, 4, 'robinjim', '2', 'http://localhost:3000/robinjim/testproj.git', '1', NULL);

-- ----------------------------
-- Table structure for t_base_projrelay
-- ----------------------------
DROP TABLE IF EXISTS `t_base_projrelay`;
CREATE TABLE `t_base_projrelay`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proj_id` bigint(20) NULL DEFAULT NULL,
  `library_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_projrelay
-- ----------------------------
INSERT INTO `t_base_projrelay` VALUES (5, 1, 1);
INSERT INTO `t_base_projrelay` VALUES (6, 1, 2);
INSERT INTO `t_base_projrelay` VALUES (7, 1, 3);
INSERT INTO `t_base_projrelay` VALUES (8, 1, 4);

-- ----------------------------
-- Table structure for t_base_team_repository
-- ----------------------------
DROP TABLE IF EXISTS `t_base_team_repository`;
CREATE TABLE `t_base_team_repository`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `team_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `auth_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `auth_pwd` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_base_team_repository
-- ----------------------------
INSERT INTO `t_base_team_repository` VALUES (1, '2', 'robinjim', 'robin7704');

-- ----------------------------
-- Table structure for t_meta_global_resource
-- ----------------------------
DROP TABLE IF EXISTS `t_meta_global_resource`;
CREATE TABLE `t_meta_global_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `res_type` bigint(20) NULL DEFAULT NULL,
  `res_name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `protocol` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `db_type` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ip_address` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `port` int(11) NULL DEFAULT NULL,
  `db_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `username` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jdbc_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_pool` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `max_active` int(11) NULL DEFAULT NULL,
  `max_idle` int(11) NULL DEFAULT NULL,
  `file_path` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cluster_code` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_format` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `record_content` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_meta_global_resource
-- ----------------------------
INSERT INTO `t_meta_global_resource` VALUES (1, 4, 'Db1', NULL, 'MySql', '172.16.200.218', 0, 'wisdombus2.0_basedata', 'wisdombus', 'MiCUWcYcJI2EcM1k', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_meta_global_resource` VALUES (2, 1, 'hdfs1', 'hdfs', NULL, '172.16.200.201', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_meta_global_resource` VALUES (3, 3, 'sftp1', 'sftp', NULL, '172.16.200.62', 22, NULL, 'luoming', '123456', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_meta_hadoop_cfg
-- ----------------------------
DROP TABLE IF EXISTS `t_meta_hadoop_cfg`;
CREATE TABLE `t_meta_hadoop_cfg`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `zk_ips` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `zk_port` int(11) NULL DEFAULT NULL,
  `hive_server_ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hive_server_port` int(11) NULL DEFAULT NULL,
  `hive_server_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hive_server_pwd` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hdfs_server_ip` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hdfs_server_port` int(11) NULL DEFAULT NULL,
  `is_ha` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ha_nameserver` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `standby_server` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mr_frame` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `yarn_resource_ips` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `yarn_resource_port` int(11) NULL DEFAULT NULL,
  `resource_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_meta_hadoop_cfg
-- ----------------------------
INSERT INTO `t_meta_hadoop_cfg` VALUES (1, 'gateway', '172.16.200.221,172.16.200.222,172.16.200.224', 2181, NULL, NULL, NULL, NULL, '172.16.200.201', 8020, '0', NULL, NULL, NULL, NULL, NULL, 2);

-- ----------------------------
-- Table structure for t_org_responsibility
-- ----------------------------
DROP TABLE IF EXISTS `t_org_responsibility`;
CREATE TABLE `t_org_responsibility`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `create_user` bigint(20) NULL DEFAULT NULL,
  `update_user` bigint(20) NULL DEFAULT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `org_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for t_sys_code
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_code`;
CREATE TABLE `t_sys_code`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CS_ID` int(11) NOT NULL,
  `ITEM_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ITEM_VALUE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `CODE_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORDER_NO` int(11) NULL DEFAULT NULL,
  `CREATE_BY` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `MODIFY_BY` int(32) NULL DEFAULT NULL,
  `CREATE_DATE` date NULL DEFAULT NULL,
  `MODIFY_DATE` date NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_code
-- ----------------------------
INSERT INTO `t_sys_code` VALUES (1, 1, 'MySql', 'MySql', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (2, 1, 'Oracle', 'Oracle', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (3, 1, 'SqlServer', 'SqlServer', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (4, 1, 'DB2', 'DB2', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (5, 2, '是', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (6, 2, '否', '0', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (7, 3, 'Hibernate', '1', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (8, 3, 'Jpa', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (9, 3, 'Default', '3', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (10, 4, 'Dhtmlx', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (11, 4, 'EasyUI', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (12, 4, 'ExtJs', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (13, 5, 'Java工程', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (14, 5, 'Web工程', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (15, 5, 'Flex工程', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (16, 6, 'native', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (17, 6, 'assigned', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (18, 6, 'increment', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (19, 6, 'sequence', '4', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (20, 7, 'Long', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (21, 7, 'Integer', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (22, 7, 'String', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (23, 8, 'Long', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (24, 8, 'Integer', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (25, 8, 'Double', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (26, 8, 'Float', '4', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (27, 8, 'Timestamp', '5', '1', 5, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (28, 8, 'Date', '6', '1', 6, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (29, 8, 'String', '7', '1', 7, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (30, 9, 'String', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (31, 9, 'Date', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (32, 9, 'Numeric', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (33, 9, 'Integer', '4', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (34, 9, 'BigInt', '5', '1', 5, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (35, 9, 'Double', '6', '1', 6, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (36, 9, 'BOOLEAN', '7', '1', 7, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (37, 9, 'Binary', '8', '1', 8, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (38, 10, 'java.lang.Long', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (39, 10, 'java.lang.Integer', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (40, 10, 'java.lang.Double', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (41, 10, 'java.lang.Float', '4', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (42, 10, 'java.sql.Timestamp', '5', '1', 5, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (43, 10, 'java.util.Date', '6', '1', 6, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (44, 10, 'java.lang.String', '7', '1', 7, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (45, 11, 'Text', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (46, 11, 'Select', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (47, 11, 'Date', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (48, 11, 'checkbox', '4', '1', 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (49, 11, 'Null', '5', '1', 5, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (50, 12, 'SVN', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (51, 12, 'Git', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (52, 13, '系统用户', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (53, 13, '机构用户', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (54, 12, 'CVS', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (55, 14, 'MAVEN', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (56, 14, 'GRADLE', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (57, 14, 'SBT', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (58, 15, '系统角色', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (59, 15, '一般角色', '2', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (60, 16, '有效', '1', '1', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (61, 16, '无效', '0', '1', 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (62, 16, '失效', '2', '1', 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_code` VALUES (63, 13, '一般用户', '3', '1', 3, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_codeset
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_codeset`;
CREATE TABLE `t_sys_codeset`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `EN_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `CN_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `CS_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `CREATE_BY` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `MODIFY_BY` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `CREATE_DATE` date NULL DEFAULT NULL,
  `MODIFYDATE` date NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_codeset
-- ----------------------------
INSERT INTO `t_sys_codeset` VALUES (1, 'DBTYPE', '数据库类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (2, 'YNTYPE', '是否', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (3, 'PRESISTTYPE', '持久化类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (4, 'WEBFRAME', '前台框架', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (5, 'PROJECTTYPE', '工程类型1', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (6, 'PKGEN', '主键生成方式', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (7, 'PKTYPE', '主键类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (8, 'FIELDMAP', '实体映射类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (9, 'DATATYPE', '数据类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (10, 'DBDISPLAY', 'ds', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (11, 'FIELDDISPLAY', '字段显示方式', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (12, 'TEAMTYPE', '团队开发模式', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (13, 'ACCOUNTTYPE', '用户类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (14, 'JARMANTYPE', 'JAR管理工具', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (15, 'ROLETYPE', '角色类型', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_codeset` VALUES (16, 'VALIDTAG', '有效标志', '1', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_dept_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dept_info`;
CREATE TABLE `t_sys_dept_info`  (
  `ID` int(11) NOT NULL,
  `DEPT_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `DEPT_ABBR` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `DEPT_CODE` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `DEPT_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `UP_DEPT_ID` int(11) NULL DEFAULT NULL,
  `ORG_ID` int(11) NULL DEFAULT NULL,
  `LEADER_USER_ID` int(11) NULL DEFAULT NULL,
  `TREE_LEVEL` int(11) NULL DEFAULT NULL,
  `TREE_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORDER_NO` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_dept_info
-- ----------------------------
INSERT INTO `t_sys_dept_info` VALUES (1, '测试部门', '测试部门', 'test', '1', NULL, 1, 1, NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_dept_info` VALUES (2, '实施部', '实施部', 'SHISHI', '1', NULL, 1, 1, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_org_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_org_info`;
CREATE TABLE `t_sys_org_info`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ORG_NAME` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORG_ABBR` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORG_CODE` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORG_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `UP_ORG_ID` int(11) NULL DEFAULT NULL,
  `ORG_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `TREE_LEVEL` int(11) NULL DEFAULT NULL,
  `TREE_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORDER_NO` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_org_info
-- ----------------------------
INSERT INTO `t_sys_org_info` VALUES (1, '测试企业', 'testcorp', '', '', 0, '1', 1, '0001', '1', '1212');
INSERT INTO `t_sys_org_info` VALUES (2, 'test', 'test', '11', '1', 1, '1', 2, '00010001', '1', '111111');
INSERT INTO `t_sys_org_info` VALUES (3, '1112', '1231sdad', '1', '1', 1, '1', 2, '00010002', '123', '123123');
INSERT INTO `t_sys_org_info` VALUES (4, '测试企业2', 'testcorp2', '112', NULL, 0, '1', 1, '0002', '2', NULL);
INSERT INTO `t_sys_org_info` VALUES (5, 'test2222', NULL, '333', '1', 1, '1', 2, '00010003', NULL, NULL);
INSERT INTO `t_sys_org_info` VALUES (6, 'test67', NULL, '23231', '2', 4, NULL, 2, '0002001', NULL, NULL);
INSERT INTO `t_sys_org_info` VALUES (11, 'teess', NULL, '21231', '1', 0, '1', 1, '0003', '3', NULL);
INSERT INTO `t_sys_org_info` VALUES (12, 'rt766', NULL, '123123', '1', 0, '1', 1, '0004', NULL, NULL);
INSERT INTO `t_sys_org_info` VALUES (13, 'erer5', NULL, '213213', '1', 11, '1', 2, '00030001', NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_resource_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_info`;
CREATE TABLE `t_sys_resource_info`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `RES_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `RES_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `URL` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `POWER_ID` int(11) NULL DEFAULT NULL,
  `IS_LEAF` int(11) NULL DEFAULT 0,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '1',
  `RES_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `PERMISSION` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `RES_ID` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `PID` int(11) NULL DEFAULT NULL,
  `SEQ_NO` int(11) NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORG_ID` bigint(20) NULL DEFAULT 0,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1007 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_info
-- ----------------------------
INSERT INTO `t_sys_resource_info` VALUES (1, '系统菜单', '1', NULL, 0, 0, '1', '01', NULL, NULL, 0, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (2, '测试1', '1', 'sys', 0, 1, '1', '0101', NULL, NULL, 1, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (3, '测试2', '1', 'sys', 0, 1, '1', '0102', NULL, NULL, 1, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (4, '测试菜单1', '1', NULL, 0, 0, '1', '02', NULL, NULL, 0, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (5, '测试5', '1', 'sys', 0, 1, '1', '0201', NULL, NULL, 4, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (6, 'test6', '1', 'activiti/userprocess/main', 0, 1, '1', '0202', NULL, NULL, 4, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1000, '系统管理', '1', NULL, 0, 0, '1', '99', NULL, NULL, 0, NULL, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1001, '组织机构管理', '1', 'system/org/show', 0, 1, '1', '9901', NULL, NULL, 1000, 1, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1002, '职位权限管理', '1', 'system/responsiblity/show', 0, 1, '1', '9902', NULL, NULL, 1000, 2, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1003, '用户管理', '1', 'system/user/show', 0, 1, '1', '9903', NULL, NULL, 1000, 3, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1004, '系统代码管理', '1', 'system/project/show', 0, 1, '1', '9904', NULL, NULL, 1000, 4, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1005, '测试4', '1', 'sys1231', 0, 1, '1', '0103', NULL, NULL, 1, 3, NULL, 0);
INSERT INTO `t_sys_resource_info` VALUES (1006, '测试11', '1', 'sssss', NULL, 1, '1', '0104', NULL, NULL, 1, 4, NULL, 0);

-- ----------------------------
-- Table structure for t_sys_resource_info_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_info_copy`;
CREATE TABLE `t_sys_resource_info_copy`  (
  `ID` int(11) NOT NULL,
  `RES_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `RES_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ACTION_URL` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `POWER_ID` int(11) NULL DEFAULT NULL,
  `RES_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `PARENT_ID` int(11) NULL DEFAULT NULL,
  `ORDER_NO` int(11) NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_info_copy
-- ----------------------------
INSERT INTO `t_sys_resource_info_copy` VALUES (1000, '系统管理', '1', NULL, 0, 'A99', NULL, NULL, NULL);
INSERT INTO `t_sys_resource_info_copy` VALUES (1001, '组织机构管理', '1', 'system/sysOrg.action', 0, 'A9901', 1000, 1, NULL);
INSERT INTO `t_sys_resource_info_copy` VALUES (1002, '角色权限管理', '1', 'system/sysRole.action', 0, 'A9902', 1000, 2, NULL);
INSERT INTO `t_sys_resource_info_copy` VALUES (1003, '用户管理', '1', 'system/sysUser.action', 0, 'A9903', 1000, 3, NULL);
INSERT INTO `t_sys_resource_info_copy` VALUES (1004, '系统代码管理', '1', NULL, 0, 'A9904', 1000, 4, NULL);

-- ----------------------------
-- Table structure for t_sys_resource_resp_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_resp_r`;
CREATE TABLE `t_sys_resource_resp_r`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resp_id` bigint(20) NOT NULL,
  `res_id` bigint(20) NOT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_resp_r
-- ----------------------------
INSERT INTO `t_sys_resource_resp_r` VALUES (1, 1, 1001, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (2, 1, 1002, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (3, 1, 1003, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (4, 1, 1004, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (5, 2, 5, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (6, 2, 6, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (7, 3, 6, '1');
INSERT INTO `t_sys_resource_resp_r` VALUES (11, 1, 6, '1');

-- ----------------------------
-- Table structure for t_sys_resource_role_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_role_r`;
CREATE TABLE `t_sys_resource_role_r`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ROLE_ID` int(11) NOT NULL,
  `RES_ID` int(11) NOT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_role_r
-- ----------------------------
INSERT INTO `t_sys_resource_role_r` VALUES (1, 1, 1001, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (2, 1, 1002, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (3, 1, 1003, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (4, 1, 1004, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (5, 2, 5, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (7, 2, 6, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (8, 3, 6, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (9, 1, 1, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (10, 1, 1000, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (11, 1, 4, '1');
INSERT INTO `t_sys_resource_role_r` VALUES (12, 1, 6, '1');

-- ----------------------------
-- Table structure for t_sys_resource_role_r_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_role_r_copy`;
CREATE TABLE `t_sys_resource_role_r_copy`  (
  `ID` int(11) NOT NULL,
  `ROLE_ID` int(11) NOT NULL,
  `RES_ID` int(11) NOT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_role_r_copy
-- ----------------------------
INSERT INTO `t_sys_resource_role_r_copy` VALUES (1, 1, 1001, '0');
INSERT INTO `t_sys_resource_role_r_copy` VALUES (2, 1, 1002, '0');
INSERT INTO `t_sys_resource_role_r_copy` VALUES (3, 1, 1003, '0');
INSERT INTO `t_sys_resource_role_r_copy` VALUES (4, 1, 1004, '0');
INSERT INTO `t_sys_resource_role_r_copy` VALUES (5, 1, 1000, '0');

-- ----------------------------
-- Table structure for t_sys_resource_user_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_user_r`;
CREATE TABLE `t_sys_resource_user_r`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` int(11) NOT NULL,
  `RES_ID` int(11) NOT NULL,
  `ASSIGN_TYPE` int(11) NULL DEFAULT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_resource_user_r
-- ----------------------------
INSERT INTO `t_sys_resource_user_r` VALUES (13, 1, 1006, 1, '1');
INSERT INTO `t_sys_resource_user_r` VALUES (14, 1, 1004, 2, '1');

-- ----------------------------
-- Table structure for t_sys_resource_user_r_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_resource_user_r_copy`;
CREATE TABLE `t_sys_resource_user_r_copy`  (
  `ID` int(11) NOT NULL,
  `USER_ID` int(11) NOT NULL,
  `RES_ID` int(11) NOT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for t_sys_responsibility
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_responsibility`;
CREATE TABLE `t_sys_responsibility`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `create_user` bigint(20) NULL DEFAULT NULL,
  `update_user` bigint(20) NULL DEFAULT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_responsibility
-- ----------------------------
INSERT INTO `t_sys_responsibility` VALUES (1, '管理员', '2019-09-01 20:56:16', '2019-09-01 20:56:09', 1, 1, '1');
INSERT INTO `t_sys_responsibility` VALUES (2, '一般用户', '2019-09-01 20:56:41', '2019-09-01 20:56:36', 1, 1, '1');

-- ----------------------------
-- Table structure for t_sys_role_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_role_info`;
CREATE TABLE `t_sys_role_info`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ROLE_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_DESC` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_role_info
-- ----------------------------
INSERT INTO `t_sys_role_info` VALUES (1, '管理员', '1', '1', 'admin', NULL);
INSERT INTO `t_sys_role_info` VALUES (2, 'test', '2', '1', 'test1', NULL);
INSERT INTO `t_sys_role_info` VALUES (3, 'test1', '2', '0', '', '');
INSERT INTO `t_sys_role_info` VALUES (4, 'test3', '2', '1', NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_role_info_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_role_info_copy`;
CREATE TABLE `t_sys_role_info_copy`  (
  `ID` int(11) NOT NULL,
  `ROLE_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_CODE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ROLE_DESC` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_role_info_copy
-- ----------------------------
INSERT INTO `t_sys_role_info_copy` VALUES (1, '管理员', '0', '1', NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_info`;
CREATE TABLE `t_sys_user_info`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ACCOUNT` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_PASSWORD` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ACCOUNT_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORDER_NO` int(11) NULL DEFAULT NULL,
  `ORG_ID` int(11) NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `DEPT_ID` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_info
-- ----------------------------
INSERT INTO `t_sys_user_info` VALUES (1, 'admin', 'E10ADC3949BA59ABBE56E057F20F883E', '管理员', '1', '1', NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (2, 'test', 'E10ADC3949BA59ABBE56E057F20F883E', '测试帐号', '2', '1', NULL, NULL, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (4, 'tttttt', 'E10ADC3949BA59ABBE56E057F20F883E', 'test1111', '2', '1', NULL, 3, '', NULL);
INSERT INTO `t_sys_user_info` VALUES (6, 'wswss', 'E10ADC3949BA59ABBE56E057F20F883E', 'tttt', '2', '1', NULL, 2, '', NULL);
INSERT INTO `t_sys_user_info` VALUES (8, 'kermit', 'E10ADC3949BA59ABBE56E057F20F883E', '流程启动着', '3', '1', NULL, 1, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (22, 'reerer', NULL, 'teetse', '2', NULL, NULL, 3, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (25, 'test1', '96E79218965EB72C92A549DD5A330112', 'test', '2', '1', 1, NULL, '', NULL);
INSERT INTO `t_sys_user_info` VALUES (26, 'test11', NULL, 'test', '3', NULL, NULL, 1, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (27, 't1', 't1', 't1', '1', NULL, 1, 1, NULL, NULL);
INSERT INTO `t_sys_user_info` VALUES (28, 'test', NULL, 'test', '2', '1', 1, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_user_info_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_info_copy`;
CREATE TABLE `t_sys_user_info_copy`  (
  `ID` int(11) NOT NULL,
  `USER_ACCOUNT` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_PASSWORD` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ACCOUNT_TYPE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `USER_STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ORDER_NO` int(11) NULL DEFAULT NULL,
  `DEPT_ID` int(11) NULL DEFAULT NULL,
  `ORG_ID` int(11) NULL DEFAULT NULL,
  `REMARK` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_info_copy
-- ----------------------------
INSERT INTO `t_sys_user_info_copy` VALUES (1, 'admin', '12345678', '管理员', NULL, '1', NULL, 1, 1, NULL);
INSERT INTO `t_sys_user_info_copy` VALUES (2, 'test', '123', '测试用户', NULL, '1', NULL, 1, 1, NULL);
INSERT INTO `t_sys_user_info_copy` VALUES (3, 'ceshi', '123', 'ceshi', NULL, '1', NULL, 1, 1, NULL);
INSERT INTO `t_sys_user_info_copy` VALUES (4, 'luoming', '123', 'luoming', NULL, '1', NULL, 1, 1, NULL);
INSERT INTO `t_sys_user_info_copy` VALUES (5, NULL, NULL, 'testse111', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_sys_user_org_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_org_r`;
CREATE TABLE `t_sys_user_org_r`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `org_id` bigint(20) NOT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_org_r
-- ----------------------------
INSERT INTO `t_sys_user_org_r` VALUES (2, 2, 1, '1');
INSERT INTO `t_sys_user_org_r` VALUES (3, 2, 2, '1');

-- ----------------------------
-- Table structure for t_sys_user_resp_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_resp_r`;
CREATE TABLE `t_sys_user_resp_r`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resp_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_resp_r
-- ----------------------------
INSERT INTO `t_sys_user_resp_r` VALUES (1, 1, 1, '1');
INSERT INTO `t_sys_user_resp_r` VALUES (2, 1, 2, '1');

-- ----------------------------
-- Table structure for t_sys_user_role_r
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_role_r`;
CREATE TABLE `t_sys_user_role_r`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ROLE_ID` int(11) NOT NULL,
  `USER_ID` int(11) NOT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `OPER` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_role_r
-- ----------------------------
INSERT INTO `t_sys_user_role_r` VALUES (1, 1, 1, '0', NULL);
INSERT INTO `t_sys_user_role_r` VALUES (2, 1, 8, '0', NULL);
INSERT INTO `t_sys_user_role_r` VALUES (3, 2, 2, '0', NULL);

-- ----------------------------
-- Table structure for t_sys_user_role_r_copy
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_role_r_copy`;
CREATE TABLE `t_sys_user_role_r_copy`  (
  `ID` int(11) NOT NULL,
  `ROLE_ID` int(11) NOT NULL,
  `USER_ID` int(11) NOT NULL,
  `STATUS` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `OPER` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of t_sys_user_role_r_copy
-- ----------------------------
INSERT INTO `t_sys_user_role_r_copy` VALUES (1, 1, 1, '0', NULL);

SET FOREIGN_KEY_CHECKS = 1;
