package dev.emirman.util.spring.multiple.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MultipleMongoFactory {
    private final static ThreadLocal<Map<String, MongoTemplate>> templates = new ThreadLocal<>() {{
        set(new HashMap<>());
    }};

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;
    @Value("${spring.data.mongodb.port:27017}")
    private int port;
    @Value("${spring.data.mongodb.authentication-database:}")
    private String authDatabase;
    @Value("${spring.data.mongodb.username:}")
    private String username;
    @Value("${spring.data.mongodb.password:}")
    private String password;

    public Map<String, MongoTemplate> templates() {
        return templates.get();
    }

    public MongoTemplate template() {
        String database = MultipleDBContextHolder.database();
        return templates.get().computeIfAbsent(database, this::createTemplate);
    }

    public MongoTemplate template(String database) {
        return templates.get().computeIfAbsent(database, this::createTemplate);
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
}
