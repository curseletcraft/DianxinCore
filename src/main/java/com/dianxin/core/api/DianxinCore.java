package com.dianxin.core.api;

import com.dianxin.core.api.annotations.core.NoInternalInstance;
import com.dianxin.core.api.meta.BotMeta;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.ApiStatus;

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

    /**
     * Method Lấy BotMeta của bot discord chính
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
    public static BotMeta getBotMeta() {
        checkInit();
        return meta;
    }

    /**
     * Method Lấy JDA của bot discord chính
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
    public static JDA getJda() {
        checkInit();
        return server.getJda();
    }

    /**
     * Method private kiểm tra xem JDA đã được khai báo trong DianxinCore chưa
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
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
