package com.dianxin.core.api.commands;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.annotations.core.NoInternalInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class CommandHandler extends ListenerAdapter {
    private final JDA jda;
    private final Map<String, BaseCommandV3> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * Khởi tạo CommandHandler, sử dụng DianxinService để lấy JDA
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
    public CommandHandler() {
        this(DianxinCore.getJda());
    }

    /**
     * Khởi tạo CommandHandler, truyền JDA thủ công
     */
    public CommandHandler(@NotNull JDA jda) {
        this.jda = jda;
        this.jda.addEventListener(this);
    }

    /**
     * Khởi tạo CommandHandler, truyền JavaDiscordBot
     */
    public <T extends JavaDiscordBot> CommandHandler(T bot) {
        this(bot.getJda());
    }

    /**
     * Phương thức đăng ký command
     * @param cmds Các lệnh cần thiết
     */
    public void register(@NotNull BaseCommandV3... cmds) {
        CommandListUpdateAction action = jda.updateCommands();
        for(BaseCommandV3 cmd : cmds) {
            String cmdName = cmd.getClass().getSimpleName();
            commands.put(cmdName, cmd); // ??
            logger.info("✅ Registered command /{}", cmdName);
            action = action.addCommands(cmd.buildCommandData());
        }
        action.queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        BaseCommandV3 cmd = commands.get(event.getName() + "Command");
        if(cmd != null) cmd.handle(event); // ?
    }
}
