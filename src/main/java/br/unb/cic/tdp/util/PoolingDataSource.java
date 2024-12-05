package br.unb.cic.tdp.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;

public class PoolingDataSource {

    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public static DataSource get(final String dataFile) {
        return dataSources.computeIfAbsent(dataFile, file -> setupDataSource("jdbc:h2:" + file + ";CACHE_SIZE=102400"));
    }

    private static DataSource setupDataSource(final String url) {
        val config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(url);
        return new HikariDataSource(config);
    }
}