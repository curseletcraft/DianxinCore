package com.dianxin.core.api.commands;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.annotations.commands.*;
import com.dianxin.core.api.meta.BotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <h4>BaseCommandV3</h4>
 * <p>
 * Lớp cơ sở cho tất cả các Slash Command trong hệ thống bot.
 * <p>
 * Relations:
 * <br>- Annotation: {@link com.dianxin.core.api.annotations.commands}
 *
 * <h3>Cách sử dụng:</h3>
 * <pre>{@code
 * @DebugCommand
 * @DeferReply
 * @GuildOnly
 * @RequirePermissions(value = Permission.MESSAGE_SEND)
 * @RequireSelfPermissions(...)
 * public class PingCommand extends BaseCommandV3 {
 *     public PingCommand(MyBot bot) {
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         event.getHook().sendMessage("🏓 Pong!").queue();
 *     }
 * }
 * }</pre>
 *
 */
@SuppressWarnings("unused")
public abstract class BaseCommandV3 {
    private final Logger logger;
    private final JDA jda;
    private final BotMeta botMeta;

    public BaseCommandV3() {
        this(DianxinCore.getJda(), DianxinCore.getBotMeta());
    }

    public BaseCommandV3(JDA jda, BotMeta meta) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
    }

    /**
     * @return Logger của command hiện tại
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @return Java discord bot chính
     */
    protected JDA getJda() {
        return jda;
    }

    public final void handle(SlashCommandInteractionEvent event) {
        if (!checkOwnerOnly(event)) return;
        if (!checkGuildOnly(event)) return;
        if (!checkUserPermissions(event)) return;
        if (!checkBotPermissions(event)) return;

        applyDeferIfNeeded(event);

        try {
            execute(event);
        } catch (Exception e) {
            logger.error("❌ Lỗi khi thực thi command {}", event.getName(), e);
        }

        logDebug(event);
    }

    // =========================================
    // begin of checker

    private boolean checkOwnerOnly(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(OwnerOnly.class)) return true;

        if(!event.getUser().getId().equals(botMeta.getBotOwnerId())) {
            event.reply("❌ Chỉ owner mới được dùng lệnh này.").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkGuildOnly(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(GuildOnly.class)) return true;

        if (event.getGuild() == null) {
            event.reply("❌ Lệnh này chỉ dùng trong server.").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(SlashCommandInteractionEvent event) {
        RequirePermissions ann = getClass().getAnnotation(RequirePermissions.class);
        if (ann == null) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Không xác định được người dùng.").setEphemeral(true).queue();
            return false;
        }

        for (Permission p : ann.value()) {
            if (!member.hasPermission(p)) {
                event.reply("❌ Bạn thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(SlashCommandInteractionEvent event) {
        RequireSelfPermissions ann = getClass().getAnnotation(RequireSelfPermissions.class);
        if (ann == null) return true;

        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member self = guild.getSelfMember();

        for (Permission p : ann.value()) {
            if (!self.hasPermission(p)) {
                event.reply("❌ Bot thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void applyDeferIfNeeded(SlashCommandInteractionEvent event) {
        if (getClass().isAnnotationPresent(DeferReply.class)) {
            event.deferReply().queue();
        }
    }

    private void logDebug(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(DebugCommand.class)) return;

        logger.debug("[CMD] {} by {} | {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getCommandString()
        );
    }

    // end of checker
    // =========================================


    // =========================================
    // begin of abstract methods

    protected abstract void execute(SlashCommandInteractionEvent event);

    @Nullable
    protected abstract List<OptionData> getOptions();

    @Nullable
    protected abstract List<SubcommandData> getSubCmd();

    // end of abstract methods
    // =========================================

    // =========================================
    // start of command data constructor

    public final CommandData buildCommandData() {
        Class<?> clazz = this.getClass();

        RegisterCommand reg = clazz.getAnnotation(RegisterCommand.class);
        if (reg == null) {
            throw new IllegalStateException("Missing @RegisterCommand annotation on " + clazz.getSimpleName());
        }

        SlashCommandData commandData = Commands.slash(reg.name(), reg.description());

        // -----------------------
        // Context types
        // -----------------------
        if(clazz.isAnnotationPresent(GuildOnly.class)) {
            commandData.setContexts(InteractionContextType.GUILD); // chỉ cho phép đăng ký lệnh trên guild
            /*
            TODO thêm các annotation như @DirectMessageOnly -> InteractionContextType.BOT_DM,
             @PrivateChannelOnly -> InteractionContextType.PRIVATE_CHANNEL
             */
        } else {
            commandData.setContexts(InteractionContextType.ALL);
        }

        // TODO thêm annotation NSFW only?

        // đăng ký các option
        List<OptionData> options = getOptions();
        if (options != null) {
            if (options.size() > 25) {
                throw new IllegalStateException("Quá nhiều options (tối đa 25)!");
            }
            commandData.addOptions(options);
        }

        // đăng ký các sub command
        List<SubcommandData> subcommandDataList = getSubCmd();
        if(subcommandDataList != null) {
            if(subcommandDataList.size() > 25) {
                throw new IllegalStateException("Quá nhiều sub commands (tối đa 25)!");
            }
            commandData.addSubcommands(this.getSubCmd());
        }

        // TODO Sub command tree này chỉ nằm ở layer 1, sau này nếu nhu cầu cao hơn thì cần dùng các phương thức cao hơn nữa
        // như addSubcommandsGroups chẳng hạn
        return commandData;
    }
}