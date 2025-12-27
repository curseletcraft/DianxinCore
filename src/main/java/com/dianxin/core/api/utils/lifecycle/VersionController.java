package com.dianxin.core.api.utils.lifecycle;

import com.dianxin.core.api.exceptions.lifecycle.ServiceUnavailableException;
import net.dv8tion.jda.api.JDAInfo;

import java.util.Arrays;

public final class VersionController {
    private static final String REQUIRED_JDA_VERSION = "6.2.0"; // Version JDA tối thiểu DianxinCore hỗ trợ
    private static final int REQUIRED_JAVA_VERSION = 21;

    private VersionController() {}

    /**
     * Lấy version JDA mà developer đang sử dụng (runtime)
     */
    public static String getJdaVersionImplemented() {
        return JDAInfo.VERSION;
    }

    /**
     * Java feature version (21, 17, 11, ...)
     */
    public static int getJavaVersion() {
        return Runtime.version().feature();
    }

    /**
     * Kiểm tra JDA của developer có tương thích không
     *
     * @throws ServiceUnavailableException nếu version thấp hơn yêu cầu
     */
    public static void checkCompatibilityOrThrow() {
        int javaVersion = getJavaVersion();
        String jdaVersion = getJdaVersionImplemented();

        if (javaVersion < REQUIRED_JAVA_VERSION) {
            throw new ServiceUnavailableException(
                    "Version Java không tương thích! Yêu cầu >= " + REQUIRED_JAVA_VERSION +
                            ", nhưng đang dùng " + javaVersion
            );
        }
        
        if (!isCompatibleVersion(jdaVersion)) {
            throw new ServiceUnavailableException(
                    "JDA version không tương thích! Yêu cầu >= " + REQUIRED_JDA_VERSION +
                            ", nhưng đang dùng " + jdaVersion
            );
        }
    }

    /**
     * implemented >= required ?
     */
    static boolean isCompatibleVersion(String implemented) {
        int[] impl = parse(implemented);
        int[] req  = parse(REQUIRED_JDA_VERSION);

        for (int i = 0; i < Math.max(impl.length, req.length); i++) {
            int a = i < impl.length ? impl[i] : 0;
            int b = i < req.length ? req[i] : 0;

            if (a > b) return true;
            if (a < b) return false;
        }
        return true; // bằng nhau
    }

    private static int[] parse(String v) {
        return Arrays.stream(v.split("\\."))
                .map(s -> s.replaceAll("[^0-9]", "")) // đề phòng -alpha
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
