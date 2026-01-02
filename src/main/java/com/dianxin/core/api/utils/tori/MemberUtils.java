package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.utils.services.ToriServices;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.1.1")
public final class MemberUtils {
    private final JavaDiscordBot bot = ToriServices.getBaseBot();
    private static MemberUtils INSTANCE;

    private MemberUtils() { }

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
