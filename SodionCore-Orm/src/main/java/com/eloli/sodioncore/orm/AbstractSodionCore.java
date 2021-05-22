package com.eloli.sodioncore.orm;

import com.eloli.sodioncore.dependency.DependencyManager;

public interface AbstractSodionCore {
    DependencyManager getDependencyManager(String name, String version);

    OrmService getOrmService();
}
