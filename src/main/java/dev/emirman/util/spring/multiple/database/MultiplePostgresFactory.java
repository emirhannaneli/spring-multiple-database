package dev.emirman.util.spring.multiple.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.emirman.util.spring.multiple.database.annotation.jpa.event.EnableMultipleJPAListener;
import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import dev.emirman.util.spring.multiple.database.filter.MultipleDBFilter;
import dev.emirman.util.spring.multiple.database.routing.DynamicRoutingDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.annotation.RequestScope;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
@Import(MultipleDBFilter.class)
public class MultiplePostgresFactory {
    private static Map<String, JdbcTemplate> templates;
    private static Map<String, DataSource> sources;
    private static Map<Object, Object> targetSources;
    private static Map<String, EntityManagerFactory> factories;
    private static Map<String, JpaTransactionManager> managers;
    private static HibernateJpaDialect dialect;
    private final ApplicationContext context;
    @Value("${spring.datasource.username:}")
    private String username;
    @Value("${spring.datasource.password:}")
    private String password;
    @Value("${spring.datasource.url:}")
    private String url;
    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String hibernateDdlAuto;
    @Value("${spring.jpa.database-platform:}")
    private String hibernateDialect;
    @Value("${spring.jpa.show-sql:false}")
    private String hibernateShowSql;
    @Value("${spring.datasource.driver-class-name:}")
    private String driver;

    public MultiplePostgresFactory(ApplicationContext context) {
        Logger logger = LoggerFactory.getLogger(MultiplePostgresFactory.class);
        templates = new HashMap<>();
        sources = new HashMap<>();
        targetSources = new HashMap<>();
        factories = new HashMap<>();
        managers = new HashMap<>();
        dialect = new HibernateJpaDialect();
        this.context = context;
        logger.info("MultiplePostgresFactory initialized");
    }

    private HikariDataSource hikariDataSource(DriverManagerDataSource source) {
        HikariDataSource hikari = new HikariDataSource();
        hikari.setJdbcUrl(source.getUrl());
        hikari.setUsername(source.getUsername());
        hikari.setPassword(source.getPassword());
        hikari.setDriverClassName(driver);
        hikari.setAutoCommit(true);
        return hikari;
    }

    public Map<String, JdbcTemplate> templates() {
        return templates;
    }

    public JdbcTemplate template() {
        String database = MultipleDBContextHolder.database();
        return templates.computeIfAbsent(database, this::createTemplate);
    }

    public JdbcTemplate template(String database) {
        return templates.computeIfAbsent(database, this::createTemplate);
    }

    public Map<String, DataSource> sources() {
        return sources;
    }

    public DataSource source() {
        String database = MultipleDBContextHolder.database();
        return sources.computeIfAbsent(database, this::createSource);
    }

    public DataSource source(String database) {
        return sources.computeIfAbsent(database, this::createSource);
    }

    public Map<String, EntityManagerFactory> factories() {
        return factories;
    }

    public EntityManagerFactory factory() {
        String database = MultipleDBContextHolder.database();
        return factories.computeIfAbsent(database, this::createFactory);
    }

    public EntityManagerFactory factory(String database) {
        return factories.computeIfAbsent(database, this::createFactory);
    }

    public Map<String, JpaTransactionManager> transactionManagers() {
        return managers;
    }

    public JpaTransactionManager transactionManager() {
        String database = MultipleDBContextHolder.database();
        return managers.computeIfAbsent(database, this::createManager);
    }

    public JpaTransactionManager transactionManager(String database) {
        return managers.computeIfAbsent(database, this::createManager);
    }

    public Map<Object, Object> targetSources() {
        return targetSources;
    }

    public Object targetSource() {
        String database = MultipleDBContextHolder.database();
        return targetSources.computeIfAbsent(database, this::createTargetSource);
    }

    public Object targetSource(String database) {
        return targetSources.computeIfAbsent(database, this::createTargetSource);
    }


    private PlatformTransactionManager createTransactionManager() {
        return transactionManager();
    }

    private PlatformTransactionManager createTransactionManager(String database) {
        return transactionManager(database);
    }

    private EntityManager createEntityManager() {
        return factory().createEntityManager();
    }

    private EntityManager createEntityManager(String database) {
        return factory(database).createEntityManager();
    }

    private JdbcTemplate createTemplate(String database) {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(source());
        return template;
    }

    private DataSource createSource(String database) {
        String defaultUrl = url.substring(0, url.lastIndexOf('/') + 1);
        DriverManagerDataSource source = new DriverManagerDataSource(defaultUrl, username, password);
        source.setDriverClassName(driver);
        source.setPassword(password);
        source.setUsername(username);
        try {
            Connection connection = source.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE \"" + database + "\"");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            // ignored
        }

        String url = this.url.substring(0, this.url.lastIndexOf('/') + 1) + database;
        source = new DriverManagerDataSource(url, username, password);
        source.setDriverClassName(driver);
        source.setUrl(url);
        source.setPassword(password);
        source.setUsername(username);
        return hikariDataSource(source);
    }

    private EntityManagerFactory createFactory(String database) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        Properties properties = properties();
        factory.setJpaProperties(properties);
        String[] packages = MultipleDBContextHolder.scanPackages();
        factory.setPackagesToScan(packages);
        factory.setDataSource(source(database));
        factory.setPersistenceUnitName(database);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.show_sql", hibernateShowSql);
        properties.setProperty("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        properties.setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return properties;
    }

    private EntityManagerFactory createFactory() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private JpaTransactionManager createManager(String database) {
        var manager = new JpaTransactionManager();
        manager.setDataSource(source());
        manager.setEntityManagerFactory(factory());
        manager.setJpaDialect(dialect);
        manager.setPersistenceUnitName(database);
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        jpaProperties.setProperty("hibernate.dialect", hibernateDialect);
        jpaProperties.setProperty("hibernate.show_sql", hibernateShowSql);
        manager.setJpaProperties(jpaProperties);
        manager.afterPropertiesSet();
        DataSource source = context.getBean("dataSource", DataSource.class);
        if (source instanceof DynamicRoutingDataSource dataSource)
            dataSource.addTargetDataSource(database);
        return manager;
    }

    private DataSource createTargetSource(Object database) {
        return source((String) database);
    }

    public String defaultDatabase() {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @RequestScope
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(MultiplePostgresFactory factory) {
        String database = MultipleDBContextHolder.database();
        String defaultDatabase = factory.defaultDatabase();
        if (database == null) return factory.transactionManager(defaultDatabase);
        return factory.transactionManager();
    }

    @Bean(name = "dataSource")
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public DataSource dynamicDataSource(MultiplePostgresFactory factory) {
        var source = new DynamicRoutingDataSource(factory);
        source.setTargetDataSources(factory.targetSources());
        boolean isDefault = MultipleDBContextHolder.database() == null;
        if (isDefault) {
            source.setDefaultTargetDataSource(factory.source(factory.defaultDatabase()));
            source.afterPropertiesSet();
            return source;
        }
        source.setDefaultTargetDataSource(factory.source());
        source.afterPropertiesSet();
        return source;
    }
}

