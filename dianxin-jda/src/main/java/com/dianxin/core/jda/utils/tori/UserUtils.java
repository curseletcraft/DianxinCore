package com.dianxin.core.jda.utils.tori;

import com.dianxin.core.jda.JavaDiscordBot;
import com.dianxin.core.jda.utils.services.ToriServices;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Utility class cho {@link User}.
 * <p>
 * Class này tập trung vào các thao tác đọc thông tin, format dữ liệu
 * và cung cấp các giá trị an toàn để hiển thị.
 */
public final class UserUtils {
    @NotNull private static final JDA jda = ToriServices.getJda();
    @NotNull private final JavaDiscordBot bot = ToriServices.getBaseBot();

    private UserUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Mention người dùng (vd: {@code <@123456789>}).
     */
    @NotNull
    public static String mention(@NotNull User user) {
        return user.getAsMention();
    }

    /**
     * Lấy ID của người dùng.
     */
    @NotNull
    public static String id(@NotNull User user) {
        return user.getId();
    }

    /**
     * Lấy username (name gốc, không discriminator).
     */
    @NotNull
    public static String username(@NotNull User user) {
        return user.getName();
    }

    /**
     * Lấy global name của người dùng (có thể null).
     *
     * @return global name hoặc {@code null} nếu người dùng chưa đặt
     */
    @Nullable
    public static String globalName(@NotNull User user) {
        return user.getGlobalName();
    }

    /**
     * Lấy tên hiển thị an toàn để show cho người dùng.
     * <p>
     * Ưu tiên:
     * <ol>
     *   <li>Global name (nếu có)</li>
     *   <li>Username</li>
     * </ol>
     *
     * @return tên hiển thị, không bao giờ null
     */
    @NotNull
    public static String displayName(@NotNull User user) { // relative User#getEffectiveName
        String global = user.getGlobalName();
        return global != null ? global : user.getName();
    }

    /**
     * Kiểm tra người dùng có global name hay không.
     */
    public static boolean hasGlobalName(@NotNull User user) {
        return user.getGlobalName() != null;
    }

    /**
     * @deprecated Discord đã bỏ discriminator. Dùng {@link #username(User)}.
     */
    @NotNull
    @Deprecated
    public static String tag(@NotNull User user) {
        return user.getAsTag();
    }

    /**
     * Lấy tag đầy đủ của người dùng (vd: username hoặc global name + ID).
     * <p>
     * Hữu ích cho log/debug.
     */
    @NotNull
    public static String debugTag(@NotNull User user) {
        return displayName(user) + " (" + user.getId() + ")";
    }

    @Nullable
    public static String avatarLink(@NotNull User user) {
        return user.getAvatarUrl();
    }

    @Nullable
    public static String avatarId(@NotNull User user) {
        return user.getAvatarId();
    }

    @NotNull
    public static String effectiveAvatarLink(@NotNull User user) {
        return user.getEffectiveAvatarUrl();
    }

    @NotNull
    public static String defaultAvatarLink(@NotNull User user) {
        return user.getDefaultAvatarUrl();
    }

    @NotNull
    public static String defaultAvatarId(@NotNull User user) {
        return user.getDefaultAvatarId();
    }

    public static boolean hasPrivateChannel(@NotNull User user) {
        return user.hasPrivateChannel();
    }

    @NotNull
    public static RestAction<@Nullable String> bannerLink(@NotNull User user) {
        return user.retrieveProfile().map(User.Profile::getBannerUrl);
    }

    @NotNull
    public static RestAction<@Nullable String> bannerId(@NotNull User user) {
        return user.retrieveProfile().map(User.Profile::getBannerId);
    }

    /**
     * Retrieve {@link User} thông qua Discord REST API.
     *
     * @param id user id
     * @return {@link RestAction} thành công với {@link User} (non-null),
     *         hoặc thất bại nếu user không tồn tại / không truy cập được
     */
    @NotNull
    public static RestAction<User> retrieveUser(@NotNull String id) {
        return jda.retrieveUserById(id);
    }

    /**
     * Lấy {@link User} từ cache.
     *
     * @param id user id
     * @return {@link User} nếu có trong cache, ngược lại null
     */
    @Nullable
    public static User getCachedUser(@NotNull String id) {
        return jda.getUserById(id);
    }

    /**
     * Thời điểm tài khoản Discord được tạo.
     *
     * <p>Dựa trên Snowflake ID, không cần REST call.</p>
     */
    @NotNull
    public static OffsetDateTime createdTime(@NotNull User user) {
        return user.getTimeCreated();
    }
}
