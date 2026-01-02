package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.JavaDiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public final class MemberUtils {
    private final JavaDiscordBot bot;
    private static MemberUtils INSTANCE;

    private MemberUtils(JavaDiscordBot bot) {
        this.bot = bot;
    }

    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    public static void initialize(@NotNull JavaDiscordBot bot) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("MemberUtils already initialized");
        }
        INSTANCE = new MemberUtils(bot);
    }

    public static MemberUtils memberUtils() {
        return MemberUtils.INSTANCE;
    }

    public static String mention(Member member) {
        return UserUtils.mention(member.getUser());
    }

    public static String mention(User user) {
        return UserUtils.mention(user);
    }

    // 1 số phương thức khác
}
