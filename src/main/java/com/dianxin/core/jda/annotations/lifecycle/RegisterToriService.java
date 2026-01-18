package com.dianxin.core.jda.annotations.lifecycle;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterToriService {
    boolean enableIActionScheduler() default true;
}
