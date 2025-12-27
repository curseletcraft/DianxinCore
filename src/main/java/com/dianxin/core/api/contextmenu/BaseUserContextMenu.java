package com.dianxin.core.api.contextmenu;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseUserContextMenu {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Thực thi khi user context menu được gọi
     *
     * @param event UserContextInteractionEvent
     */
    public abstract void execute(UserContextInteractionEvent event);

    /**
     * Hook trước execute (optional)
     */
    protected boolean beforeExecute(UserContextInteractionEvent event) {
        return true;
    }

    /**
     * Hook sau execute (optional)
     */
    protected void afterExecute(UserContextInteractionEvent event) {}
}
