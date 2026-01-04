package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.utils.services.ToriServices;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused"})
public final class ModerationUtils {
    @NotNull private static final JavaDiscordBot bot = ToriServices.getBaseBot();
    @NotNull private static final JDA jda = bot.getJda();

    private ModerationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Ban user khỏi guild (xóa 7 ngày tin nhắn).
     */
    public static void ban(@NotNull Guild guild, @NotNull User user) {
        ban(guild, user, null, 7);
    }

    /**
     * Ban user với lý do (xóa 7 ngày tin nhắn).
     */
    public static void ban(@NotNull Guild guild, @NotNull User user, @Nullable String reason) {
        ban(guild, user, reason, 7);
    }

    /**
     * Ban user với số ngày xóa tin nhắn.
     */
    public static void ban(@NotNull Guild guild, @NotNull User user, int days) {
        ban(guild, user, null, days);
    }

    /**
     * Ban user với lý do và số ngày xóa tin nhắn.
     *
     * @param days số ngày xóa tin nhắn (0–7)
     */
    public static void ban(@NotNull Guild guild, @NotNull User user, @Nullable String reason, int days) {
        guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();
    }

    /**
     * Kick user khỏi guild.
     */
    public static void kick(@NotNull Guild guild, @NotNull User user) {
        guild.kick(user).queue();
    }

    /**
     * Kick user với lý do.
     */
    public static void kick(@NotNull Guild guild, @NotNull User user, @Nullable String reason) {
        guild.kick(user).reason(reason).queue();
    }


    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(12);

    /**
     * Timeout member với thời gian mặc định (12h), không lý do.
     */
    public static void timeout(@NotNull Member member) {
        timeout(member, DEFAULT_TIMEOUT, null);
    }

    /**
     * Timeout member với lý do, thời gian mặc định (12h).
     */
    public static void timeout(@NotNull Member member, @Nullable String reason) {
        timeout(member, DEFAULT_TIMEOUT, reason);
    }

    /**
     * Timeout member với thời gian chỉ định, không lý do.
     */
    public static void timeout(@NotNull Member member, @NotNull Duration duration) {
        timeout(member, duration, null);
    }

    /**
     * Timeout member với thời gian và lý do.
     */
    public static void timeout(@NotNull Member member, @NotNull Duration duration, @Nullable String reason) {
        member.timeoutFor(duration).reason(reason).queue();
    }

    /**
     * Gỡ timeout của member.
     */
    public static void untimeout(@NotNull Member member) {
        member.removeTimeout().queue();
    }

    /**
     * Thêm role cho member.
     */
    public static void addRole(@NotNull Member member, @NotNull Role role) {
        member.getGuild().addRoleToMember(member, role).queue();
    }

    /**
     * Gỡ role khỏi member.
     */
    public static void removeRole(@NotNull Member member, @NotNull Role role) {
        member.getGuild().removeRoleFromMember(member, role).queue();
    }

    /**
     * Xóa số lượng tin nhắn gần nhất trong channel.
     *
     * @param amount số lượng tin nhắn (1–100)
     */
    public static void purge(@NotNull TextChannel channel, int amount) {
        channel.getHistory()
                .retrievePast(Math.min(amount, 100))
                .queue(channel::purgeMessages);
    }

    public static void changeNickname(@NotNull Member member, @Nullable String nickname) {
        member.modifyNickname(nickname).queue();
    }

    /**
     * Kiểm tra member có đủ toàn bộ permission hay không.
     */
    public static boolean hasPermission(@NotNull Member member, @NotNull Permission... permissions) {
        return member.hasPermission(permissions);
    }
}
