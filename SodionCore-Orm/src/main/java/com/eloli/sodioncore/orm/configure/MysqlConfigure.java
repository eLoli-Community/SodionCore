package com.eloli.sodioncore.orm.configure;

import com.eloli.sodioncore.config.Migrate;
import com.google.gson.annotations.Expose;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQLDialect;

public class MysqlConfigure extends DatabaseConfigure {
    @Migrate("mysql.host")
    @Expose
    public String host = "127.0.0.1:3306";
    @Migrate("mysql.databaseName")
    @Expose
    public String databaseName = "sc";
    @Migrate("mysql.username")
    @Expose
    public String username = "root";
    @Migrate("mysql.password")
    @Expose
    public String password = "";

    @Override
    public String getDriverName() {
        return "mysql:mysql-connector-java:8.0.24:com.mysql.jdbc.Driver";
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url",
                "jdbc:mysql://" + host + "/" + databaseName + "?"
                        + "useUnicode=true&"
                        + "connectionCollation=utf8_general_ci&"
                        + "characterEncoding=UTF-8");
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", MySQLDialect.class.getName());
    }
}
