package com.dianxin.core.api;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

@Deprecated
final class InternalServer implements Server {

    private final JavaDiscordBot bot;
    private final Scheduler scheduler;

    InternalServer(JavaDiscordBot javaDiscordBot) {
        this.bot = javaDiscordBot;
        this.scheduler = new InternalScheduler();
    }

    @Override
    public @NotNull JDA getJda() {
        return bot.getJda();
    }

    @Override
    public @NotNull Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
        bot.onShutdown();
    }
}
