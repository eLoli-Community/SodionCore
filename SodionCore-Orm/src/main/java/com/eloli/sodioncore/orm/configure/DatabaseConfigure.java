package com.eloli.sodioncore.orm.configure;

import com.eloli.sodioncore.config.Configure;
import org.hibernate.cfg.Configuration;

public abstract class DatabaseConfigure extends Configure {
    public abstract String getDriverName();

    public abstract void apply(Configuration configuration);
}
