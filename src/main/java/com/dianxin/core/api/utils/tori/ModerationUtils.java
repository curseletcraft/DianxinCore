package com.dianxin.core.api.utils.tori;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

// TODO add more funcs
@ApiStatus.AvailableSince("1.1.1")
@SuppressWarnings({"unused"})
public final class ModerationUtils {
    private ModerationUtils() { }

    public static void ban(@NotNull Guild guild, @NotNull User user, String reason) {
        guild.ban(user, 7, TimeUnit.DAYS)
                .reason(reason)
                .queue();
    }

    public static void kick(@NotNull Guild guild, @NotNull User user, String reason) {
        guild.kick(user)
                .reason(reason)
                .queue();
    }
}
