package com.dianxin.core.fastutil.utils;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>[EN]</b> Utility class for inspecting object states using Reflection.<br>
 * Useful for debugging entities without implementing toString() methods manually.
 * <br><br>
 * <b>[VN]</b> Lớp tiện ích để kiểm tra trạng thái object bằng Reflection.<br>
 * Hữu ích để debug các entity mà không cần viết hàm toString() thủ công.
 *
 * Example:
 * public class User {
 *     private String name = "Mino";
 *     private int age = 20;
 *     private String secret = "hidden_token"; // Private vẫn in ra được
 * }
 *
 * Call
 * User user = new User();
 *
 * // Cách 1: In thẳng ra Log
 * DebugUtils.print(user);
 *
 * // Cách 2: Lấy chuỗi String để làm việc khác
 * String debugInfo = DebugUtils.inspect(user);
 * System.out.println(debugInfo);
 *
 * Output
 * [INFO] Debug Object [User]:
 * {
 *   name: "Mino"
 *   age: 20
 *   secret: "hidden_token"
 * }
 */
@ApiStatus.Experimental
public final class DebugUtils {
    private static final Logger logger = LoggerFactory.getLogger(DebugUtils.class);

    /**
     * <b>[EN]</b> Logs all fields of an object to the console (INFO level).<br>
     * <b>[VN]</b> Ghi log tất cả các field của object ra console (level INFO).
     * @param object The object to inspect / Object cần kiểm tra
     */
    public static void print(Object object) {
        if (object == null) {
            logger.info("Debug Object: null");
            return;
        }
        EntityDebuggerContext context = new EntityDebuggerContext(object);
        logger.info("Debug Object [{}]: \n{}", object.getClass().getSimpleName(), context);
    }

    /**
     * <b>[EN]</b> Returns a string representation of the object's fields.<br>
     * <b>[VN]</b> Trả về chuỗi đại diện cho các field của object.
     */
    public static String inspect(Object object) {
        if (object == null) return "null";
        return new EntityDebuggerContext(object).toString();
    }

    /**
     * <b>[EN]</b> Inner class to capture object state.<br>
     * <b>[VN]</b> Class nội bộ để chụp lại trạng thái object.
     */
    public static class EntityDebuggerContext {
        private final Map<String, Object> fieldValues = new LinkedHashMap<>();

        public EntityDebuggerContext(Object obj) {
            if (obj == null) return;
            Class<?> currentClass = obj.getClass();

            // <b>[VN]</b> Duyệt ngược lên cả class cha để lấy hết field (trừ Object.class)
            // <b>[EN]</b> Traverse up to superclasses to get all fields (excluding Object.class)
            while (currentClass != null && currentClass != Object.class) {
                Field[] declaredFields = currentClass.getDeclaredFields();

                for (Field field : declaredFields) {
                    // <b>[VN]</b> Bỏ qua static fields vì nó không thuộc về instance
                    // <b>[EN]</b> Skip static fields as they don't belong to the instance
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    try {
                        // <b>[VN]</b> Cho phép truy cập private field
                        // <b>[EN]</b> Allow access to private fields
                        field.setAccessible(true);

                        Object value = field.get(obj);
                        fieldValues.put(field.getName(), value);

                    } catch (IllegalAccessException e) {
                        fieldValues.put(field.getName(), "[ACCESS DENIED]");
                    } catch (Exception e) {
                        fieldValues.put(field.getName(), "[ERROR: " + e.getMessage() + "]");
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        }

        public Map<String, Object> getFields() {
            return fieldValues;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            fieldValues.forEach((key, value) -> {
                sb.append("  ").append(key).append(": ");
                if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else {
                    sb.append(value);
                }
                sb.append("\n");
            });
            sb.append("}");
            return sb.toString();
        }
    }
}