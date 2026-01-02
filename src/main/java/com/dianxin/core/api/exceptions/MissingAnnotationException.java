package com.dianxin.core.api.exceptions;

public class MissingAnnotationException extends IllegalStateException {
    public MissingAnnotationException(String message) {
        super(message);
    }

    public MissingAnnotationException(Class<?> annotationClazz, Class<?> targetClazz) {
        super("Missing @" + annotationClazz.getSimpleName() + " annotation on " + targetClazz.getSimpleName());
    }
}
