package com.dianxin.core.jda.annotations.lifecycle;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.*;

// TODO
@ApiStatus.AvailableSince("1.2")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AsyncTask {

    /**
     * Optional description (for log/debug)
     */
    String value() default "";
}
