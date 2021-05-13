package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.hibernate.SessionFactory;

import java.util.List;

public class OrmService implements AutoCloseable {
    public SessionFactory sessionFactory;

    public OrmService(DependencyManager dependencyManager, List<Class<? extends SodionEntity>> entities, DatabaseConfigure config) {
        dependencyManager.checkDependencyMaven(config.getDriverName());

        dependencyManager.checkDependencyMaven("org.hibernate.orm:hibernate-core:6.0.0.Alpha7:org.hibernate.Hibernate");

        HibernateBoot boot = new HibernateBoot(entities,config);
        sessionFactory = boot.sessionFactory;
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }
}
