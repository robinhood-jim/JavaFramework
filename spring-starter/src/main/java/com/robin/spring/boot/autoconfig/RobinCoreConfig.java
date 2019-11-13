package com.robin.spring.boot.autoconfig;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.SqlMapperDao;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.service.SqlMapperService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.mapper.SqlMapperConfigure;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.SqlDialectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;

@EnableConfigurationProperties({CoreConfigurationProperties.class,DataSourceConfig.class})
@Configuration
public class RobinCoreConfig implements TransactionManagementConfigurer, BeanFactoryAware {
    private CoreConfigurationProperties configurationProperties;
    private Logger log= LoggerFactory.getLogger(getClass());
    private DataSourceConfig dataSourceConfig;
    @Resource(name = "txManager")
    private PlatformTransactionManager txManager;
    private BeanFactory beanFactory;
    public RobinCoreConfig(CoreConfigurationProperties configurationProperties,DataSourceConfig dataSourceConfig){
        this.configurationProperties=configurationProperties;
        this.dataSourceConfig=dataSourceConfig;
    }
    @Primary
    @Bean("dataSource")
    @DependsOn("springContextHolder")
    public DataSource getDataSource(){
        try {
            DataSourceConfig.DataSourceCfg config=dataSourceConfig.getDatasources().get("main");
            if(config!=null)
                return DataSourceBuilder.create().type((Class<? extends DataSource>) Class.forName(config.getType())).url(config.getJdbcUrl()).driverClassName(config.getDriverClassName()).username(config.getUsername()).password(config.getPassword()).build();
            else{
                throw new ConfigurationIncorrectException("datasource main is missing");
            }
        }catch (Exception ex){

        }
        return null;
    }
    @Bean(name="queryFactory")
    @Qualifier("queryFactory")
    @ConditionalOnProperty(value = "core.query.xmlConfig",havingValue = "true")
    public QueryFactory getQueryFactory(){
        QueryFactory factory=new QueryFactory();
        factory.setXmlConfigPath(configurationProperties.getQueryConfigPath());
        return factory;
    }
    @Primary
    @Bean(name = "queryMapper")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperConfigure getQueryMapper(){
        SqlMapperConfigure configure=new SqlMapperConfigure();
        configure.setXmlConfigPath(configurationProperties.getQueryMapperPath());
        return configure;
    }
    @Primary
    @Bean(name = "queryMapperDao")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperDao getSqlMapperDao(@Qualifier("dataSource") DataSource dataSource, @Qualifier("sqlGen") BaseSqlGen sqlGen,@Qualifier("queryMapper") SqlMapperConfigure sqlMapperConfigure){
        return new SqlMapperDao(sqlMapperConfigure,dataSource,sqlGen);
    }
    @Primary
    @Bean(name = "queryMapperService")
    @ConditionalOnProperty(value = "core.query.mapper",havingValue = "true")
    public SqlMapperService getSqlMapperService(@Qualifier("queryMapperDao") SqlMapperDao sqlMapperDao){
        return new SqlMapperService(sqlMapperDao);
    }
    @Primary
    @Bean(name="lobHandler")
    @Qualifier("lobHandler")
    public LobHandler getLobHandler(){
        return new DefaultLobHandler();
    }

    @Primary
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

    @Primary
    @Bean(name="springContextHolder")
    @Lazy(false)
    public SpringContextHolder getHolder(){
        return new SpringContextHolder();
    }

    @Primary
    @Bean(name="jdbcDao")
    public JdbcDao getJdbcDao(@Qualifier("dataSource") DataSource dataSource, @Qualifier("sqlGen") BaseSqlGen sqlGen, @Qualifier("queryFactory") QueryFactory factory, @Qualifier("lobHandler") LobHandler lobhandler){
        JdbcDao dao=new JdbcDao();
        dao.setDataSource(dataSource);
        dao.setLobHandler(lobhandler);
        dao.setQueryFactory(factory);
        dao.setSqlGen(sqlGen);
        return dao;
    }

    public CoreConfigurationProperties getConfigurationProperties() {
        return configurationProperties;
    }

    @Bean
    public PlatformTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return txManager;
    }

    @PostConstruct
    public void configure(){
        Map<String,DataSourceConfig.DataSourceCfg> configMap=dataSourceConfig.getDatasources();
        Iterator<Map.Entry<String,DataSourceConfig.DataSourceCfg>> iterator=configMap.entrySet().iterator();
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        while(iterator.hasNext()){
            Map.Entry<String,DataSourceConfig.DataSourceCfg> entry=iterator.next();
            if(!"main".equalsIgnoreCase(entry.getKey())){
                DataSource dataSource=createDataSource(entry.getValue());
                configurableBeanFactory.registerSingleton(entry.getKey(),dataSource);
                configurableBeanFactory.registerSingleton(entry.getKey()+"Dao",createJdbcDao(dataSource));
            }
        }

    }
    private DataSource createDataSource(DataSourceConfig.DataSourceCfg config){
        try {
            if(config!=null)
                return DataSourceBuilder.create().type((Class<? extends DataSource>) Class.forName(config.getType())).url(config.getJdbcUrl()).driverClassName(config.getDriverClassName()).username(config.getUsername()).password(config.getPassword()).build();
            else{
                throw new ConfigurationIncorrectException("datasource construct error");
            }
        }catch (Exception ex){

        }
        return null;
    }
    private JdbcDao createJdbcDao(DataSource dataSource){
        return new JdbcDao(dataSource,SpringContextHolder.getBean(LobHandler.class),SpringContextHolder.getBean(QueryFactory.class),SpringContextHolder.getBean(BaseSqlGen.class));
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=beanFactory;
    }
}
