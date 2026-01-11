package com.dianxin.core.jda.scheduler;

import java.util.Map;
import java.util.concurrent.*;

public class BotScheduler implements Scheduler {

    // Map lưu trữ các task đang chạy để quản lý cancel theo ID
    private final Map<Integer, Task> activeTasks = new ConcurrentHashMap<>();

    // Pool 1: Quản lý thời gian và task nhẹ (tương đương Main Thread ảo)
    private final ScheduledExecutorService schedulerPool;

    // Pool 2: Quản lý task nặng, IO (tương đương Async Thread)
    private final ExecutorService asyncPool;

    public BotScheduler() {
        // Core pool size = số nhân CPU (để tối ưu)
        int cores = Runtime.getRuntime().availableProcessors();
        this.schedulerPool = Executors.newScheduledThreadPool(cores);
        this.asyncPool = Executors.newCachedThreadPool();
    }

    @Override
    public Task runTask(Runnable runnable) {
        return schedule(runnable, 0, 0, TimeUnit.MILLISECONDS, false, false);
    }

    @Override
    public Task runTaskAsync(Runnable runnable) {
        return schedule(runnable, 0, 0, TimeUnit.MILLISECONDS, true, false);
    }

    @Override
    public Task runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
        return schedule(runnable, delay, 0, unit, false, false);
    }

    @Override
    public Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit) {
        return schedule(runnable, delay, 0, unit, true, false);
    }

    @Override
    public Task runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
        return schedule(runnable, delay, period, unit, false, true);
    }

    @Override
    public Task runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit) {
        return schedule(runnable, delay, period, unit, true, true);
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = activeTasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void shutdown() {
        activeTasks.values().forEach(Task::cancel);
        activeTasks.clear();
        schedulerPool.shutdown();
        asyncPool.shutdown();
    }

    // --- Core Logic ---

    private Task schedule(Runnable runnable, long delay, long period, TimeUnit unit, boolean isAsync, boolean isRepeated) {
        Task task = new Task(!isAsync);
        activeTasks.put(task.getTaskId(), task);

        // Wrapper để tự động xóa task khỏi map khi chạy xong (nếu không lặp)
        Runnable wrapper = () -> {
            try {
                if (isAsync) {
                    // Nếu là async delay/timer, ta đẩy việc thực thi sang asyncPool
                    asyncPool.submit(() -> {
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runnable.run();
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log lỗi để không chết luồng
            } finally {
                // Nếu không phải timer lặp lại, chạy xong thì xóa khỏi danh sách quản lý
                if (!isRepeated) {
                    activeTasks.remove(task.getTaskId());
                }
            }
        };

        Future<?> future;
        if (isRepeated) {
            // Timer
            future = schedulerPool.scheduleAtFixedRate(wrapper, delay, period, unit);
        } else if (delay > 0) {
            // Later
            future = schedulerPool.schedule(wrapper, delay, unit);
        } else {
            // Run now
            if (isAsync) {
                future = asyncPool.submit(wrapper);
            } else {
                future = schedulerPool.submit(wrapper);
            }
        }

        task.setFuture(future);
        return task;
    }
}