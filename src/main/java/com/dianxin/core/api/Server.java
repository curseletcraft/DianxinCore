package com.dianxin.core.api;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface Server {
    @NotNull JDA getJda();

    @ApiStatus.Experimental
    @NotNull Scheduler getScheduler();
    void shutdown();
}
