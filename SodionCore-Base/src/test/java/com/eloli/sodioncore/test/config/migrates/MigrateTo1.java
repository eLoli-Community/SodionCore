package com.eloli.sodioncore.test.config.migrates;

import com.eloli.sodioncore.config.Migrater;
import com.eloli.sodioncore.test.config.MainConfiguration;

import java.util.Locale;

public class MigrateTo1 extends Migrater<HistoryConfiguration0, MainConfiguration> {
    @Override
    public void migrate(HistoryConfiguration0 from, MainConfiguration to) {
        to.defaultLang = from.defaultLang.toUpperCase(Locale.ROOT);
    }
}
