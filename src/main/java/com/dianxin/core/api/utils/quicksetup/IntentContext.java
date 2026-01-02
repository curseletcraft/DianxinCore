package com.dianxin.core.api.utils.quicksetup;

import com.dianxin.core.api.utils.services.ToriServices;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@SuppressWarnings("unused")
public final class IntentContext {
    private IntentContext() { }

    @NotNull
    public EnumSet<GatewayIntent> getAllIntents() {
        ToriServices.getJda(); // ignore
        return EnumSet.allOf(GatewayIntent.class);
    }

    @NotNull
    public EnumSet<GatewayIntent> getDefaultIntents() {
        ToriServices.getJda(); // ignore
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
