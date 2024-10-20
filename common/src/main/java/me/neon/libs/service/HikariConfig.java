package me.neon.libs.service;

/**
 * NeonLibs
 * me.neon.libs.service
 *
 * @author 老廖
 * @since 2024/4/14 16:26
 */
public class HikariConfig {

    public final int maximumPoolSize;

    public final int minimumIdle;

    public final int maximumLifetime;

    public final int keepaliveTime;

    public final int connectionTimeout;

    public HikariConfig() {
        this.maximumPoolSize = 10;
        this.minimumIdle = 10;
        this.maximumLifetime = 1800000;
        this.keepaliveTime = 0;
        this.connectionTimeout = 5000;
    }

    public HikariConfig(int maximumPoolSize, int minimumIdle, int maximumLifetime, int keepaliveTime, int connectionTimeout) {
        this.maximumPoolSize = maximumPoolSize;
        this.minimumIdle = minimumIdle;
        this.maximumLifetime = maximumLifetime;
        this.keepaliveTime = keepaliveTime;
        this.connectionTimeout = connectionTimeout;
    }

}
