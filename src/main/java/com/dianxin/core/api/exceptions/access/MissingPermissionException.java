package com.dianxin.core.api.exceptions.access;

import com.dianxin.core.fastutil.exceptions.AccessDeniedException;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class MissingPermissionException extends AccessDeniedException {
    public MissingPermissionException (Permission... permissions) {
        super("Bot is missing required permissions: " +
                Arrays.stream(permissions)
                        .map(Permission::getName)
                        .collect(Collectors.joining(", "))
        );
    }
}
