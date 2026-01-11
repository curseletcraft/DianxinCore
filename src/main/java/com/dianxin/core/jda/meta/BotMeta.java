package com.dianxin.core.jda.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class BotMeta {
    @NotNull private final String botName;
    @Nullable private final String botDescription;
    @Nullable private final String botVersion;
    @NotNull private final List<String> botAuthors;
    @Nullable private final List<String> botContributors;
    @Nullable private final String botWebsite;
    @NotNull private final String botOwnerId;

    public BotMeta(@NotNull String botName,
                   @Nullable String botDescription,
                   @Nullable String botVersion,
                   @NotNull List<String> botAuthors,
                   @Nullable List<String> botContributors,
                   @Nullable String botWebsite,
                   @NotNull String botOwnerId) {
        this.botName = botName;
        this.botDescription = botDescription;
        this.botVersion = botVersion;
        this.botAuthors = botAuthors;
        this.botContributors = botContributors;
        this.botWebsite = botWebsite;
        this.botOwnerId = botOwnerId;
    }

    // Bot meta nhưng đơn giản hơn
    public BotMeta(@NotNull String botName,
                   @NotNull List<String> botAuthors,
                   @NotNull String botOwnerId) {
        this(botName, null, null, botAuthors, null, null, botOwnerId);
    }

    // Getters
    public @NotNull String getBotName() {
        return botName;
    }

    public @Nullable String getBotDescription() {
        return botDescription;
    }

    public @Nullable String getBotVersion() {
        return botVersion;
    }

    public @NotNull List<String> getBotAuthors() {
        return botAuthors;
    }

    public @Nullable String getBotWebsite() {
        return botWebsite;
    }

    public @Nullable List<String> getBotContributors() {
        return botContributors;
    }

    public @NotNull String getBotOwnerId() {
        return botOwnerId;
    }

    public @NotNull String getBotInfo() {
        return botVersion == null ? botName : (botName + " v" + botVersion);
    }
}
