package com.eloli.sodioncore.test.config;

import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.test.config.migrates.HistoryConfiguration0;
import com.eloli.sodioncore.test.config.migrates.MigrateTo1;
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
        ConfigureService<HistoryConfiguration0> historyConfigureService =
                new ConfigureService<HistoryConfiguration0>(baseFileService, "config.json")
                        .register(null, HistoryConfiguration0.class);
        historyConfigureService.init();

        assertEquals(historyConfigureService.instance.defaultLang, "en");

        ConfigureService<MainConfiguration> configureService =
                new ConfigureService<MainConfiguration>(baseFileService, "config.json")
                        .register(null, HistoryConfiguration0.class)
                        .register(MigrateTo1.class, MainConfiguration.class);
        configureService.init();
        assertEquals(configureService.instance.defaultLang, "EN");

    }
}
