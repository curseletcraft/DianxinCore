package com.dianxin.core.fastutil.utils;

import com.dianxin.core.fastutil.exceptions.UtilityClassInitializationException;

import java.awt.Color;
import java.util.Random;

@SuppressWarnings("unused")
public final class ColorUtil {
    private static final Random RANDOM = new Random();

    private ColorUtil() {
        throw new UtilityClassInitializationException(ColorUtil.class);
    }

    /**
     * Tạo một màu ngẫu nhiên hoàn toàn.
     * @return Đối tượng Color ngẫu nhiên.
     */
    public static Color random() {
        return new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    /**
     * Tạo một chuỗi Hex ngẫu nhiên (VD: "#FFAABB").
     * Lưu ý: Mình đổi return type thành String vì tên hàm là randomHex,
     * nếu bạn muốn trả về Color thì dùng hàm random() ở trên nhé.
     * @return Chuỗi Hex color.
     */
    public static String randomHexString() {
        Color color = random();
        return toHexString(color);
    }

    /**
     * Tạo một màu ngẫu nhiên nằm trong khoảng giữa 2 mã Hex.
     * Rất hữu ích để tạo màu gradient hoặc random theo tông màu.
     * * @param minHex Mã hex bắt đầu (VD: "#000000")
     * @param maxHex Mã hex kết thúc (VD: "#101010")
     * @return Color nằm trong khoảng màu đó.
     */
    public static Color randomColorRescricted(String minHex, String maxHex) {
        // 1. Chuẩn hóa input (bỏ dấu # nếu có)
        if (minHex.startsWith("#")) minHex = minHex.substring(1);
        if (maxHex.startsWith("#")) maxHex = maxHex.substring(1);

        // 2. Validate độ dài
        if (minHex.length() != 6 || maxHex.length() != 6) {
            throw new IllegalArgumentException("Mã Hex phải bao gồm 6 ký tự (VD: FF0000)");
        }

        try {
            // 3. Tách các thành phần R, G, B từ chuỗi Hex
            int minR = Integer.parseInt(minHex.substring(0, 2), 16);
            int minG = Integer.parseInt(minHex.substring(2, 4), 16);
            int minB = Integer.parseInt(minHex.substring(4, 6), 16);

            int maxR = Integer.parseInt(maxHex.substring(0, 2), 16);
            int maxG = Integer.parseInt(maxHex.substring(2, 4), 16);
            int maxB = Integer.parseInt(maxHex.substring(4, 6), 16);

            // 4. Random từng thành phần trong khoảng min-max
            // Sử dụng Math.min/max để đảm bảo không bị lỗi nếu người dùng truyền min > max
            int r = randomInRange(Math.min(minR, maxR), Math.max(minR, maxR));
            int g = randomInRange(Math.min(minG, maxG), Math.max(minG, maxG));
            int b = randomInRange(Math.min(minB, maxB), Math.max(minB, maxB));

            return new Color(r, g, b);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã Hex chứa ký tự không hợp lệ", e);
        }
    }

    /**
     * Tiện ích: Chuyển đổi Color object sang chuỗi Hex (#RRGGBB).
     */
    public static String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()).toUpperCase();
    }

    // --- Helper private ---
    private static int randomInRange(int min, int max) {
        if (min == max) return min;
        return RANDOM.nextInt((max - min) + 1) + min;
    }
}