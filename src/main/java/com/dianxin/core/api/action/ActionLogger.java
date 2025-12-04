package com.dianxin.core.api.action;

import org.slf4j.Logger;

public class ActionLogger {

    private final Logger logger;

    public ActionLogger(Logger logger) {
        this.logger = logger;
    }

    public void log(IAction action) {
        if (action instanceof IActionResult<?> ar) {
            switch (ar.status()) {
                case SUCCESS -> logger.info(format(ar));
                case WARN    -> logger.warn(format(ar));
                case FAILED  -> logger.error(format(ar), ar.error());
                default      -> logger.debug(format(ar));
            }
        } else {
            logger.debug(format(action));
        }
    }

    private String format(IAction action) {
        return action.name() + " | " + action.context().asMap();
    }
}

