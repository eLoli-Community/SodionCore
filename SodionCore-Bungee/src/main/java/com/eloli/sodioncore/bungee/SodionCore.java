package com.eloli.sodioncore.bungee;

import com.eloli.sodioncore.bungee.config.Configuration;
import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.logger.AbstractLogger;
import com.eloli.sodioncore.orm.OrmService;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SodionCore extends Plugin {
    private static final Map<String, DependencyManager> dependencyManager = new HashMap<>();
    public static OrmService ormService;
    private static BaseFileService baseFileService;
    private static ConfigureService<Configuration> configureService;
    private static DatabaseConfigure databaseConfigure;
    private static AbstractLogger logger;

    @Override
    public void onEnable() {
        baseFileService = new BaseFileService(getDataFolder().toString());
        try {
            configureService = new ConfigureService<Configuration>(baseFileService, "config.json")
                    .register(null, Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
            getProxy().stop();
        }
        logger = new BungeeLogger(this);

        try {
            databaseConfigure = (DatabaseConfigure) Configuration.class
                    .getField(configureService.instance.database)
                    .get(configureService.instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            getProxy().stop();
        }

        ormService = new OrmService(getDependencyManager(this), new ArrayList<>(), databaseConfigure);


    }

    public DependencyManager getDependencyManager(Plugin plugin) {
        DependencyManager result = dependencyManager.get(plugin.getDescription().getName());
        if (result == null) {
            result = new DependencyManager(
                    baseFileService,
                    logger,
                    new HashMap<>(),
                    plugin.getDescription().getName() + "-" + plugin.getDescription().getVersion(),
                    configureService.instance.mavenRepository);
            dependencyManager.put(plugin.getDescription().getName(), result);
        }
        return result;
    }
}
