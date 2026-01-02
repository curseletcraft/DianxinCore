package com.dianxin.core.api.annotations.lifecycle;

import com.dianxin.core.api.utils.services.SubServiceType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterToriService {
    SubServiceType[] exclude() default {};
}
