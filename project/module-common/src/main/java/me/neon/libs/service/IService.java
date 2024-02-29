package me.neon.libs.service;

import me.neon.libs.taboolib.core.env.RuntimeDependencies;
import me.neon.libs.taboolib.core.env.RuntimeDependency;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 作者: 老廖
 * 时间: 2022/11/6
 **/
@RuntimeDependencies({
        @RuntimeDependency(
                value = "!org.slf4j:slf4j-api:2.0.8",
                test = "org.slf4j.LoggerFactory",
                relocate = {"!org.slf4j", "org.slf4j"},
                transitive = false
        ),
        @RuntimeDependency(
                value = "!com.zaxxer:HikariCP:4.0.3",
                test = "com.zaxxer.hikari.HikariDataSource",
                relocate = {
                        "!com.zaxxer.hikari", "com.zaxxer.hikari",
                        "!org.slf4j", "org.slf4j"}
                ,
                transitive = false
        )
})
public interface IService {

    Connection getConnection() throws SQLException;

    void startSql();

    void stopSql();
}
