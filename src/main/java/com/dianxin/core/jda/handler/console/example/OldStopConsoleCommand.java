package com.dianxin.core.jda.handler.console.example;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.api.console.commands.AbstractConsoleCommand;

@SuppressWarnings("unused")
public class OldStopConsoleCommand extends AbstractConsoleCommand {
    private final JavaDiscordBot bot; // hoặc bot con

    public OldStopConsoleCommand(JavaDiscordBot bot) {
        super("stop");
        this.bot = bot;
    }

    @Override
    public void execute(String[] args) {
        bot.onShutdown();
        System.exit(0);
    }
}
