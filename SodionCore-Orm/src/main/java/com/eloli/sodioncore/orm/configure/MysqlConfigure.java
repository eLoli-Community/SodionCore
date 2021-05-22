package com.eloli.sodioncore.orm.configure;

import com.google.gson.annotations.Expose;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQLDialect;

public class MysqlConfigure extends DatabaseConfigure {

    @Expose
    public String host = "127.0.0.1:3306";

    @Expose
    public String databaseName = "sc";

    @Expose
    public String username = "root";

    @Expose
    public String password = "";

    @Override
    public String getDriverName() {
        return "mysql:mysql-connector-java:8.0.24:com.mysql.jdbc.Driver";
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + "/" + databaseName);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", MySQLDialect.class.getName());
    }
}
