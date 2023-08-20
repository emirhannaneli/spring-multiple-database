package dev.emirman.util.spring.multiple.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import dev.emirman.util.spring.multiple.database.filter.MultipleDBFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Import({MultipleDBFilter.class})
public class MultipleMongoFactory {
    private static Map<String, MongoTemplate> templates;

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;
    @Value("${spring.data.mongodb.port:27017}")
    private int port;
    Logger logger = LoggerFactory.getLogger(MultipleMongoFactory.class);
    @Value("${spring.data.mongodb.authentication-database:}")
    private String authDatabase;
    @Value("${spring.data.mongodb.username:}")
    private String username;
    @Value("${spring.data.mongodb.password:}")
    private String password;
    @Value("${spring.data.mongodb.database:}")
    private String database;

    public MultipleMongoFactory() {
        templates = new HashMap<>();
        logger.info("MultipleMongoFactory initialized");
    }

    public Map<String, MongoTemplate> templates() {
        return templates;
    }

    public MongoTemplate template() {
        String database = MultipleDBContextHolder.database();
        if (database == null || database.isEmpty()) database = this.database;
        return templates.computeIfAbsent(database, this::createTemplate);
    }

    public String defaultDatabase() {
        return database;
    }

    public MongoTemplate template(String database) {
        return templates.computeIfAbsent(database, this::createTemplate);
    }

    private MongoTemplate createTemplate(String database) {
        String uri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s", username, password, host, port, database, authDatabase);
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applicationName("MultipleMongo")
                .build();
        MongoClient client = MongoClients.create(settings);
        return new MongoTemplate(client, database);
    }

    @Bean
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MongoTemplate mongoTemplate() {
        return template();
    }
}
