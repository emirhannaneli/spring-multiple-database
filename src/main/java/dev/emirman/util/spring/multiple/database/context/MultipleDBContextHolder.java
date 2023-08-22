package dev.emirman.util.spring.multiple.database.context;

import org.springframework.stereotype.Component;

@Component
public class MultipleDBContextHolder {
    private static String database;

    private static String[] scanPackages;

    public static void database(String databaseName) {
        database = databaseName;
    }

    public static String database() {
        return database;
    }

    public static void scanPackages(String[] scanPackages) {
        MultipleDBContextHolder.scanPackages = scanPackages;
    }

    public static String[] scanPackages() {
        return scanPackages;
    }

}
