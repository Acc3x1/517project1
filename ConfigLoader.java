package com.iso11820.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 配置加载器 - 从 appsettings.json 加载配置
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static AppConfig config;

    /**
     * 加载配置
     */
    public static AppConfig load() {
        if (config != null) {
            return config;
        }

        ObjectMapper mapper = new ObjectMapper();
        // appsettings.json 使用 PascalCase (如 "SqlitePath")，而 Java Bean 属性是 camelCase
        // 需要开启大小写不敏感匹配
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        // 忽略未知字段，避免因配置扩展字段导致启动失败
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (InputStream is = ConfigLoader.class.getResourceAsStream("/appsettings.json")) {
            if (is == null) {
                logger.error("无法找到配置文件 appsettings.json");
                throw new RuntimeException("配置文件不存在");
            }
            config = mapper.readValue(is, AppConfig.class);
            logger.info("配置加载成功");
            return config;
        } catch (IOException e) {
            logger.error("配置加载失败", e);
            throw new RuntimeException("配置加载失败", e);
        }
    }

    /**
     * 获取当前配置
     */
    public static AppConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }
}