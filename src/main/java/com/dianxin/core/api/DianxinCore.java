package com.dianxin.core.api;

import com.dianxin.core.api.meta.BotMeta;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.ApiStatus;

@Deprecated
public final class DianxinCore {

    private static Server server;
    private static BotMeta meta;
    private static final String ALREADY_INITIALIZED = "DianxinCore Server đã được khởi tạo! " +
            "Nếu bạn đang mở 2 bot trong cùng 1 file jar, " +
            "khuyến khích sử dụng @NoInternalInstance annotation.";

    private DianxinCore() {}

    @ApiStatus.Internal // chỉ framework được gọi
    static void setServer(Server serverInstance, BotMeta metaInstance) {
        if (server != null) {
            throw new IllegalStateException(ALREADY_INITIALIZED);
        }
        server = serverInstance;
        meta = metaInstance;
    }

    public static Server getServer() {
        checkInit();
        return server;
    }
    public static BotMeta getBotMeta() {
        checkInit();
        return meta;
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
