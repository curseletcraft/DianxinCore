package com.dianxin.core.jda.handler.console.example;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.jda.handler.console.AbstractConsoleCommand;

@SuppressWarnings("unused")
public class StopConsoleCommand extends AbstractConsoleCommand {
    public StopConsoleCommand() {
        super("stop");
    }

    @Override
    public void execute(String[] args) {
        JavaDiscordBot.getJavaDiscordBot().onShutdown();
        System.exit(0);
    }
}
