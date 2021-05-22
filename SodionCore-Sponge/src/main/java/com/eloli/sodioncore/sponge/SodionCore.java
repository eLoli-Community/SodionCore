package com.eloli.sodioncore.sponge;

import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.logger.AbstractLogger;
import com.eloli.sodioncore.orm.AbstractSodionCore;
import com.eloli.sodioncore.orm.OrmService;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import com.eloli.sodioncore.sponge.config.Configuration;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = "sodioncore")
public class SodionCore implements AbstractSodionCore {
    private final Map<String, DependencyManager> dependencyManager = new HashMap<>();
    @Inject
    private Logger spongeLogger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    private BaseFileService baseFileService;
    private ConfigureService<Configuration> configureService;
    private DatabaseConfigure databaseConfigure;
    private AbstractLogger logger;
    private OrmService ormService;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        baseFileService = new BaseFileService(Sponge.getConfigManager().getPluginConfig(this).getConfigPath().toString());
        try {
            configureService = new ConfigureService<Configuration>(baseFileService, configDir + "/config.json")
                    .register(null, Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
            Sponge.getServer().shutdown();
        }
        logger = new SpongeLogger(spongeLogger);

        try {
            databaseConfigure = (DatabaseConfigure) Configuration.class
                    .getField(configureService.instance.database)
                    .get(configureService.instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            Sponge.getServer().shutdown();
        }

        ormService = new OrmService(getDependencyManager(this), new ArrayList<>(), databaseConfigure);
    }

    public DependencyManager getDependencyManager(Object plugin) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin announce = plugin.getClass().getAnnotation(Plugin.class);
            return getDependencyManager(announce.id(), announce.version().equals("") ? "dev" : announce.version());
        }
        return null;
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
