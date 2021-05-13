package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.List;

public class OrmService implements AutoCloseable {
    public SessionFactory sessionFactory;

    public OrmService(DependencyManager dependencyManager, List<Class<? extends SodionEntity>> entities, DatabaseConfigure config) throws Exception {
        dependencyManager.checkDependencyMaven(config.getDriverName());

        Configuration conf = new Configuration();
        for (Class<? extends SodionEntity> entity : entities) {
            conf.addAnnotatedClass(entity);
        }
        config.apply(conf);
        conf.setProperty("hibernate.hbm2ddl.auto", "update");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(conf.getProperties()).build();
        sessionFactory = conf.buildSessionFactory(serviceRegistry);
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }
}
