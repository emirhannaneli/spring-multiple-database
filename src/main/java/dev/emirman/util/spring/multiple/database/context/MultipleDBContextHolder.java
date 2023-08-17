package dev.emirman.util.spring.multiple.database.context;

import org.springframework.stereotype.Component;

@Component
public record MultipleDBContextHolder() {
    private final static ThreadLocal<String> database = new ThreadLocal<>();

    public static void database(String databaseName) {
        database.set(databaseName);
    }

    public static String database() {
        return database.get();
    }


}
