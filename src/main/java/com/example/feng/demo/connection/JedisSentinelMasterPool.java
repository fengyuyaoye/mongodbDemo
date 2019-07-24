package com.example.feng.demo.connection;

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

    protected Set<JedisSentinelMasterPool.MasterListener> masterListeners =
        new HashSet<JedisSentinelMasterPool.MasterListener>();

    protected Logger log = Logger.getLogger(getClass().getName());

    private volatile JedisSentinelMasterFactory factory;
    private volatile HostAndPort currentHostMaster;

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

        HostAndPort master = initSentinels(sentinels, masterName);
        initPool(master);
    }

    public void destroy() {
        for (JedisSentinelMasterPool.MasterListener m : masterListeners) {
            m.shutdown();
        }

        super.destroy();
    }

    public HostAndPort getCurrentHostMaster() {
        return currentHostMaster;
    }

    private void initPool(HostAndPort master) {
        if (!master.equals(currentHostMaster)) {
            currentHostMaster = master;
            if (factory == null) {
                factory = new JedisSentinelMasterFactory(master.getHost(), master.getPort(), connectionTimeout,
                    soTimeout, password, database, clientName, false, null, null, null, masterName);
                initPool(poolConfig, factory);
            } else {
                factory.setHostAndPort(currentHostMaster);
                // although we clear the pool, we still have to check the
                // returned object
                // in getResource, this call only clears idle instances, not
                // borrowed instances
                internalPool.clear();
            }

            log.info("Created JedisPool to master at " + master);
        }
    }

    private HostAndPort initSentinels(Set<String> sentinels, final String masterName) {

        HostAndPort aSentinel = null;
        boolean sentinelAvailable = false;

        log.info("Trying to find a valid sentinel from available Sentinels...");

        for (String sentinelStr : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinelStr);

            log.info("Connecting to Sentinel " + hap);

            Jedis jedis = null;
            try {
                jedis = new Jedis(hap.getHost(), hap.getPort());
                sentinelAvailable = true;

                List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);
                if (masterAddr == null || masterAddr.size() != 2) {
                    log.info("Can not get master addr from sentinel, master name: " + masterName + ". Sentinel: "
                            + hap + ".");
                    continue;
                }

                aSentinel = hap;
                log.info("Found a Redis Sentinel at " + aSentinel);
                break;
            } catch (JedisException e) {
                log.warning("Cannot get master address from sentinel running @ " + hap + ". Reason: " + e
                        + ". Trying next one.");
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        if (aSentinel == null) {
            if (sentinelAvailable) {
                // can connect to sentinel, but master name seems to not monitored
                throw new JedisException(
                        "Can connect to sentinel, but " + masterName + " seems to be not monitored...");
            } else {
                throw new JedisConnectionException(
                        "All sentinels down, cannot determine where is " + masterName + " master is running...");
            }
        }

        log.info("Found Redis sentinel running at " + aSentinel + ", starting Sentinel listeners...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);
            JedisSentinelMasterPool.MasterListener masterListener =
                    new JedisSentinelMasterPool.MasterListener(masterName, hap.getHost(), hap.getPort());
            // whether MasterListener threads are alive or not, process can be stopped
            masterListener.setDaemon(true);
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return aSentinel;
    }

    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));

        return new HostAndPort(host, port);
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

                    j.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            log.fine("Sentinel " + host + ":" + port + " published: " + message + ".");

                            String[] switchMasterMsg = message.split(" ");

                            if (switchMasterMsg.length > 3) {

                                if (masterName.equals(switchMasterMsg[0])) {
                                    initPool(toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
                                } else {
                                    log.fine("Ignoring message on +switch-master for master name " + switchMasterMsg[0]
                                        + ", our master name is " + masterName);
                                }

                            } else {
                                log.severe("Invalid message received on Sentinel " + host + ":" + port
                                    + " on channel +switch-master: " + message);
                            }
                        }
                    }, "+switch-master");

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
    }
}
