package com.dianxin.core.api.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Experimental
public class J4CommandHandler {
    private final Map<String, BaseCommandV2> commands = new HashMap<>();

    public void register(BaseCommandV2... cmds) {
        for(BaseCommandV2 cmd : cmds) {
            commands.put(cmd.getClass().getSimpleName(), cmd); // ??
        }
    }

    public void handle(SlashCommandInteractionEvent event) {
        BaseCommandV2 cmd = commands.get(event.getName() + "Command");
        if(cmd != null) cmd.handle(event); // ?
    }
}
