package com.dianxin.core.api.commands;

import com.dianxin.core.api.JavaDiscordBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.0.16.2")
public abstract class AbstractCommandManager<T extends JavaDiscordBot> extends ListenerAdapter {
    private final T bot;

    public AbstractCommandManager(T bot) {
        this.bot = bot;
    }

    public abstract void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event);

    public abstract void inits(CommandListUpdateAction commandsTree);
}
