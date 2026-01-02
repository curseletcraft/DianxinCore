package com.dianxin.core.api.commands;

import com.dianxin.core.api.annotations.commands.CommandTree;
import com.dianxin.core.api.exceptions.EmptyStringException;
import com.dianxin.core.api.exceptions.InvalidRegistrationNameException;
import com.dianxin.core.api.exceptions.MissingAnnotationException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandTreeRegistry {
    private static final Map<String, CommandNode> ROOTS = new HashMap<>();

    private CommandTreeRegistry() {}

    public static void register(BaseCommandV3 cmd) {
        Class<?> clazz = cmd.getClass();

        CommandTree tree = clazz.getAnnotation(CommandTree.class);
        if (tree == null) {
            throw new MissingAnnotationException(CommandTree.class, clazz);
        }

        List<String> branches = List.of(tree.name().split(" "));
        if (branches.isEmpty()) {
            throw new EmptyStringException("CommandTree name trống");
        }
        if (branches.size() > 2) {
            throw new InvalidRegistrationNameException("Chỉ hỗ trợ tối đa 2 cấp: " + tree.name());
        }

        String rootName = branches.getFirst();

        // Lấy hoặc tạo root
        CommandNode root = ROOTS.computeIfAbsent(
                rootName,
                k -> new CommandNode(k, tree.description())
        );

        // Nếu root đã tồn tại → desc không được mâu thuẫn
        if (!root.description.equals(tree.description())
                && branches.size() == 1) {
            throw new InvalidRegistrationNameException(
                    "Root command '" + rootName + "' có description khác nhau"
            );
        }

        // Subcommand
        if (branches.size() == 2) {
            SubcommandData sub = new SubcommandData(branches.get(1), tree.description());

            if (cmd.getOptions() != null) {
                sub.addOptions(cmd.getOptions());
            }

            // Check trùng subcommand
            if (root.subcommands.stream()
                    .anyMatch(s -> s.getName().equals(sub.getName()))) {
                throw new InvalidRegistrationNameException(
                        "Subcommand bị trùng: " + tree.name()
                );
            }

            root.subcommands.add(sub);
        }
    }

    public static List<CommandData> build() {
        List<CommandData> list = new ArrayList<>();

        for (CommandNode root : ROOTS.values()) {
            SlashCommandData data = Commands.slash(root.name, root.description);

            if (!root.subcommands.isEmpty()) {
                data.addSubcommands(root.subcommands);
            }

            list.add(data);
        }
        return list;
    }
}
