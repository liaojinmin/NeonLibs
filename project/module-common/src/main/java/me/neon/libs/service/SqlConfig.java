package me.neon.libs.service;

import java.io.File;

public final class SqlConfig {

    public final boolean isMysql;
    public final File dataFolder;
    public final String host;
    public final int port;

    public final String database;
    public final String username;
    public final String password;
    public final String params;

    public SqlConfig(boolean mysql, File dataFolder, String a, int b, String c, String d, String e, String f) {
        this.isMysql = mysql;
        this.dataFolder = dataFolder;
        this.host = a;
        this.port = b;
        this.database = c;
        this.username = d;
        this.password = e;
        this.params = f;
    }

}
