package com.eloli.sodioncore.test.config.migrates;

import com.eloli.sodioncore.config.Configure;
import com.eloli.sodioncore.config.Lore;
import com.google.gson.annotations.Expose;

public class HistoryConfiguration0 extends Configure {
    @Expose(serialize = true, deserialize = false)
    public Integer version = 0;

    @Lore("The default language should message use.")
    @Expose
    public String defaultLang = "en";
}
