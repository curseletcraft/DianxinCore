package com.dianxin.core.api;

import com.dianxin.core.api.handler.console.ConsoleCommandManager;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

@SuppressWarnings("unused")
public abstract class JavaDiscordBot {
    @Getter private JDA jda;
    @Getter private final @NotNull String botName;
    @Getter private final Logger logger;

    @Getter private final ConsoleCommandManager consoleManager = new ConsoleCommandManager();

    private final String botToken;

    public JavaDiscordBot(String token, @NotNull String botName) {
        this.botToken = token;
        this.botName = botName;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void start() throws InterruptedException {
        JDABuilder jdaBuilder;
        EnumSet<GatewayIntent> intents = getIntents();
        Activity activity = getActivity();

        if(intents == null) {
            logger.warn("⚠️ Bot {} không có GatewayIntents — có thể sẽ không nhận được event nào.", botName);
            jdaBuilder = JDABuilder.createDefault(botToken);
        } else {
            jdaBuilder = JDABuilder.createDefault(botToken, intents);
        }

        if(activity != null) {
            jdaBuilder.setActivity(activity);
        }

        this.jda = jdaBuilder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        logger.info("✅ Bot {} đã khởi động thành công.", botName);
        logger.info("Link mời bot: {}", jda.getInviteUrl());

        // Đăng ký lệnh console custom
        registerConsoleCommands();

        // Bắt đầu lắng nghe console
        consoleManager.startListening(this);

        onEnable();
    }

    public void onEnable() { }
    public void onDisable() { }

    public void onShutdown() {
        onDisable();
        logger.info("⏹ Đang tắt bot {}...", botName);
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdown();
    }

    /** Có thể override hoặc để null nếu không cần intents */
    protected EnumSet<GatewayIntent> getIntents() {
        return null;
    }

    /** Có thể override hoặc để null nếu không cần activity */
    protected Activity getActivity() {
        return null;
    }

    public static EnumSet<GatewayIntent> getAllIntents() {
        return EnumSet.allOf(GatewayIntent.class);
    }

    public static EnumSet<GatewayIntent> getDefaultIntents() {
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

    protected void registerConsoleCommands() {
        // Bot con override
    }
}
