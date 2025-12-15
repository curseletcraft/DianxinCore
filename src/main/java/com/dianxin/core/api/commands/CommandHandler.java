package com.dianxin.core.api.commands;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.JavaDiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Experimental
public class CommandHandler extends ListenerAdapter {
    private final JDA jda;
    private final Map<String, BaseCommandV3> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    public CommandHandler() {
        this.jda = DianxinCore.getJda();
    }

    public CommandHandler(JDA jda) {
        this.jda = jda;
    }

    public <T extends JavaDiscordBot> CommandHandler(T bot) {
        this.jda = bot.getJda();
    }

    public void register(BaseCommandV3... cmds) {
        for(BaseCommandV3 cmd : cmds) {
            String cmdName = cmd.getClass().getSimpleName();
            commands.put(cmdName, cmd); // ??
            logger.info("✅ Registered command /{}", cmdName);
        }
    }

    public void handle(SlashCommandInteractionEvent event) {
        BaseCommandV3 cmd = commands.get(event.getName() + "Command");
        if(cmd != null) cmd.handle(event); // ?
    }

    public void registerAllCommandsToJDA() {

    }
}
