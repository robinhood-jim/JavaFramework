package com.robin.basis.config;

import com.robin.basis.service.system.LoginService;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.MysqlSqlGen;
import com.robin.core.web.service.ILoginService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;

@Configuration
public class DbConfig {
    private Logger logger= LoggerFactory.getLogger(getClass());

    @Value("${project.queryConfigPath}")
    private String queryConfigPath;


    @Bean(name = "dataSource")
    @Qualifier("dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties){
        return DataSourceBuilder.create(dataSourceProperties.getClassLoader()).type(HikariDataSource.class)
                .driverClassName(dataSourceProperties.determineDriverClassName())
                .url(dataSourceProperties.determineUrl())
                .username(dataSourceProperties.determineUsername())
                .password(dataSourceProperties.determinePassword())
                .build();
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
        return MysqlSqlGen.getInstance();
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
    //@Bean
    public MessageSource getMessageSource(){
        ResourceBundleMessageSource messageSource=new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
    @Bean
    public ILoginService getLoginService(){
        return new LoginService();
    }

    @Bean
    @Lazy(value = false)
    public SpringContextHolder getHolder(){
        return new SpringContextHolder();
    }





}
