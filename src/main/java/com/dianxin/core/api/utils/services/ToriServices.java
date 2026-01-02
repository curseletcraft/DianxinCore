package com.dianxin.core.api.utils.services;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.annotations.lifecycle.RegisterToriService;
import com.dianxin.core.api.exceptions.ServiceUnavailableException;
import com.dianxin.core.api.utils.quicksetup.ActivityContext;
import com.dianxin.core.api.utils.quicksetup.IntentContext;
import com.dianxin.core.api.utils.tori.ModerationUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("unused")
public final class ToriServices {
    private static final String TORI_SERVICE_API_VERSION = "1.1.0-M1";
    private static final int TORI_SERVICES_VERSION = 2;

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

        EnumSet<SubServiceType> enabled = EnumSet.allOf(SubServiceType.class);
        List.of(ann.exclude()).forEach(enabled::remove);

        if(enabled.contains(SubServiceType.ACTIVITY_CONTEXT)) {
            ActivityContext.initialize(bot);
        }

        if(enabled.contains(SubServiceType.INTENT_CONTEXT)) {
            IntentContext.initialize(bot);
        }

        if (enabled.contains(SubServiceType.MODERATION_UTILS)) {
            ModerationUtils.initialize(bot);
        }

//        if (enabled.contains(SubServiceType.DEBUG_UTILS)) {
//            DebugUtils.initialize(bot);
//        }

        initialized = true;
    }

    public static JavaDiscordBot getBaseBot() {
        if(jda == null) {
            throw new ServiceUnavailableException("ToriServices is not initialized!");
        }
        return bot;
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

    // others
    public static String getToriServiceApiVersion() {
        return TORI_SERVICE_API_VERSION;
    }

    public static int getToriServiceVersion() {
        return TORI_SERVICES_VERSION;
    }
}
