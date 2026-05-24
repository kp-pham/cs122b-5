package common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.naming.InitialContext;

public class RedisUtil {
    private static JedisPool jedisPool;

    public static void init() {
        if (jedisPool != null) {
            return;
        }

        try {
            InitialContext context = new InitialContext();
            String address = (String) context.lookup("java:comp/env/redis/Address");
            String[] parts = address.split(":");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid Redis. Address format: " + address);
            }

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(50);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(2);
            poolConfig.setTestOnBorrow(true);

            jedisPool = new JedisPool(poolConfig, host, port);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Redis connection pool", e);
        }
    }

    public static String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public static void set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, ttlSeconds);
        }
    }

    public static long increment(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }
}
