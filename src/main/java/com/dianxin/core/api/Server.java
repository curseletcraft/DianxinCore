package com.dianxin.core.api;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public interface Server {
    @NotNull JDA getJda();
    @NotNull Scheduler getScheduler();
    void shutdown();
}
