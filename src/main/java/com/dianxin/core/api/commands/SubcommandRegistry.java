package com.dianxin.core.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Deprecated
interface SubcommandRegistry {
    SubcommandData getSubcommand();
}