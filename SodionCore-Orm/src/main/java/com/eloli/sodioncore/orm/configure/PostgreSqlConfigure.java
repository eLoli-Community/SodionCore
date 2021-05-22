package com.eloli.sodioncore.orm.configure;

import com.google.gson.annotations.Expose;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQLDialect;

public class PostgreSqlConfigure extends DatabaseConfigure {

    @Expose
    public String host = "127.0.0.1:5432";

    @Expose
    public String databaseName = "sc";

    @Expose
    public String username = "postgres";

    @Expose
    public String password = "";

    @Override
    public String getDriverName() {
        return "org.postgresql:postgresql:42.2.20:org.postgresql.Driver";
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://" + host + "/" + databaseName);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", PostgreSQLDialect.class.getName());
    }
}
