package com.dianxin.core.api.utils.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ModerationUtils {
    public static void ban(@NotNull Guild guild, @NotNull User user, String reason) {
        guild.ban(user, 7, TimeUnit.DAYS).reason(reason).queue();
    }
}
