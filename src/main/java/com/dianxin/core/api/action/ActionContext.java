package com.dianxin.core.api.action;

import java.util.Map;

public class ActionContext {

    private final Map<String, Object> data;

    public ActionContext(Map<String, Object> data) {
        this.data = Map.copyOf(data);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> asMap() {
        return data;
    }
}
