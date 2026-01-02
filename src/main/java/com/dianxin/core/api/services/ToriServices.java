package com.dianxin.core.api.services;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.annotations.lifecycle.RegisterToriService;
import com.dianxin.core.api.utils.tori.ModerationUtils;
import com.dianxin.core.api.utils.java.DebugUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.EnumSet;
import java.util.List;

public final class ToriServices {
    private static final String DIANXIN_CORE_API_VERSION = "1.0.16.2"; // TODO update lên 1.1.0
    private static final int DIANXIN_SERVICES_VERSION = 2;

    private static boolean initialized = false;

    private ToriServices() {
        throw new UnsupportedOperationException("ToriServices is a bootstrap class");
    }

    @ApiStatus.Internal
    public static <T extends JavaDiscordBot> void initialize(T bot) {
        if (initialized) {
            throw new IllegalStateException("ToriServices đã được initialize!");
        }

        RegisterToriService ann = bot.getClass().getAnnotation(RegisterToriService.class);
        if(ann == null) return;

        EnumSet<ServiceType> enabled = EnumSet.allOf(ServiceType.class);
        List.of(ann.exclude()).forEach(enabled::remove);

        if (enabled.contains(ServiceType.MODERATION_UTILS)) {
            ModerationUtils.initialize(bot);
        }

        if (enabled.contains(ServiceType.DEBUG_UTILS)) {
            DebugUtils.initialize(bot);
        }

        initialized = true;
    }

    public enum ServiceType {
        MODERATION_UTILS,
        COLOR_UTILS,
        DEBUG_UTILS,
        ACTIVITY_CONTEXT,
        INTENT_CONTEXT,
        TIME_UTILS;
    }
}
