package me.neon.libs.service;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * GeekCollectLimit
 * me.geek.collect.sql.impl
 *
 * @author 老廖
 * @since 2023/10/3 6:40
 */
public final class ServiceMysql implements IService {
    private HikariDataSource dataSource;
    private final SqlConfig mysqlData;

    private final Plugin plugin;

    public ServiceMysql(Plugin plugin, SqlConfig sqlConfig) {
        this.mysqlData = sqlConfig;
        this.plugin = plugin;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void startSql() {
        String url = "jdbc:mysql://"+mysqlData.host+":"+mysqlData.port+"/"+mysqlData.database+""+mysqlData.params;
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(mysqlData.username);
        dataSource.setPassword(mysqlData.password);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        } catch (RuntimeException | NoClassDefFoundError | ClassNotFoundException e) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        dataSource.setMaxLifetime(1800000);
        dataSource.setKeepaliveTime(0);
        dataSource.setConnectionTimeout(5000);
        dataSource.setPoolName(plugin.getName() + "-Mysql");
    }

    @Override
    public void stopSql() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
