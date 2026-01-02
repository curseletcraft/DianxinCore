package com.dianxin.core.api.utils.quicksetup;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.exceptions.ServiceUnavailableException;
import com.dianxin.core.fastutil.utils.Checks;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

@SuppressWarnings("unused")
public final class IntentContext {
    private static final Logger logger = LoggerFactory.getLogger(IntentContext.class);
    private static JavaDiscordBot bot;
    private static IntentContext INSTANCE;

    private IntentContext() { }

    public static void initialize(@NotNull JavaDiscordBot bot) {
        if(INSTANCE != null) {
            throw new UnsupportedOperationException("IntentContext has already initialized!");
        }

        IntentContext.bot = bot;
        IntentContext.INSTANCE = new IntentContext();
    }

    @NotNull
    public static IntentContext intentContext() {
        Checks.notNull(INSTANCE, new ServiceUnavailableException("IntentContext is not initialized!"));
        return IntentContext.INSTANCE;
    }

    @NotNull
    public JavaDiscordBot getBaseBot() {
        Checks.notNull(INSTANCE, new ServiceUnavailableException("IntentContext is not initialized!"));
        return IntentContext.bot;
    }

    @NotNull
    public EnumSet<GatewayIntent> getAllIntents() {
        return EnumSet.allOf(GatewayIntent.class);
    }

    @NotNull
    public EnumSet<GatewayIntent> getDefaultIntents() {
        return EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.AUTO_MODERATION_CONFIGURATION,
                GatewayIntent.AUTO_MODERATION_EXECUTION,
                GatewayIntent.GUILD_MESSAGE_POLLS,
                GatewayIntent.DIRECT_MESSAGE_POLLS
        );
    }
}
