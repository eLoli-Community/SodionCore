package com.eloli.sodioncore.bukkit;

import com.eloli.sodioncore.bukkit.config.Configuration;
import com.eloli.sodioncore.bukkit.logger.BukkitLogger;
import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.logger.AbstractLogger;
import com.eloli.sodioncore.orm.AbstractSodionCore;
import com.eloli.sodioncore.orm.OrmService;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SodionCore extends JavaPlugin implements AbstractSodionCore {
    private static final Map<String, DependencyManager> dependencyManager = new HashMap<>();
    private static OrmService ormService;
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
            configureService.init();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().shutdown();
        }
        logger = new BukkitLogger(this);

        try {
            databaseConfigure = (DatabaseConfigure) Configuration.class
                    .getField(configureService.instance.database)
                    .get(configureService.instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            getServer().shutdown();
        }

        ormService = new OrmService(new ArrayList<>(), databaseConfigure);


    }

    public DependencyManager getDependencyManager(Plugin plugin) {
        return getDependencyManager(plugin.getName(), plugin.getDescription().getVersion());
    }

    @Override
    public DependencyManager getDependencyManager(String name, String version) {
        DependencyManager result = dependencyManager.get(name);
        if (result == null) {
            result = new DependencyManager(
                    baseFileService,
                    logger,
                    new HashMap<>(),
                    name + "-" + version,
                    configureService.instance.mavenRepository);
            dependencyManager.put(name, result);
        }
        return result;
    }

    @Override
    public OrmService getOrmService() {
        return ormService;
    }
}
