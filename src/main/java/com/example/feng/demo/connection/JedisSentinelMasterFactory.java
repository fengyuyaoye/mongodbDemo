package com.example.feng.demo.connection;

import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.apache.catalina.Host;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.InvalidURIException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.JedisURIHelper;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/24
 */
public class JedisSentinelMasterFactory implements PooledObjectFactory<Jedis> {
    private final int retryTimeWhenRetrieveSlave = 5;
    private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
    private final CopyOnWriteArraySet<HostAndPort> hostAndPortSet = new CopyOnWriteArraySet<>();
    private final int connectionTimeout;
    private final int soTimeout;
    private final String password;
    private final int database;
    private final String clientName;
    private final boolean ssl;
    private final SSLSocketFactory sslSocketFactory;
    private SSLParameters sslParameters;
    private HostnameVerifier hostnameVerifier;
    private final String masterName;

    public JedisSentinelMasterFactory(final String host, final int port, final int connectionTimeout,
        final int soTimeout, final String password, final int database, final String clientName, final boolean ssl,
        final SSLSocketFactory sslSocketFactory, final SSLParameters sslParameters,
        final HostnameVerifier hostnameVerifier, String masterName, Set<String> sentinels) {
        this.hostAndPort.set(new HostAndPort(host, port));
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
        this.masterName = masterName;
        this.hostAndPortSet.addAll(getAllSentinelHostAndPorts(sentinels));
    }

    private Set<HostAndPort> getAllSentinelHostAndPorts(Set<String> sentinels) {
        Set<HostAndPort> result = new HashSet<>();
        if (CollectionUtils.isNotEmpty(sentinels)) {
            for (String sentinel : sentinels) {
                result.add(HostAndPort.parseString(sentinel));
            }
        }
        return result;
    }

    public JedisSentinelMasterFactory(final URI uri, final int connectionTimeout, final int soTimeout,
        final String clientName, final boolean ssl, final SSLSocketFactory sslSocketFactory,
        final SSLParameters sslParameters, final HostnameVerifier hostnameVerifier, String masterName) {
        if (!JedisURIHelper.isValid(uri)) {
            throw new InvalidURIException(
                String.format("Cannot open Redis connection due invalid URI. %s", uri.toString()));
        }
        this.masterName = masterName;
        this.hostAndPort.set(new HostAndPort(uri.getHost(), uri.getPort()));
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = JedisURIHelper.getPassword(uri);
        this.database = JedisURIHelper.getDBIndex(uri);
        this.clientName = clientName;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }

    public void setHostAndPort(final HostAndPort hostAndPort) {
        this.hostAndPort.set(hostAndPort);
    }

    @Override
    public void activateObject(PooledObject<Jedis> pooledJedis) throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.getDB() != database) {
            jedis.select(database);
        }

    }

    @Override
    public void destroyObject(PooledObject<Jedis> pooledJedis) throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                }
                jedis.disconnect();
            } catch (Exception e) {

            }
        }

    }

    @Override
    public PooledObject<Jedis> makeObject() throws Exception {
        List<HostAndPort> slaves = getSentinelSlaves();
        return tryToGetSlave(slaves);
    }

    private List<HostAndPort> getSentinelSlaves() {
        List<HostAndPort> result = new ArrayList<>();
        Jedis jedisSentinel = null;
        try {
            jedisSentinel = getASentinel();
            List<Map<String, String>> slaves = jedisSentinel.sentinelSlaves(this.masterName);
            if (slaves == null || slaves.isEmpty()) {
                throw new JedisException(String.format("No valid slave for master: %s", this.masterName));
            }
            if (CollectionUtils.isNotEmpty(slaves)) {
                for (Map<String, String> slave : slaves) {
                    String host = slave.get("ip");
                    String port = slave.get("port");
                    HostAndPort hostAndPort = new HostAndPort(host, Integer.valueOf(port));
                    result.add(hostAndPort);
                }
            }
            return result;
        } finally {
            if (jedisSentinel != null) {
                jedisSentinel.close();
            }
        }
    }

    private DefaultPooledObject<Jedis> tryToGetSlave(List<HostAndPort> slaves) {
        int retry = retryTimeWhenRetrieveSlave;
        while (retry-- > 0) {
            SecureRandom sr = new SecureRandom();
            int randomIndex = sr.nextInt(slaves.size());
            String host = slaves.get(randomIndex).getHost();
            int port = slaves.get(randomIndex).getPort();

            final Jedis jedis = new Jedis(host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory,
                sslParameters, hostnameVerifier);
            try {
                jedis.connect();
                if (null != this.password) {
                    jedis.auth(this.password);
                }
                if (database != 0) {
                    jedis.select(database);
                }
                if (clientName != null) {
                    jedis.clientSetname(clientName);
                }
                return new DefaultPooledObject<Jedis>(jedis);
            } catch (Exception je) {
                jedis.close();
                slaves.remove(randomIndex);
                continue;
            }
        }
        return null;
    }

    private Jedis getASentinel() {
        for (int i = 0; i < 5; i++) {
            final HostAndPort hostAndPort = this.hostAndPort.get();
            final Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl,
                    sslSocketFactory, sslParameters, hostnameVerifier);
            try {
                jedis.connect();
            } catch (JedisException je) {
                jedis.close();
                hostAndPortSet.remove(hostAndPort);
                for (HostAndPort hp : hostAndPortSet) {
                    this.hostAndPort.set(hp);
                    break;
                }
                continue;
            }
            return jedis;
        }
        return null;
    }

    @Override
    public void passivateObject(PooledObject<Jedis> pooledJedis) throws Exception {
        // TODO maybe should select db 0? Not sure right now.
    }

    @Override
    public boolean validateObject(PooledObject<Jedis> pooledJedis) {
        final BinaryJedis jedis = pooledJedis.getObject();
        try {
            HostAndPort hostAndPort = this.hostAndPort.get();

            String connectionHost = jedis.getClient().getHost();
            int connectionPort = jedis.getClient().getPort();

            return hostAndPort.getHost().equals(connectionHost) && hostAndPort.getPort() == connectionPort
                && jedis.isConnected() && jedis.ping().equals("PONG");
        } catch (final Exception e) {
            return false;
        }
    }
}
