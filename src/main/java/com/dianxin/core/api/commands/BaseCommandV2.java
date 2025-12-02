package com.dianxin.core.api.commands;

import com.dianxin.core.api.annotations.commands.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCommandV2 {
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

