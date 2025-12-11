package com.dianxin.core.api.commands;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.annotations.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>BaseCommandV2</h2>
 *
 * Lớp cơ sở cho tất cả các Slash Command trong hệ thống bot.
 * <p>
 * Cung cấp sẵn các tính năng phổ biến:
 * <ul>
 *   <li>Kiểm tra môi trường guild-only</li>
 *   <li>Tự động defer reply (nếu được bật)</li>
 *   <li>Kiểm tra quyền của user và bot</li>
 *   <li>Cơ chế debug tiện dụng</li>
 * </ul>
 *
 * <h3>Cách sử dụng:</h3>
 * <pre>{@code
 * @DebugCommand
 * @DeferReply
 * @GuildOnly
 * @RequirePermissions(value = Permission.MESSAGE_SEND)
 * @RequireSelfPermissions(...)
 * public class PingCommand extends BaseCommandV2 {
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
public abstract class BaseCommandV2 {
    private final Logger logger;
    private final JDA jda;

    public BaseCommandV2() {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = DianxinCore.getJda();
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
        Class<?> clazz = getClass();

        /* ✅ Guild only */
        if (clazz.isAnnotationPresent(GuildOnly.class) && event.getGuild() == null) {
            event.reply("❌ Lệnh này chỉ dùng trong server.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || member == null) {
            event.reply("⚠️ Không thể xác định guild hoặc member.").setEphemeral(true).queue();
            return;
        }

        /* ✅ User permissions */
        RequirePermissions rp = clazz.getAnnotation(RequirePermissions.class);
        if (rp != null) {
            for (Permission p : rp.value()) {
                if (!member.hasPermission(p)) {
                    event.reply("❌ Bạn thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                    return;
                }
            }
        }

        /* ✅ Bot permissions */
        RequireSelfPermissions rsp = clazz.getAnnotation(RequireSelfPermissions.class);
        if (rsp != null) {
            Member self = guild.getSelfMember();
            for (Permission p : rsp.value()) {
                if (!self.hasPermission(p)) {
                    event.reply("❌ Bot thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                    return;
                }
            }
        }

        /* ✅ Defer reply */
        boolean deferred = clazz.isAnnotationPresent(DeferReply.class);
        if (deferred) {
            event.deferReply().queue();
        }

        /* ✅ Execute command */
        Runnable task = () -> {
            try {
                execute(event);
            } catch (Exception e) {
                logger.error("❌ Lỗi khi thực thi command {}", event.getName(), e);
            }
        };

        task.run();

        /* ✅ Debug */
        if (clazz.isAnnotationPresent(DebugCommand.class)) {
            logger.debug("[CMD] {} by {} | {}",
                    event.getName(),
                    event.getUser().getAsTag(),
                    event.getCommandString());
        }
    }

    protected abstract void execute(SlashCommandInteractionEvent event);
}

