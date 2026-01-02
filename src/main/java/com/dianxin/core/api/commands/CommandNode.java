package com.dianxin.core.api.commands;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

@Deprecated(since = "1.0.16.2")
@ForRemoval(deadline = "1.1.2")
@SuppressWarnings("unused")
final class CommandNode {
    final String name;
    String description;
    final List<SubcommandData> subcommands = new ArrayList<>();

    CommandNode(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
