package com.dianxin.core.api.commands;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.annotations.commands.*;
import com.dianxin.core.api.annotations.core.NoInternalInstance;
import com.dianxin.core.api.exceptions.command.EmptyStringException;
import com.dianxin.core.api.exceptions.command.InvalidRegistrationNameException;
import com.dianxin.core.api.exceptions.command.MissingAnnotationException;
import com.dianxin.core.api.meta.BotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.ApiStatus.Internal;
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

    /**
     * Khởi tạo Base Command, sử dụng DianxinServices
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
    public BaseCommandV3() {
        this(DianxinCore.getJda(), DianxinCore.getBotMeta());
    }

    /**
     * Khởi tạo Base Command, sử dụng jda thủ công
     * @param jda JDA thủ công được truyền vào
     * @param meta Bot Meta thủ công được truyền vòa
     */
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

    /**
     * Phương thức vận hành command
     * @param event SlashCommandInteractionEvent được truyền
     */
    @Internal
    protected final void handle(SlashCommandInteractionEvent event) {
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

    /**
     * Lấy bộ dữ liệu CommandData của một BaseCommand đã được implement.
     * <br>
     * Đây là khối dữ liệu quan trọng nhất để Discord API (tức JDA) định hình được command cần đăng ký
     * và sử dụng được một cách hợp lệ.
     *
     * @throws MissingAnnotationException Command kế thừa {@link BaseCommandV3} không annotate {@link CommandTree}
     * @throws EmptyStringException Name trong {@link CommandTree} đang để trống
     * @throws InvalidRegistrationNameException {@link CommandTree} với name nhập hơn 2 từ
     *
     * @return CommandData của class
     */
    @Internal
    protected final CommandData buildCommandData() {
        Class<?> clazz = this.getClass();

        CommandTree reg = clazz.getAnnotation(CommandTree.class);
        if (reg == null) {
            throw new MissingAnnotationException(CommandTree.class, clazz);
        }

        // ví dụ @CommandTree(name = "moderation kick", ...) thì split thành moderation là nhánh chính, kick là nhánh phụ
        List<String> branches = List.of(reg.name().split(" "));
        if (branches.isEmpty()) {
            throw new EmptyStringException("@CommandTree name không hợp lệ");
        }

        SlashCommandData commandData = Commands.slash(branches.getFirst(), reg.description()); // đáng ngại

        if (branches.size() > 2) {
            throw new InvalidRegistrationNameException("CommandTree chỉ hỗ trợ tối đa 2 cấp (root sub): " + reg.name());
        }

        // Context types
        if(clazz.isAnnotationPresent(GuildOnly.class)) {
            commandData.setContexts(InteractionContextType.GUILD); // chỉ cho phép đăng ký lệnh trên guild
        } else if(clazz.isAnnotationPresent(DirectMessageOnly.class)) {
            commandData.setContexts(InteractionContextType.BOT_DM);
        } else if(clazz.isAnnotationPresent(PrivateChannelOnly.class)) {
            commandData.setContexts(InteractionContextType.PRIVATE_CHANNEL);
        } else {
            commandData.setContexts(InteractionContextType.ALL);
        }

        if (branches.size() == 2) {
            String subName = branches.get(1);
            SubcommandData sub = new SubcommandData(subName, reg.description());
        }

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
    // =========================================
    // end of command data constructor
}