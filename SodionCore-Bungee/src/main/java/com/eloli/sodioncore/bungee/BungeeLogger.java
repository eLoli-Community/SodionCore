package com.eloli.sodioncore.bungee;


import com.eloli.sodioncore.logger.AbstractLogger;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class BungeeLogger implements AbstractLogger {
    private final Logger logger;
    public BungeeLogger(Plugin plugin){
        logger = plugin.getLogger();
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
