package com.dianxin.core.api.annotations.contextmenu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu một class là User Context Menu
 * <br>
 * interactionName sẽ hiển thị trong menu chuột phải
 * của Discord (User context)
 * <br>
 * Ví dụ:
 * {@code
 * @ContextMenu(interactionName = "View Profile")
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextMenu {
    String interactionName();
}
