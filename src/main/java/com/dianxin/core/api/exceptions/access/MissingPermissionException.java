package com.dianxin.core.api.exceptions.access;

import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MissingPermissionException extends AccessDeniedException {
    public MissingPermissionException (Permission... permissions) {
        super("Bot is missing required permissions: " +
                Arrays.stream(permissions)
                        .map(Permission::getName)
                        .collect(Collectors.joining(", "))
        );
    }
}
