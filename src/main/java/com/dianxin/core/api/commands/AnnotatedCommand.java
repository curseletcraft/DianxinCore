package com.dianxin.core.api.commands;

import com.dianxin.core.api.annotations.commands.registry.Command;
import com.dianxin.core.api.annotations.commands.registry.Option;
import com.dianxin.core.api.annotations.commands.registry.Subcommand;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.lang.reflect.Field;

public interface AnnotatedCommand {
    default CommandData buildCommandFromAnnotation() {
        Class<?> clazz = this.getClass();

        // Main command
        if (clazz.isAnnotationPresent(Command.class)) {
            Command cmd = clazz.getAnnotation(Command.class);
            SlashCommandData data = Commands.slash(cmd.name(), cmd.description());

            // đọc các @Option trong class
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Option.class)) {
                    Option o = f.getAnnotation(Option.class);
                    data.addOptions(new OptionData(o.type(), o.name(), o.description(), o.required()));
                    // cannot resolve addOptions in data
                }
            }

            return data;
        }

        // Subcommand
        if (clazz.isAnnotationPresent(Subcommand.class)) {
            Subcommand sub = clazz.getAnnotation(Subcommand.class);
            SubcommandData subData = new SubcommandData(sub.name(), sub.description());

            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Option.class)) {
                    Option o = f.getAnnotation(Option.class);
                    subData.addOptions(new OptionData(
                            o.type(), o.name(), o.description(), o.required()
                    ));
                }
            }

            // trả về như một CommandData để phù hợp wrapper
            return Commands.slash(sub.parent(), "parentHolder").addSubcommands(subData);
        }

        throw new IllegalStateException("Command class must have @Command or @Subcommand");
    }
}

