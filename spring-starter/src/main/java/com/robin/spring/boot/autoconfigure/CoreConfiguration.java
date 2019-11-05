package com.robin.spring.boot.autoconfigure;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.SqlMapperDao;
import com.robin.core.base.service.SqlMapperService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.mapper.SqlMapperConfigure;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.SqlDialectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class CoreConfiguration {
    private CoreConfigurationProperties configurationProperties;
    public CoreConfiguration(CoreConfigurationProperties configurationProperties){
        this.configurationProperties=configurationProperties;
    }
    @Primary
    @Bean("dataSource")
    @ConfigurationProperties(prefix = "core.ds.main")
    @DependsOn("springContextHolder")
    public DataSource getDataSource(){
        return DataSourceBuilder.create().build();
    }
    @Bean(name="queryFactory")
    @Qualifier("queryFactory")
    @ConditionalOnProperty(value = "core.query.xml",havingValue = "true")
    public QueryFactory getQueryFactory(){
        QueryFactory factory=new QueryFactory();
        factory.setXmlConfigPath(configurationProperties.getQueryConfigPath());
        return factory;
    }
    @Bean(name = "queryMapper")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperConfigure getQueryMapper(){
        SqlMapperConfigure configure=new SqlMapperConfigure();
        configure.setXmlConfigPath(configurationProperties.getQueryMapperPath());
        return configure;
    }
    @Bean(name = "queryMapperDao")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperDao getSqlMapperDao(@Qualifier("dataSource") DataSource dataSource, @Qualifier("sqlGen") BaseSqlGen sqlGen,@Qualifier("queryMapper") SqlMapperConfigure sqlMapperConfigure){
        return new SqlMapperDao(sqlMapperConfigure,dataSource,sqlGen);
    }
    @Bean(name = "queryMapperService")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperService getSqlMapperService(@Qualifier("queryMapperDao") SqlMapperDao sqlMapperDao){
        return new SqlMapperService(sqlMapperDao);
    }

    @Bean(name="lobHandler")
    @Qualifier("lobHandler")
    public LobHandler getLobHandler(){
        return new DefaultLobHandler();
    }

    @Bean(name = "sqlGen")
    @Qualifier("sqlGen")
    public BaseSqlGen getSqlGen(){
        try {
            return SqlDialectFactory.getSqlGeneratorByDialect(configurationProperties.getDbDialect());
        }catch (Exception ex){
            log.error("{}",ex);
        }
        return null;
    }

    @Bean(name="springContextHolder")
    @Lazy(false)
    public SpringContextHolder getHolder(){
        return new SpringContextHolder();
    }
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
