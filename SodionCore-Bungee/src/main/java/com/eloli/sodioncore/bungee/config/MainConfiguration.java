package com.eloli.sodioncore.bungee.config;

import com.eloli.sodioncore.config.Configure;
import com.eloli.sodioncore.config.Lore;
import com.eloli.sodioncore.config.Migrate;
import com.eloli.sodioncore.orm.configure.H2Configure;
import com.eloli.sodioncore.orm.configure.MysqlConfigure;
import com.eloli.sodioncore.orm.configure.PostgreSqlConfigure;
import com.eloli.sodioncore.orm.configure.SqliteConfigure;
import com.google.gson.annotations.Expose;

public class MainConfiguration extends Configure {
    @Expose(serialize = true, deserialize = false)
    public Integer version = 2;

    @Lore("Maven Repo Url")
    @Lore("If you are in China, you should use this")
    @Lore("https://maven.aliyun.com/repository/central/")
    @Migrate("mavenRepository")
    @Expose
    public String mavenRepository = "https://repo1.maven.org/maven2/";

    @Lore("The default language should message use.")
    @Expose
    public String database = "h2";

    @Migrate("h2")
    @Expose
    public H2Configure h2 = new H2Configure();

    @Expose
    public MysqlConfigure mysql = new MysqlConfigure();

    @Expose
    public SqliteConfigure sqliteConfigure = new SqliteConfigure();

    @Expose
    public PostgreSqlConfigure postgreSql = new PostgreSqlConfigure();
}