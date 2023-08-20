package dev.emirman.util.spring.multiple.database.context;

import org.springframework.stereotype.Component;

@Component
public class MultipleDBContextHolder {
    private static String database;

    public static void database(String databaseName) {
        database = databaseName;
    }

    public static String database() {
        return database;
    }


}
