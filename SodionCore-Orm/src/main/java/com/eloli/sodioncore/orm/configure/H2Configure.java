package com.eloli.sodioncore.orm.configure;

import com.eloli.sodioncore.config.Migrate;
import com.google.gson.annotations.Expose;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.H2Dialect;

public class H2Configure extends DatabaseConfigure {

    @Expose
    public String url = "./Users";

    @Expose
    public String username = "";

    @Expose
    public String password = "";

    @Override
    public String getDriverName() {
        return "com.h2database:h2:1.4.200:org.h2.Driver";
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:" + url);
        if (!"".equals(username)) {
            configuration.setProperty("hibernate.connection.username", username);
        }
        if (!"".equals(password)) {
            configuration.setProperty("hibernate.connection.password", password);
        }
        configuration.setProperty("hibernate.dialect", H2Dialect.class.getName());
    }
}
