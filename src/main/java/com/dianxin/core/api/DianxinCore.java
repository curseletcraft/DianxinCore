package com.dianxin.core.api;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.ApiStatus;

public final class DianxinCore {

    private static Server server;

    private DianxinCore() {}

    @ApiStatus.Internal // chỉ framework được gọi
    static void setServer(Server serverInstance) {
        if (server != null) {
            throw new IllegalStateException("DianxinCore Server đã được khởi tạo!");
        }
        server = serverInstance;
    }

    public static Server getServer() {
        checkInit();
        return server;
    }

    public static JDA getJda() {
        checkInit();
        return server.getJda();
    }

    private static void checkInit() {
        if (server == null) {
            throw new IllegalStateException("DianxinCore chưa được initialize!");
        }
    }

    public static Scheduler getScheduler() {
        checkInit();
        return server.getScheduler();
    }
}
