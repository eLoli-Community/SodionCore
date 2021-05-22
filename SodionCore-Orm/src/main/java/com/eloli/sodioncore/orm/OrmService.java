package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class OrmService implements AutoCloseable {
    private final List<Class<? extends SodionEntity>> entities;
    private final DatabaseConfigure config;
    public SessionFactory sessionFactory;

    public OrmService(DependencyManager dependencyManager, List<Class<? extends SodionEntity>> entities, DatabaseConfigure config) {
        dependencyManager.checkDependencyMaven(config.getDriverName());

        dependencyManager.checkDependencyMaven("org.hibernate.orm:hibernate-core:6.0.0.Alpha7:org.hibernate.Hibernate");

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
