package com.dianxin.core.jda.utils.tori;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.fastutil.exceptions.ServiceUnavailableException;
import com.dianxin.core.jda.utils.services.ToriServices;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused"})
public final class ChannelUtils {
    @NotNull
    private static final JavaDiscordBot bot = ToriServices.getBaseBot();

    @NotNull
    private static final JDA jda = bot.getJda();

    private ChannelUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @throws ServiceUnavailableException nếu ToriService chưa được init
     */
    public static Channel getChannelById(String id) {
        return jda.getChannelById(Channel.class, id);
    }

    /**
     * @throws ServiceUnavailableException nếu ToriService chưa được init
     */
    public static TextChannel getTextChannelById(String id) {
        return jda.getTextChannelById(id);
    }

    /**
     * @throws ServiceUnavailableException nếu ToriService chưa được init
     */
    public static VoiceChannel getVoiceChannelById(String id) {
        return jda.getVoiceChannelById(id);
    }
}
