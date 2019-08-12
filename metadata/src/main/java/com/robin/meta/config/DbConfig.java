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
package com.robin.meta.config;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.MysqlSqlGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;

@Configuration
public class DbConfig {
    private Logger logger= LoggerFactory.getLogger(getClass());
    @Value("${core.url}")
    private String coreurl;
    @Value("${core.driver-class-name}")
    private String coredriverClassName;
    @Value("${core.username}")
    private String coreuserName;
    @Value("${core.password}")
    private String corepassword;
    @Value("${core.type}")
    private String coretype;
    @Value("${project.queryConfigPath}")
    private String queryConfigPath;


    @Bean(name = "dataSource")
    @Qualifier("dataSource")
    @Primary
    @DependsOn("springContextHolder")
    public DataSource dataSource(){
        try {
            return DataSourceBuilder.create().type((Class<? extends DataSource>) Class.forName(coretype)).url(coreurl).driverClassName(coredriverClassName).username(coreuserName).password(corepassword).build();
        }catch (Exception ex){
            logger.error("",ex);
        }
        return null;
    }
    @Bean(name="queryFactory")
    @Qualifier("queryFactory")
    public QueryFactory getQueryFactory(){
        QueryFactory factory=new QueryFactory();
        factory.setXmlConfigPath(queryConfigPath);
        return factory;
    }
    @Bean(name="lobHandler")
    @Qualifier("lobHandler")
    public LobHandler getLobHandler(){
        return new DefaultLobHandler();
    }

    @Bean(name = "sqlGen")
    @Qualifier("sqlGen")
    public BaseSqlGen getSqlGen(){
        return new MysqlSqlGen();
    }

    @Bean(name="springContextHolder")
    @Lazy(false)
    public SpringContextHolder getHolder(){
        return new SpringContextHolder();
    }
    /**
     * DependsOn is required,Otherwise springContextHolder may not initialize
     * @return
     */
    @Bean(name="jdbcDao")
    public JdbcDao getJdbcDao(@Qualifier("dataSource") DataSource dataSource, @Qualifier("sqlGen") BaseSqlGen sqlGen, @Qualifier("queryFactory") QueryFactory factory, @Qualifier("lobHandler") LobHandler lobhandler){
        JdbcDao dao=new JdbcDao();
        dao.setDataSource(dataSource);
        dao.setLobHandler(lobhandler);
        dao.setQueryFactory(factory);
        dao.setSqlGen(sqlGen);
        return dao;
    }




}
