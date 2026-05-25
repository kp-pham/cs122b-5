package common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.naming.InitialContext;
import java.util.Map;

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

    public static String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    public static Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }

    public static long hincrBy(String key, String field, long value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(key, field, value);
        }
    }

    public static void hdel(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(key, field);
        }
    }

    public static long incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
