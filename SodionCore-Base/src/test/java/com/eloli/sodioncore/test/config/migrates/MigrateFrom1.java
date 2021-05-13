package com.eloli.sodioncore.test.config.migrates;

import com.eloli.sodioncore.config.Migrater;
import com.eloli.sodioncore.test.config.MainConfiguration;

import java.util.Locale;

public class MigrateFrom1 extends Migrater<HistoryConfiguration1, MainConfiguration> {
    @Override
    public void migrate(HistoryConfiguration1 from, MainConfiguration to) {
        to.defaultLang = from.defaultLang.toUpperCase(Locale.ROOT);
    }
}
