package com.dianxin.core.api.commands.experimental;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface SubcommandRegistry {
    SubcommandData getSubcommand();
}
