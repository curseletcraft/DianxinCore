package com.dianxin.core.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Deprecated
interface MaincommandRegistry {
    CommandData getCommand();
}

