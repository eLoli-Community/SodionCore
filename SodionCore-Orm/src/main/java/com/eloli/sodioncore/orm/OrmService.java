package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class OrmService implements AutoCloseable {
    private final List<Class<? extends SodionEntity>> entities;
    private final DatabaseConfigure config;
    public SessionFactory sessionFactory;

    public OrmService(List<Class<? extends SodionEntity>> entities, DatabaseConfigure config) {
        this.entities = new ArrayList<>(entities);
        this.config = config;

        sessionFactory = new HibernateBoot(entities, config).sessionFactory;
    }

    public void addEntities(List<Class<? extends SodionEntity>> entities) {
        this.entities.addAll(entities);
        sessionFactory.close();
        sessionFactory = new HibernateBoot(entities, config).sessionFactory;
    }

    @Override
    public void close() {
        sessionFactory.close();
    }
}
