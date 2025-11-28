package com.dianxin.core.api.commands.experimental;

import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.commands.BaseCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.ApiStatus;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý và auto-register các slash commands được đánh dấu bằng {@link SlashInfo}.
 */
@ApiStatus.Experimental
public class SlashCommandRegistry<T extends JavaDiscordBot> extends ListenerAdapter {
    private final T bot;
    private final Map<String, BaseCommand<T>> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param bot Bot hiện tại
     */
    public SlashCommandRegistry(T bot) {
        this.bot = bot;
    }

    /**
     * Quét package chỉ định để tìm và đăng ký command.
     *
     * @param basePackage package gốc, ví dụ: "mino.dx.curseletcraft.ajax.zetabytemain.commands"
     */
    public void registerAll(String basePackage) {
        Reflections reflections = new Reflections(basePackage);

        reflections.getSubTypesOf(BaseCommand.class).forEach(clazz -> {
            SlashInfo info = clazz.getAnnotation(SlashInfo.class);
            if (info == null) return;

            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(bot.getClass());
                constructor.setAccessible(true);

                @SuppressWarnings("unchecked")
                BaseCommand<T> instance = (BaseCommand<T>) constructor.newInstance(bot);

                commands.put(info.name(), instance);
                logger.info("✅ Đã đăng ký lệnh /{} ({})", info.name(), info.description());
            } catch (Exception e) {
                logger.error("❌ Không thể khởi tạo lệnh {}: {}", clazz.getSimpleName(), e.getMessage());
            }
        });

        // Thêm listener để nhận event slash command
        bot.getJda().addEventListener(this);
    }

    /**
     * Gọi command khi có SlashCommandInteractionEvent.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        BaseCommand<T> command = commands.get(event.getName());
        if (command != null) {
            command.handle(event);
        }
    }

    /**
     * @return Map chứa toàn bộ commands đã đăng ký
     */
    public Map<String, BaseCommand<T>> getCommands() {
        return commands;
    }
}

