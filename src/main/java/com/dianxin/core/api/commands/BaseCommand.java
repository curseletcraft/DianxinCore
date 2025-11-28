package com.dianxin.core.api.commands;

import com.dianxin.core.api.JavaDiscordBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * <h2>BaseCommand</h2>
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
 * public class PingCommand extends BaseCommand<MyBot> {
 *     public PingCommand(MyBot bot) {
 *         super(bot, true, false, true);
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         event.getHook().sendMessage("🏓 Pong!").queue();
 *     }
 *
 *     @Override
 *     public Permission requirePermission() {
 *         return Permission.MESSAGE_SEND;
 *     }
 * }
 * }</pre>
 *
 * @param <T> Loại bot kế thừa từ {@link JavaDiscordBot}
 */
@SuppressWarnings("unused")
@ApiStatus.Experimental
public abstract class BaseCommand<T extends JavaDiscordBot> {
    /** Tham chiếu đến bot hiện tại */
    private final T bot;

    /** Logger riêng cho từng command */
    private final Logger logger;

    /** Nếu true → tự động defer reply */
    private final boolean defer;

    /** Nếu true → chỉ được dùng trong server (không chạy ở DM) */
    private final boolean guildOnly;

    /** Nếu true → bật debug log */
    private final boolean debugEnabled;

    /**
     * Khởi tạo BaseCommand.
     *
     * @param bot Bot chính đang sử dụng command
     * @param defer Có tự động defer reply trước khi thực thi không
     * @param guildOnly Có chỉ cho phép chạy trong guild không
     * @param debugEnabled Có bật debug log không
     */
    public BaseCommand(T bot, boolean defer, boolean guildOnly, boolean debugEnabled) {
        this.bot = bot;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.defer = defer;
        this.guildOnly = guildOnly;
        this.debugEnabled = debugEnabled;
    }

    /**
     * Xử lý logic khi slash command được gọi.
     * <p>Đây là entrypoint mặc định cho tất cả commands.</p>
     *
     * @param event Sự kiện slash command
     */
    public final void handle(SlashCommandInteractionEvent event) {
        // ✅ Chặn DM nếu command chỉ dành cho guild
        if (guildOnly && event.getGuild() == null) {
            event.reply("❌ Lệnh này chỉ dùng trong server.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || member == null) {
            event.reply("⚠️ Không thể xác định thông tin người dùng hoặc máy chủ.").setEphemeral(true).queue();
            return;
        }

        // ✅ Kiểm tra quyền user
        Collection<Permission> required = requirePermissions();
        for(Permission p : required) {
            if(!member.hasPermission(p)) {
                event.reply("❌ Bạn không có quyền `" + p.getName() + "` để dùng lệnh này.").setEphemeral(true).queue();
                return;
            }
        }

        // ✅ Kiểm tra quyền bot
        Collection<Permission> selfRequired = requireSelfPermissions();
        Member self = guild.getSelfMember();
        for (Permission p : selfRequired) {
            if (!self.hasPermission(p)) {
                event.reply("❌ Bot thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return;
            }
        }

        // ✅ Tự động defer nếu cần
        if (defer) {
            event.deferReply().queue();
        }

        // ✅ Gọi hàm thực thi
        execute(event);

        // ✅ In debug log nếu bật
        debug(event.getName(), event.getUser().getAsTag(), event.getCommandString());
    }

    /**
     * Ghi log debug (nếu được bật).
     *
     * @param objects Các object cần log
     */
    protected final void debug(Object... objects) {
        if(debugEnabled) {
            logger.debug(Arrays.toString(objects));
        }
    }

    /**
     * Yêu cầu quyền cho user (nếu cần).
     * <p>Nếu trả về null → không yêu cầu quyền cụ thể.</p>
     *
     * @return Permission cần có, hoặc {@code null} nếu không yêu cầu
     */
    @NotNull
    protected Collection<Permission> requirePermissions() {
        return Collections.emptyList();
    }

    /**
     * <p><b>Example code:</b><br>
     * <pre><code>
     * public class ExampleCommand extends BaseCommand
     * {
     *    {@literal @Override}
     *     public Collection<Permission> requireSelfPermissions() {
     *         return Arrays.asList(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES);
     *     }
     * }
     * </code></pre>
     * @return Self permissions for bot
     */
    @NotNull
    protected Collection<Permission> requireSelfPermissions() {
        return Collections.emptyList();
    }

    /**
     * Thực thi logic của command.
     *
     * @param event Sự kiện slash command
     */
    public abstract void execute(SlashCommandInteractionEvent event);

    /**
     * @return Logger của command hiện tại
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @return Bot chính đang được command này sử dụng
     */
    protected T getBot() {
        return bot;
    }
}
