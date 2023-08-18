package dev.emirman.util.spring.multiple.database;

import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class MultiplePostgresFactory {
    private final LocalContainerEntityManagerFactoryBean factoryBean;
    private static ThreadLocal<Map<String, JdbcTemplate>> templates;
    private static ThreadLocal<Map<String, DataSource>> sources;
    private static ThreadLocal<Map<String, EntityManagerFactory>> factories;

    @Value("${spring.data.postgres.host:localhost}")
    private String host;
    @Value("${spring.data.postgres.port:5432}")
    private int port;
    @Value("${spring.data.postgres.database:}")
    private String database;
    @Value("${spring.data.postgres.username:}")
    private String username;
    @Value("${spring.data.postgres.password:}")
    private String password;

    public MultiplePostgresFactory(LocalContainerEntityManagerFactoryBean factoryBean) {
        templates = ThreadLocal.withInitial(HashMap::new);
        sources = ThreadLocal.withInitial(HashMap::new);
        factories = ThreadLocal.withInitial(HashMap::new);
        this.factoryBean = factoryBean;
    }

    public Map<String, JdbcTemplate> templates() {
        return templates.get();
    }

    public JdbcTemplate template() {
        String database = MultipleDBContextHolder.database();
        return templates.get().computeIfAbsent(database, this::createTemplate);
    }

    public JdbcTemplate template(String database) {
        return templates.get().computeIfAbsent(database, this::createTemplate);
    }

    public Map<String, DataSource> sources() {
        return sources.get();
    }

    public DataSource source() {
        String database = MultipleDBContextHolder.database();
        return sources.get().computeIfAbsent(database, this::createSource);
    }

    public DataSource source(String database) {
        return sources.get().computeIfAbsent(database, this::createSource);
    }

    public Map<String, EntityManagerFactory> factories() {
        return factories.get();
    }

    public EntityManagerFactory factory() {
        String database = MultipleDBContextHolder.database();
        return factories.get().computeIfAbsent(database, this::createFactory);
    }

    public EntityManagerFactory factory(String database) {
        return factories.get().computeIfAbsent(database, this::createFactory);
    }

    public EntityManager entityManager() {
        return factory().createEntityManager();
    }

    public EntityManager entityManager(String database) {
        return factory(database).createEntityManager();
    }

    private JdbcTemplate createTemplate(String database) {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(source());
        return template;
    }

    private DataSource createSource(String database) {
        String defaultUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, this.database);
        DriverManagerDataSource source = new DriverManagerDataSource(defaultUrl, username, password);
        source.setDriverClassName("org.postgresql.Driver");
        source.setPassword(password);
        source.setUsername(username);
        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + database);
        } catch (SQLException e) {
            // ignored
        }

        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        source = new DriverManagerDataSource(url, username, password);
        source.setDriverClassName("org.postgresql.Driver");
        source.setPassword(password);
        source.setUsername(username);

        return source;
    }

    private EntityManagerFactory createFactory(String database) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        factory.setJpaProperties(jpaProperties);
        factory.setPackagesToScan("dev.emirman"); // TODO: 2021-10-13 change this
        factory.setDataSource(source(database));
        factory.setPersistenceUnitName(database);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private EntityManagerFactory createFactory() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
