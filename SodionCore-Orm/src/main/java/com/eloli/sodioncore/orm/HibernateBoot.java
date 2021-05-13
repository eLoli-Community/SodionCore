package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateBoot {
    public SessionFactory sessionFactory;
    public HibernateBoot(List<Class<? extends SodionEntity>> entities, DatabaseConfigure config){
        Configuration conf = new Configuration();
        for (Class<? extends SodionEntity> entity : entities) {
            conf.addAnnotatedClass(entity);
        }
        config.apply(conf);
        conf.setProperty("hibernate.hbm2ddl.auto", "update");

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(conf.getProperties()).build();
        sessionFactory = conf.buildSessionFactory(serviceRegistry);
    }
}
