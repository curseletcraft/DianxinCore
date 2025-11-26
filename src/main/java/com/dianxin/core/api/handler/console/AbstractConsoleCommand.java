package com.dianxin.core.api.handler.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConsoleCommand {
    private final Logger logger;
    private final String commandLine;

    public AbstractConsoleCommand(String commandLine) {
        this.commandLine = commandLine.trim();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /** Lệnh được thực thi, truyền tham chiếu bot */
    public abstract void execute(String[] args);

    protected Logger getLogger() {
        return logger;
    }

    public String getCommandLine() {
        return commandLine;
    }
}
