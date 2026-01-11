package com.dianxin.core.jda.annotations.core;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UsingLikeBukkitLogback {

    /**
     * Package logger root
     * Ví dụ: "com.mybot", "vn.user.curseletcraft"
     */
    String basePackage();
}