package com.dianxin.core.api.annotations.core;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface NoInternalInstance {
}
