package com.cqu.rbac.config;

import com.cqu.rbac.common.exception.DaoException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 配置管理器。
 * <p>设计模式：单例 Singleton——饿汉式私有构造 + 静态实例。</p>
 * <p>加载 classpath 下的 db.properties；系统属性（-Dkey=value）优先于文件配置，便于临时覆盖。</p>
 */
public final class ConfigManager {

    /** 单例实例（饿汉式，类加载即初始化，天然线程安全） */
    private static final ConfigManager INSTANCE = new ConfigManager();

    /** 配置文件路径 */
    private static final String CONFIG_FILE = "db.properties";

    private final Properties properties = new Properties();

    private ConfigManager() {
        load();
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    /** 加载 classpath:db.properties（UTF-8 读取，避免中文注释乱码） */
    private void load() {
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new DaoException("配置文件 " + CONFIG_FILE + " 未找到，请确认位于 src/main/resources 下");
            }
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new DaoException("加载配置文件 " + CONFIG_FILE + " 失败", e);
        }
    }

    /**
     * 读取字符串配置：优先系统属性（-D 参数），其次配置文件。
     */
    public String getString(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isEmpty()) {
            return sys.trim();
        }
        return properties.getProperty(key);
    }

    /** 读取字符串配置，为空时返回默认值 */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value == null || value.isEmpty() ? defaultValue : value.trim();
    }

    /** 读取整形配置，解析失败/缺失时返回默认值 */
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 读取布尔配置 */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
