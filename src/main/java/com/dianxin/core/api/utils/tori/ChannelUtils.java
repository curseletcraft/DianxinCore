package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.JavaDiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

public final class ChannelUtils {
    private final JavaDiscordBot bot;
    private static ChannelUtils INSTANCE;
    private static JDA jda;

    private ChannelUtils(JavaDiscordBot bot) {
        this.bot = bot;
        throw new UnsupportedOperationException();
    }

    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    public static void initialize(@NotNull JavaDiscordBot bot) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("MemberUtils already initialized");
        }
        INSTANCE = new ChannelUtils(bot);
        jda = bot.getJda();
    }

    public static Channel getChannelById(String id) {
        return jda.getChannelById(Channel.class, id);
    }

    public static TextChannel getTextChannelById(String id) {
        return jda.getTextChannelById(id);
    }

    public static VoiceChannel getVoiceChannelById(String id) {
        return jda.getVoiceChannelById(id);
    }
}
