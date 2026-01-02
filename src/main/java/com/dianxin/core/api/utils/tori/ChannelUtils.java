package com.dianxin.core.api.utils.tori;

import com.dianxin.core.api.utils.services.ToriServices;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.1.1")
public final class ChannelUtils {
    private ChannelUtils() {}

    public static Channel getChannelById(String id) {
        return ToriServices.getJda().getChannelById(Channel.class, id);
    }

    public static TextChannel getTextChannelById(String id) {
        return ToriServices.getJda().getTextChannelById(id);
    }

    public static VoiceChannel getVoiceChannelById(String id) {
        return ToriServices.getJda().getVoiceChannelById(id);
    }
}
