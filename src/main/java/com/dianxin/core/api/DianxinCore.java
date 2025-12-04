package com.dianxin.core.api;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.ApiStatus;

public final class DianxinCore {

    private static Server server;

    private DianxinCore() {}

    @ApiStatus.Internal // chỉ framework được gọi
    static void setServer(Server serverInstance) {
        if (server != null) {
            throw new IllegalStateException("DianxinCore Server đã được khởi tạo! " +
                    "Nếu bạn đang mở 2 bot trong cùng 1 file jar, " +
                    "khuyến khích sử dụng @NoInternalInstance annotation.");
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

    @ApiStatus.Experimental
    public static Scheduler getScheduler() {
        checkInit();
        return server.getScheduler();
    }
}
