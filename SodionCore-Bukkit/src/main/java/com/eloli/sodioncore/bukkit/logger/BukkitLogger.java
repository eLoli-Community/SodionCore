package com.eloli.sodioncore.bukkit.logger;

import com.eloli.sodioncore.logger.AbstractLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class BukkitLogger implements AbstractLogger {
    private AbstractLogger logger;

    public BukkitLogger(JavaPlugin plugin) {
        try {
            logger = new PaperLogger((Logger) Plugin.class.getMethod("getSLF4JLogger").invoke(plugin));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignore) {
            logger = new SpigotLogger(plugin.getLogger());
        }
    }

    @Override
    public void info(String info) {
        logger.info(info);
    }

    @Override
    public void info(String info, Exception exception) {
        logger.info(info, exception);
    }

    @Override
    public void warn(String info) {
        logger.warn(info);
    }

    @Override
    public void warn(String info, Exception exception) {
        logger.warn(info, exception);
    }
}
