package com.dianxin.core.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface SubcommandRegistry {
    SubcommandData getSubcommand();
}