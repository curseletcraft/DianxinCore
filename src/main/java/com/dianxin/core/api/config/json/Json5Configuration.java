package com.dianxin.core.api.config.json;

import org.apache.juneau.marshaller.Json5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Json5Configuration hỗ trợ đọc config JSON5,
 * đồng thời có thể reload lại config khi runtime.
 * @deprecated Use {@link Json5GenericConfiguration} instead.
 */
@Deprecated
public class Json5Configuration {
    private AbstractBotConfiguration botConfig;
    private final Logger logger = LoggerFactory.getLogger(Json5Configuration.class);

    private final File configFile;
    private final String defaultResource; // resource mặc định trong jar

    public Json5Configuration(String defaultResource, String filePath) throws IOException, IllegalArgumentException {
        this.defaultResource = defaultResource;
        this.configFile = new File(filePath);

        if(!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(defaultResource)) {
                if(in == null) {
                    throw new RuntimeException("Không tìm thấy default config: " + defaultResource);
                }
                configFile.getParentFile().mkdirs();
                Files.copy(in, configFile.toPath());
            }
        }

        // Load lần đầu
        reloadConfig();
    }

    /**
     * Reload config từ file JSON5.
     */
    public void reloadConfig() {
        try {
            String json5 = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            this.botConfig = Json5.to(json5, AbstractBotConfiguration.class);
            logger.info("✅ Config JSON5 đã được reload thành công từ '{}'", configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("❌ Lỗi khi reload config JSON5 từ '{}'", configFile.getAbsolutePath(), e);
        }
    }

    /**
     * Lưu lại config hiện tại ra file.
     */
    public void saveConfig() {
        try {
            String json5 = Json5.of(botConfig);
            Files.writeString(configFile.toPath(), json5, StandardCharsets.UTF_8);
            logger.info("✅ Config JSON5 đã được lưu thành công vào '{}'", configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("❌ Lỗi khi lưu config JSON5 vào '{}'", configFile.getAbsolutePath(), e);
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public AbstractBotConfiguration getBotConfig() {
        return botConfig;
    }
}
