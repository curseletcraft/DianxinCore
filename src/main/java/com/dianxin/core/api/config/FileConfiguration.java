package com.dianxin.core.api.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface FileConfiguration {

    void save(File file) throws IOException;

    void save(String file) throws IOException;

    void load(File file) throws IOException;

    void load(String file) throws IOException;

    void saveConfig();

    void reloadConfig();

    // Core getters
    String getString(String path);
    String getString(String path, String def);

    int getInt(String path);
    int getInt(String path, int def);

    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);

    double getDouble(String path);
    double getDouble(String path, double def);

    long getLong(String path);
    long getLong(String path, long def);

    java.util.List<String> getStringList(String path);
    java.util.List<?> getList(String path);

    MemorySection getSection(String path);

    boolean contains(String path);
    boolean contains(String path, boolean ignoreDefault);

    boolean isSet(String path);

    Set<String> getKeys(boolean deep);
    Map<String, Object> getValues(boolean deep);
}
