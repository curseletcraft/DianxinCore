package com.dianxin.core.api.config;

import java.util.*;

@SuppressWarnings("unused")
public class MemoryConfiguration extends MemorySection {

    protected MemorySection defaults;

    public MemoryConfiguration() {
        super(null, "");
    }

    public void addDefault(String path, Object value) {
        if (defaults == null) defaults = new MemorySection(null, "defaults");
        defaults.set(path, value);
    }

    public void addDefaults(Map<String, Object> map) {
        for (var e : map.entrySet()) addDefault(e.getKey(), e.getValue());
    }

    @Override
    public Object get(String path) {
        Object val = super.get(path);
        if (val != null) return val;
        return defaults != null ? defaults.get(path) : null;
    }

    public boolean contains(String path, boolean ignoreDefault) {
        if (ignoreDefault) return super.get(path) != null;
        return get(path) != null;
    }

    public boolean isSet(String path) {
        return get(path) != null;
    }

    // GETTERS LIKE BUKKIT
    public String getString(String path) {
        Object o = get(path);
        return o == null ? null : String.valueOf(o);
    }

    public String getString(String path, String def) {
        String v = getString(path);
        return v == null ? def : v;
    }

    public int getInt(String path) {
        Object o = get(path);
        return o instanceof Number n ? n.intValue() : 0;
    }

    public int getInt(String path, int def) {
        Object o = get(path);
        return o instanceof Number n ? n.intValue() : def;
    }

    public boolean getBoolean(String path) {
        Object o = get(path);
        return o instanceof Boolean b ? b : false;
    }

    public boolean getBoolean(String path, boolean def) {
        Object o = get(path);
        return o instanceof Boolean b ? b : def;
    }

    public long getLong(String path) {
        Object o = get(path);
        return o instanceof Number n ? n.longValue() : 0L;
    }

    public long getLong(String path, long def) {
        Object o = get(path);
        return o instanceof Number n ? n.longValue() : def;
    }

    public double getDouble(String path) {
        Object o = get(path);
        return o instanceof Number n ? n.doubleValue() : 0.0;
    }

    public double getDouble(String path, double def) {
        Object o = get(path);
        return o instanceof Number n ? n.doubleValue() : def;
    }

    public List<String> getStringList(String path) {
        Object o = get(path);
        if (o instanceof List<?> list) {
            List<String> s = new ArrayList<>();
            for (Object x : list) s.add(String.valueOf(x));
            return s;
        }
        return new ArrayList<>();
    }

    public List<?> getList(String path) {
        Object o = get(path);
        return o instanceof List<?> list ? list : new ArrayList<>();
    }
}
