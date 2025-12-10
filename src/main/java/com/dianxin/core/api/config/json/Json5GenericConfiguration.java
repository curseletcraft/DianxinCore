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
 * Json5Configuration hỗ trợ đọc và lưu config JSON5,
 * đồng thời có thể reload lại config khi runtime.
 *
 * @param <T> Kiểu config cụ thể, phải kế thừa AbstractBotConfiguration
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class Json5GenericConfiguration<T extends AbstractBotConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(Json5GenericConfiguration.class);

    private T botConfig;
    private final File configFile;
    private final String defaultResource;
    private final Class<T> clazz;

    /**
     * Tạo config JSON5, nếu file chưa tồn tại sẽ copy từ resource mặc định.
     *
     * @param defaultResource Resource mặc định trong jar (vd: "config.json5")
     * @param filePath        Đường dẫn file config trên server
     * @param clazz           Class<T> của config cụ thể
     * @throws IOException nếu có lỗi đọc ghi
     */
    public Json5GenericConfiguration(String defaultResource, String filePath, Class<T> clazz) throws IOException {
        this.defaultResource = defaultResource;
        this.configFile = new File(filePath);
        this.clazz = clazz;

        ensureFileExists();

        // Load lần đầu
        reloadConfig();
    }

    /** Đảm bảo file config tồn tại, copy từ resource nếu cần */
    private void ensureFileExists() throws IOException {
        if (configFile.exists()) return;

        // Copy từ resource
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(defaultResource)) {
            if (in == null) {
                throw new RuntimeException("Không tìm thấy default config: " + defaultResource);
            }

            // Nếu file nằm ở root project (không có parent), skip mkdirs
            File parent = configFile.getParentFile();
            if (parent != null) parent.mkdirs();

            Files.copy(in, configFile.toPath());
            logger.info("✅ File config mặc định đã được tạo: {}", configFile.getAbsolutePath());
        }
    }

    /** Reload config từ file JSON5 */
    public void reloadConfig() {
        try {
            String json5 = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            this.botConfig = Json5.to(json5, clazz);
            logger.info("✅ Config JSON5 đã được reload thành công từ '{}'", configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("❌ Lỗi khi reload config JSON5 từ '{}'", configFile.getAbsolutePath(), e);
        }
    }

    /** Lưu config hiện tại ra file JSON5 */
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

    public T getBotConfig() {
        return botConfig;
    }
}
