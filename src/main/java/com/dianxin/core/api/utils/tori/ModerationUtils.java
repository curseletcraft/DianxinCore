package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.exceptions.ServiceIsAlreadyInitException;
import com.dianxin.core.api.exceptions.ServiceUnavailableException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

// TODO add more funcs
@ApiStatus.AvailableSince("1.1.1")
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class ModerationUtils {
    private static ModerationUtils INSTANCE;
    private final JavaDiscordBot bot;

    private ModerationUtils(JavaDiscordBot bot) {
        this.bot = bot;
    }

    @ApiStatus.Internal
    public static void initialize(@NotNull JavaDiscordBot botInstance) {
        if(INSTANCE != null) {
            throw new ServiceIsAlreadyInitException("ModerationUtils đã được initialize!");
        }
        INSTANCE = new ModerationUtils(botInstance);
    }

    public static ModerationUtils moderationUtils() {
        if (INSTANCE == null) {
            throw new ServiceUnavailableException("ModerationUtils chưa được initialize! " +
                            "Hãy đảm bảo không exclude MODERATION_UTILS trong @RegisterToriService");
        }
        return ModerationUtils.INSTANCE;
    }

    public JDA getJda() {
        return bot.getJda();
    }

    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    public void ban(@NotNull Guild guild, @NotNull User user, String reason) {
        guild.ban(user, 7, TimeUnit.DAYS)
                .reason(reason)
                .queue();
    }



    public void kick(@NotNull Guild guild, @NotNull User user, String reason) {
        guild.kick(user)
                .reason(reason)
                .queue();
    }
}
