package com.eloli.sodioncore.bukkit.config.migrates;

import com.eloli.sodioncore.config.Configure;
import com.eloli.sodioncore.config.Lore;
import com.eloli.sodioncore.orm.configure.H2Configure;
import com.google.gson.annotations.Expose;

public class HistoryConfiguration1 extends Configure {
    @Expose(serialize = true, deserialize = false)
    public Integer version = 1;

    @Lore("The default language should message use.")
    @Expose
    public String defaultLang = "en";

    @Lore("old h2 field")
    public H2Configure h2 = new H2Configure();
}
