package com.dianxin.core.api.utils.quicksetup;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.exceptions.ServiceUnavailableException;
import com.dianxin.core.fastutil.utils.Checks;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public final class ActivityContext {
    private static final Logger logger = LoggerFactory.getLogger(ActivityContext.class);
    private static JavaDiscordBot bot;
    private static ActivityContext INSTANCE;

    private ActivityContext() { }

    public static void initialize(@NotNull JavaDiscordBot bot) {
        if(INSTANCE != null) {
            throw new UnsupportedOperationException("ActivityContext is already initialized!");
        }

        ActivityContext.bot = bot;
        ActivityContext.INSTANCE = new ActivityContext();
    }

    @NotNull
    public static ActivityContext activityContext() {
        Checks.notNull(INSTANCE, new ServiceUnavailableException("ActivityContext is not initialized!"));
        return INSTANCE;
    }

    @NotNull
    public JavaDiscordBot getBaseBot() {
        Checks.notNull(INSTANCE, new ServiceUnavailableException("ActivityContext is not initialized!"));
        return ActivityContext.bot;
    }

    @Nullable
    public Activity parseActivity(@Nullable String rawType, @NotNull String context, @Nullable String url) {
        if (rawType == null) return null;

        Activity.ActivityType type;
        try {
            type = Activity.ActivityType.valueOf(rawType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Activity type '{}' không hợp lệ, fallback PLAYING", rawType);
            type = Activity.ActivityType.PLAYING;
        }

        return switch (type) {
            case STREAMING -> {
                if (url == null || url.isBlank()) {
                    logger.warn("STREAMING nhưng url rỗng, fallback PLAYING");
                    yield Activity.playing(context);
                }
                yield Activity.streaming(context, url);
            }

            case LISTENING  -> Activity.listening(context);
            case WATCHING   -> Activity.watching(context);
            case COMPETING  -> Activity.competing(context);
            case CUSTOM_STATUS -> Activity.customStatus(context);
            default -> Activity.playing(context);
        };
    }
}
