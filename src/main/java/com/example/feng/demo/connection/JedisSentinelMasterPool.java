package com.example.feng.demo.connection;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/24
 */
public class JedisSentinelMasterPool extends Pool<Jedis> {
    protected GenericObjectPoolConfig poolConfig;

    protected int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
    protected int soTimeout = Protocol.DEFAULT_TIMEOUT;

    protected String password;

    protected int database = Protocol.DEFAULT_DATABASE;

    protected String clientName;
    private String masterName;
    private Set<String> sentinels;

    protected Set<JedisSentinelMasterPool.MasterListener> masterListeners =
        new HashSet<JedisSentinelMasterPool.MasterListener>();

    protected Logger log = Logger.getLogger(getClass().getName());

    private volatile JedisSentinelMasterFactory factory;
    private volatile HostAndPort currentHostSentinel;

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig) {
        this(masterName, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels) {
        this(masterName, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, null,
            Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, String password) {
        this(masterName, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, password);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        int timeout, final String password) {
        this(masterName, sentinels, poolConfig, timeout, password, Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        final int timeout) {
        this(masterName, sentinels, poolConfig, timeout, null, Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        final String password) {
        this(masterName, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, password);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        int timeout, final String password, final int database) {
        this(masterName, sentinels, poolConfig, timeout, timeout, password, database);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        int timeout, final String password, final int database, final String clientName) {
        this(masterName, sentinels, poolConfig, timeout, timeout, password, database, clientName);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        final int timeout, final int soTimeout, final String password, final int database) {
        this(masterName, sentinels, poolConfig, timeout, soTimeout, password, database, null);
    }

    public JedisSentinelMasterPool(String masterName, Set<String> sentinels, final GenericObjectPoolConfig poolConfig,
        final int connectionTimeout, final int soTimeout, final String password, final int database,
        final String clientName) {
        this.poolConfig = poolConfig;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.masterName = masterName;
        this.sentinels = sentinels;
        HostAndPort sentinel = initSentinels(sentinels, masterName);
        initPool(sentinel);
    }

    public void destroy() {
        for (JedisSentinelMasterPool.MasterListener m : masterListeners) {
            m.shutdown();
        }

        super.destroy();
    }

    public HostAndPort getCurrentHostSentinel() {
        return currentHostSentinel;
    }

    private void initPool(HostAndPort sentinel) {
        if (!sentinel.equals(currentHostSentinel)) {
            currentHostSentinel = sentinel;
            if (factory == null) {
                factory = new JedisSentinelMasterFactory(sentinel.getHost(), sentinel.getPort(), connectionTimeout,
                    soTimeout, password, database, clientName, false, null, null, null, masterName, sentinels);
                initPool(poolConfig, factory);
            } else {
                factory.setHostAndPort(currentHostSentinel);
                // although we clear the pool, we still have to check the
                // returned object
                // in getResource, this call only clears idle instances, not
                // borrowed instances
                internalPool.clear();
            }

            log.info("Created JedisPool to sentinel at " + sentinel);
        }
    }

    private HostAndPort initSentinels(Set<String> sentinels, final String masterName) {

        HostAndPort sSentinel = null;
        boolean sentinelAvailable = false;

        log.info("Trying to find sSentinel from available Sentinels...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);

            log.fine("Connecting to Sentinel " + hap);

            Jedis jedis = null;
            try {
                jedis = new Jedis(hap.getHost(), hap.getPort());

                List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);

                // connected to sentinel...
                sentinelAvailable = true;

                if (masterAddr == null || masterAddr.size() != 2) {
                    log.warning(
                        "Can not get sSentinel addr, sSentinel name: " + masterName + ". Sentinel: " + hap + ".");
                    continue;
                }

                // sSentinel = toHostAndPort(masterAddr);
                sSentinel = hap;
                log.fine("Found Redis sSentinel at " + sSentinel);
                break;
            } catch (JedisException e) {
                // resolves #1036, it should handle JedisException there's another chance
                // of raising JedisDataException
                log.warning("Cannot get sSentinel address from sentinel running @ " + hap + ". Reason: " + e
                    + ". Trying next one.");
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        if (sSentinel == null) {
            if (sentinelAvailable) {
                // can connect to sentinel, but sSentinel name seems to not
                // monitored
                throw new JedisException(
                    "Can connect to sentinel, but " + masterName + " seems to be not monitored...");
            } else {
                throw new JedisConnectionException(
                    "All sentinels down, cannot determine where is " + masterName + " sSentinel is running...");
            }
        }

        log.info("Redis sSentinel running at " + sSentinel + ", starting Sentinel listeners...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);
            JedisSentinelMasterPool.MasterListener masterListener =
                new JedisSentinelMasterPool.MasterListener(masterName, hap.getHost(), hap.getPort());
            // whether MasterListener threads are alive or not, process can be stopped
            masterListener.setDaemon(true);
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return sSentinel;
    }

    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));

        return new HostAndPort(host, port);
    }

    @Override
    public Jedis getResource() {
        Jedis jedis = super.getResource();
        jedis.setDataSource(this);
        return jedis;
    }

    /**
     * @deprecated starting from Jedis 3.0 this method will not be exposed. Resource cleanup should be done using @see
     *             {@link redis.clients.jedis.Jedis#close()}
     */
    @Override
    @Deprecated
    public void returnBrokenResource(final Jedis resource) {
        if (resource != null) {
            returnBrokenResourceObject(resource);
        }
    }

    /**
     * @deprecated starting from Jedis 3.0 this method will not be exposed. Resource cleanup should be done using @see
     *             {@link redis.clients.jedis.Jedis#close()}
     */
    @Override
    @Deprecated
    public void returnResource(final Jedis resource) {
        if (resource != null) {
            resource.resetState();
            returnResourceObject(resource);
        }
    }

    protected class MasterListener extends Thread {

        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis = 5000;
        protected volatile Jedis j;
        protected AtomicBoolean running = new AtomicBoolean(false);

        protected MasterListener() {}

        public MasterListener(String masterName, String host, int port) {
            super(String.format("MasterListener-%s-[%s:%d]", masterName, host, port));
            this.masterName = masterName;
            this.host = host;
            this.port = port;
        }

        public MasterListener(String masterName, String host, int port, long subscribeRetryWaitTimeMillis) {
            this(masterName, host, port);
            this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
        }

        @Override
        public void run() {

            running.set(true);

            while (running.get()) {

                j = new Jedis(host, port);

                try {
                    // double check that it is not being shutdown
                    if (!running.get()) {
                        break;
                    }

                    j.subscribe(new SentinelSlaveChangePubSub(), "+switch-master");

                } catch (JedisConnectionException e) {

                    if (running.get()) {
                        log.log(Level.SEVERE,
                            "Lost connection to Sentinel at " + host + ":" + port + ". Sleeping 5000ms and retrying.",
                            e);
                        try {
                            Thread.sleep(subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException e1) {
                            log.log(Level.SEVERE, "Sleep interrupted: ", e1);
                        }


                    } else {
                        log.fine("Unsubscribing from Sentinel at " + host + ":" + port);
                    }
                } finally {
                    j.close();
                }
            }
        }

        public void shutdown() {
            try {
                log.fine("Shutting down listener on " + host + ":" + port);
                running.set(false);
                // This isn't good, the Jedis object is not thread safe
                if (j != null) {
                    j.disconnect();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Caught exception while shutting down: ", e);
            }
        }

        private class SentinelSlaveChangePubSub extends JedisPubSub {
            @Override
            public void onMessage(String channel, String message) {
                if (masterName == null) {
                    log.warning("Master Name is null!");
                    throw new InvalidParameterException("Master Name is null!");
                }
                log.info("Get message on chanel: " + channel + " published: " + message + "." + " current sentinel "
                    + host + ":" + port);

                String[] msg = message.split(" ");
                List<String> msgList = Arrays.asList(msg);
                if (msgList.isEmpty()) {
                    return;
                }
                boolean needResetPool = false;
                if (masterName.equalsIgnoreCase(msgList.get(0))) { // message from channel +switch-master
                    // message looks like [+switch-master mymaster 192.168.0.2 6479 192.168.0.1 6479]
                    needResetPool = true;
                }
                int tmpIndex = msgList.indexOf("@") + 1;
                // message looks like [+reboot slave 192.168.0.3:6479 192.168.0.3 6479 @ mymaster 192.168.0.1 6479]
                if (tmpIndex > 0 && masterName.equalsIgnoreCase(msgList.get(tmpIndex))) { // message from other channels
                    needResetPool = true;
                }
                if (needResetPool) {
                    HostAndPort aSentinel = initSentinels(sentinels, masterName);
                    initPool(aSentinel);
                } else {
                    log.info("message is not for master " + masterName);
                }

            }
        }
    }
}
