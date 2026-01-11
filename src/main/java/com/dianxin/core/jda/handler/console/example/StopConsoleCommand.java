package com.dianxin.core.jda.handler.console.example;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.jda.handler.console.AbstractConsoleCommand;

@SuppressWarnings("unused")
public class StopConsoleCommand extends AbstractConsoleCommand {
    private final JavaDiscordBot bot; // hoặc bot con

    public StopConsoleCommand(JavaDiscordBot bot) {
        super("stop");
        this.bot = bot;
    }

    @Override
    public void execute(String[] args) {
        bot.onShutdown();
        System.exit(0);
    }
}
