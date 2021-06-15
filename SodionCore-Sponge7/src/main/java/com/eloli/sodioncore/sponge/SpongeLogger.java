package com.eloli.sodioncore.sponge;

import com.eloli.sodioncore.logger.AbstractLogger;
import org.slf4j.Logger;

public class SpongeLogger implements AbstractLogger {
    private final Logger logger;

    public SpongeLogger(Logger logger) {
        this.logger = logger;
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