package com.eloli.sodioncore.test.config;

import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.file.BaseFileService;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Config {
    @Test
    public void save() throws Exception {
        BaseFileService baseFileService = new BaseFileService("./run");
        File configFile = new File(baseFileService.getConfigPath("config.json"));
        MainConfiguration mainConfiguration = new MainConfiguration();

        if (configFile.exists()) {
            configFile.delete();
        }
        ConfigureService<MainConfiguration> configureService = new ConfigureService<>(
                baseFileService,
                "config.json",
                "com.eloli.sodioncore.test.config");
        assertEquals(configureService.instance.defaultLang, mainConfiguration.defaultLang);

    }
}
