package com.dianxin.core.api.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.util.*;

public class YamlConfiguration extends MemoryConfiguration implements FileConfiguration {

    private final Yaml yaml;
    private File file;
    private final String defaultResourceName;

    // Constructor chính
    public YamlConfiguration(String defaultResourceName, String filePath) {
        this(defaultResourceName);
        this.file = new File(filePath);
    }

    // Constructor default
    public YamlConfiguration(String defaultResourceName) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        this.defaultResourceName = defaultResourceName;
    }

    // Constructor rỗng
    public YamlConfiguration() {
        this(null);
    }

    @Override
    public void load(File file) throws IOException {
        this.file = file;

        if (!file.exists()) {
            createEmpty(file);
            return;
        }

        try (InputStream input = new FileInputStream(file)) {
            Object loaded = yaml.load(input);
            if (loaded instanceof Map<?, ?> map) {
                this.map = convert(map);
            }
        }
    }

    @Override
    public void load(String path) throws IOException {
        load(new File(path));
    }

    private void createEmpty(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        file.createNewFile();
        this.map = new LinkedHashMap<>();
    }

    @Override
    public void saveDefaultConfig() {

        if (file == null)
            throw new IllegalStateException("No file path assigned for config.");

        if (file.exists()) return; // Đã có → bỏ qua

        if (defaultResourceName == null)
            throw new IllegalStateException("Default resource name is null.");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(defaultResourceName)) {

            if (input == null)
                throw new RuntimeException("Default resource not found: " + defaultResourceName);

            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();

            try (OutputStream output = new FileOutputStream(file)) {
                input.transferTo(output);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save default config: " + file.getName(), e);
        }
    }

    @Override
    public void save(File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            yaml.dump(map, writer);
        }
    }

    @Override
    public void save(String path) throws IOException {
        save(new File(path));
    }

    @Override
    public void saveConfig() {
        if (file == null) return;
        try {
            save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + file.getName(), e);
        }
    }

    @Override
    public void reloadConfig() {
        if (file == null) return;
        try {
            load(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reload config: " + file.getName(), e);
        }
    }

    private Map<String, Object> convert(Map<?, ?> input) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> e : input.entrySet()) {
            String key = Objects.toString(e.getKey());
            Object value = e.getValue();

            if (value instanceof Map<?, ?> child) {
                result.put(key, convert(child));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }
}
