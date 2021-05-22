package com.eloli.sodioncore.orm.configure;

import com.google.gson.annotations.Expose;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.SQLServerDialect;

public class SqliteConfigure extends DatabaseConfigure {
    @Expose
    public String url = "./Users";

    @Override
    public String getDriverName() {
        return "org.xerial:sqlite-jdbc:3.34.0:org.sqlite.JDBC";
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + url);
        configuration.setProperty("hibernate.dialect", SQLServerDialect.class.getName());
    }
}
