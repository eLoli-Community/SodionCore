package com.eloli.sodioncore.bukkit.logger;

import com.eloli.sodioncore.logger.AbstractLogger;

import java.util.logging.Logger;

public class SpigotLogger implements AbstractLogger {
    private final Logger logger;

    public SpigotLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String info) {
        logger.info(info);
    }

    @Override
    public void info(String info, Exception exception) {
        logger.info(info);
        exception.printStackTrace();
    }

    @Override
    public void warn(String info) {
        logger.warning(info);
    }

    @Override
    public void warn(String info, Exception exception) {
        logger.warning(info);
        exception.printStackTrace();
    }
}
