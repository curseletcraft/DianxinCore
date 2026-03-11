package com.dianxin.core.jda.commands.console;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.api.console.commands.AbstractConsoleCommand;

@SuppressWarnings("unused")
public class StopConsoleCommand extends AbstractConsoleCommand {
    public StopConsoleCommand() {
        super("stop");
    }

    @Override
    public void execute(String[] args) {
        JavaDiscordBot.getJavaDiscordBot().onShutdown();
    }
}
