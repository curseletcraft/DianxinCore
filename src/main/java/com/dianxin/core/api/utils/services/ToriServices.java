package com.dianxin.core.api.utils.services;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.annotations.lifecycle.RegisterToriService;
import com.dianxin.core.api.exceptions.lifecycle.ServiceUnavailableException;
import com.dianxin.core.api.meta.BotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class ToriServices {
    private static final String TORI_SERVICE_API_VERSION = "4";
    private static final String TORI_SERVICES_VERSION = "1.1.1";

    private static boolean initialized = false;

    private static JavaDiscordBot bot;
    @Nullable private static JDA jda;

    private ToriServices() {
        throw new UnsupportedOperationException("ToriServices is a bootstrap class");
    }

    @ApiStatus.Internal
    public static <T extends JavaDiscordBot> void initialize(T bot) {
        if (initialized) {
            throw new IllegalStateException("ToriServices has already initialized!");
        }

        ToriServices.bot = bot;
        ToriServices.jda = bot.getJda();

        RegisterToriService ann = bot.getClass().getAnnotation(RegisterToriService.class);
        if(ann == null) return;

        initialized = true;
    }

    /**
     * Lấy base bot của bot đã được ứng
     * @return JavaDiscordBot, không phải bot được extend
     * @throws ServiceUnavailableException nếu bot không được init, hoặc do chưa annotate {@link RegisterToriService}
     */
    public static JavaDiscordBot getBaseBot() {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return bot;
    }

    public static BotMeta getBotMeta() {
        if(bot == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return bot.getMeta();
    }

    public static JDA getJda() {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return jda;
    }

    public static User getSelf() {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return jda.getSelfUser();
    }

    public static String getBotInviteLink() {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return jda.getInviteUrl();
    }

    public static String getBotInviteLink(Permission... permissions) {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return jda.getInviteUrl(permissions);
    }

    public static void shutdown() {
        if(jda == null || bot == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        // TODO add scheduler shutdown
        bot.onShutdown();
    }

    // others
    public static String getToriServiceApiVersion() {
        return TORI_SERVICE_API_VERSION;
    }

    public static String getToriServiceVersion() {
        return TORI_SERVICES_VERSION;
    }
}
