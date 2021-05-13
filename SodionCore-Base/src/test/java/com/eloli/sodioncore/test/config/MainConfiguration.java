package com.eloli.sodioncore.test.config;

import com.eloli.sodioncore.config.Configure;
import com.eloli.sodioncore.config.Lore;
import com.eloli.sodioncore.config.Migrate;
import com.google.gson.annotations.Expose;

public class MainConfiguration extends Configure {
    @Expose(serialize = true, deserialize = false)
    public Integer version = 2;

    @Lore("The default language should message use.")
    @Migrate("defaultLang")
    @Expose
    public String defaultLang = "en";
}
