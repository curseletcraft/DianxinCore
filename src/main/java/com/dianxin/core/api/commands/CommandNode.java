package com.dianxin.core.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

final class CommandNode {
    final String name;
    String description;
    final List<SubcommandData> subcommands = new ArrayList<>();

    CommandNode(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
