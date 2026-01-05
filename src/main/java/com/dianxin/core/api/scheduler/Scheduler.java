package com.dianxin.core.api.scheduler;

import java.util.concurrent.TimeUnit;

/**
 * Example usage:
 * <pre><code>
*     public class ExampleUsage {
*
*     private final Scheduler scheduler = new BotScheduler();
*
*     public void demo() {
*         // 1. Chạy ngay lập tức (Sync - nhẹ)
*         scheduler.runTask(() -> {
*             System.out.println("Chạy ngay!");
*         });
*
*         // 2. Chạy Async (IO nặng - database, tải file)
*         scheduler.runTaskAsync(() -> {
*             System.out.println("Đang tải dữ liệu nặng...");
*             // Thread.sleep(5000)...
*         });
*
*         // 3. Chạy sau 5 giây (Later)
*         scheduler.runTaskLater(() -> {
*             System.out.println("Đã qua 5 giây!");
*         }, 5, TimeUnit.SECONDS);
*
*         // 4. Chạy lặp lại mỗi 1 giây (Timer) - Giống BukkitRunnable
*         Task timerTask = scheduler.runTaskTimer(() -> {
*             System.out.println("Đang đếm giờ...");
*         }, 0, 1, TimeUnit.SECONDS);
*
*         // Hủy task sau 10 giây
*         scheduler.runTaskLater(() -> {
*             System.out.println("Hủy timer!");
*             timerTask.cancel(); // Hoặc scheduler.cancelTask(timerTask.getTaskId());
*         }, 10, TimeUnit.SECONDS);
*     }
* }
 * </code></pre>
 */
public interface Scheduler {

    /**
     * Chạy một task ngay lập tức trên luồng xử lý chính (Pool).
     */
    Task runTask(Runnable runnable);

    /**
     * Chạy một task bất đồng bộ trên luồng riêng biệt (dành cho IO nặng).
     */
    Task runTaskAsync(Runnable runnable);

    /**
     * Chạy task sau một khoảng thời gian (Delay).
     * @param delay Thời gian chờ (tính bằng mili giây - tick = 50ms)
     */
    Task runTaskLater(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Chạy task bất đồng bộ sau một khoảng thời gian.
     */
    Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Chạy task lặp đi lặp lại (Timer).
     * @param delay Thời gian chờ trước khi bắt đầu
     * @param period Chu kỳ lặp lại
     */
    Task runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Chạy task lặp lại bất đồng bộ.
     */
    Task runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Hủy một task dựa trên ID.
     */
    void cancelTask(int taskId);

    /**
     * Hủy tất cả task đang chạy (Dùng khi shutdown bot).
     */
    void shutdown();
}