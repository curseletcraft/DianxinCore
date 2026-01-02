package com.dianxin.core.fastutil.utils;

public final class Checks {
    public static void notNull(Object arg, String name) {
        if (arg == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    public static void notNull(Object arg, Throwable t) throws Throwable {
        if(arg == null) {
            throw t;
        }
    }

    public static void notNull(Object arg, RuntimeException re) {
        if(arg == null) {
            throw re;
        }
    }
}
