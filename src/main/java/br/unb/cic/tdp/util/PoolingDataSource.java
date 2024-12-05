package br.unb.cic.tdp.util;

import lombok.val;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PoolingDataSource {

    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public static DataSource get(final String dataFile) {
        return dataSources.computeIfAbsent(dataFile, file -> setupDataSource("jdbc:h2:" + file));
    }

    private static DataSource setupDataSource(final String connectURI) {
        val connectionFactory = new DriverManagerConnectionFactory(connectURI, null);

        val poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setDefaultAutoCommit(true);

        val connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

        poolableConnectionFactory.setPool(connectionPool);

        return new org.apache.commons.dbcp2.PoolingDataSource<>(connectionPool);
    }
}