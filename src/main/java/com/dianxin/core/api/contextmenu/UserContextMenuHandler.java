package com.dianxin.core.api.contextmenu;

import com.dianxin.core.api.DianxinCore;
import com.dianxin.core.api.JavaDiscordBot;
import com.dianxin.core.api.annotations.contextmenu.ContextMenu;
import com.dianxin.core.api.annotations.core.NoInternalInstance;
import com.dianxin.core.api.commands.BaseCommandV3;
import com.dianxin.core.api.commands.CommandHandler;
import com.dianxin.core.api.exceptions.command.InvalidRegistrationNameException;
import com.dianxin.core.api.exceptions.command.MissingAnnotationException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class UserContextMenuHandler extends ListenerAdapter {
    private final JDA jda;
    private final Map<String, BaseUserContextMenu> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(UserContextMenuHandler.class);

    /**
     * Khởi tạo UserContextMenuHandler, sử dụng DianxinService để lấy JDA
     * @throws IllegalStateException Khi DianxinCore chưa được init, có thể do đang sử dụng {@link NoInternalInstance}
     */
    public UserContextMenuHandler() {
        this(DianxinCore.getJda());
    }

    /**
     * Khởi tạo UserContextMenuHandler, truyền JDA thủ công
     */
    public UserContextMenuHandler(@NotNull JDA jda) {
        this.jda = jda;
        this.jda.addEventListener(this);
    }

    /**
     * Khởi tạo UserContextMenuHandler, truyền JavaDiscordBot
     */
    public <T extends JavaDiscordBot> UserContextMenuHandler(T bot) {
        this(bot.getJda());
    }

    /**
     * Đăng ký một User Context Menu
     *
     * @param contextMenu Instance của context menu
     *
     * @throws MissingAnnotationException
     * @throws InvalidRegistrationNameException
     */
    public void register(@NotNull BaseUserContextMenu contextMenu) {
        Class<?> tClass = contextMenu.getClass();
        if(!tClass.isAnnotationPresent(ContextMenu.class)) {
            throw new MissingAnnotationException(ContextMenu.class, tClass);
        }

        ContextMenu contextMenu1 = tClass.getAnnotation(ContextMenu.class);
        String interactionName = contextMenu1.interactionName();
        if(interactionName.isEmpty()) {
            throw new InvalidRegistrationNameException("Interaction Name trong " + tClass.getSimpleName() +
                    " không được để trống!");
        }

        jda.updateCommands().addCommands(Commands.context(Command.Type.USER, interactionName)).queue();

        commands.put(interactionName, contextMenu);
        logger.info("✅ Registered user context menu: **{}**", interactionName);
    }

    /**
     * ?
     * Cảnh báo: Khuyến khích nên đăng ký từng context menu thay vì một list để tránh trường hợp throw
     * dẫn đến các phần tử còn lại không được duyệt
     * @param contextMenus
     */
    @ApiStatus.Obsolete // obsolete là annotation, tương tự deprecated nhưng yếu hơn
    public void register(@NotNull BaseUserContextMenu... contextMenus) {
        Stream.of(contextMenus).forEach(this::register);
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        BaseUserContextMenu menu = commands.get(event.getName());
        if (menu == null) return;

        try {
            if (!menu.beforeExecute(event)) return;
            menu.execute(event);
            menu.afterExecute(event);
        } catch (Exception e) {
            logger.error("❌ Lỗi context menu {}", event.getName(), e);
            event.reply("❌ Có lỗi xảy ra.").setEphemeral(true).queue();
        }
    }
}
