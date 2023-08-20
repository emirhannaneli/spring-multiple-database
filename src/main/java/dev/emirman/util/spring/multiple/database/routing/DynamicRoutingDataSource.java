package dev.emirman.util.spring.multiple.database.routing;

import dev.emirman.util.spring.multiple.database.MultiplePostgresFactory;
import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    private final MultiplePostgresFactory factory;

    public DynamicRoutingDataSource(MultiplePostgresFactory factory) {
        this.factory = factory;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String database = MultipleDBContextHolder.database();
        boolean isNull = database == null;
        if (!isNull) System.out.println("determineCurrentLookupKey: " + database);
        return isNull ? factory.defaultDatabase() : database;
    }

    public void addTargetDataSource(String database) {
        setTargetDataSources(factory.targetSources());
        setDefaultTargetDataSource(factory.targetSource(database));
        afterPropertiesSet();
    }
}
