package com.eloli.sodioncore.logger;

public interface AbstractLogger {
    void info(String info);

    void info(String info, Exception exception);

    void warn(String info);

    void warn(String info, Exception exception);
}
