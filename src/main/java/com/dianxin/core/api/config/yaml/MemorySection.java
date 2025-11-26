package com.dianxin.core.api.config.yaml;

import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Experimental
@SuppressWarnings({"unused"})
public class MemorySection {

    protected Map<String, Object> map = new LinkedHashMap<>();
    protected MemorySection parent;
    protected String name;

    public MemorySection() {}

    public MemorySection(MemorySection parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    // PATH BUILDER
    public String getCurrentPath() {
        if (parent == null) return "";
        return parent.getCurrentPath().isEmpty() ? name : parent.getCurrentPath() + "." + name;
    }

    // GET
    public Object get(String path) {
        if (path == null || path.isEmpty()) return this;

        String[] parts = path.split("\\.");
        MemorySection sec = this;

        for (int i = 0; i < parts.length - 1; i++) {
            Object child = sec.map.get(parts[i]);
            if (!(child instanceof MemorySection)) return null;
            sec = (MemorySection) child;
        }

        return sec.map.get(parts[parts.length - 1]);
    }

    // SET
    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        MemorySection sec = this;

        for (int i = 0; i < parts.length - 1; i++) {
            Object child = sec.map.get(parts[i]);
            if (!(child instanceof MemorySection)) {
                child = new MemorySection(sec, parts[i]);
                sec.map.put(parts[i], child);
            }
            sec = (MemorySection) child;
        }

        sec.map.put(parts[parts.length - 1], value);
    }

    // SECTION GETTER
    public MemorySection getSection(String path) {
        Object o = get(path);
        return o instanceof MemorySection ms ? ms : null;
    }

    // KEYS
    public Set<String> getKeys(boolean deep) {
        Set<String> out = new LinkedHashSet<>();

        for (String key : map.keySet()) {
            out.add(key);

            if (deep && map.get(key) instanceof MemorySection sub)
                for (String s : sub.getKeys(true))
                    out.add(key + "." + s);
        }
        return out;
    }

    // VALUES
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> out = new LinkedHashMap<>();

        for (String key : map.keySet()) {
            Object val = map.get(key);

            if (deep && val instanceof MemorySection sub) {
                for (Map.Entry<String, Object> e : sub.getValues(true).entrySet())
                    out.put(key + "." + e.getKey(), e.getValue());
            } else {
                out.put(key, val);
            }
        }

        return out;
    }

    public boolean contains(String path) {
        return get(path) != null;
    }
}
