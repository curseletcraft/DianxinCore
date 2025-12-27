package com.dianxin.core.api.annotations.lifecycle;

import com.dianxin.core.api.services.ToriServices;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterToriService {
    ToriServices.ServiceType[] exclude() default {};
}
