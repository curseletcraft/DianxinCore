package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

class ResultedActionImpl<T> implements ResultedAction<T> {

    // Future bây giờ ôm trọn ActionResult, không lo bị ngắt quãng bởi Exception
    private final CompletableFuture<ActionResult<T>> future;

    ResultedActionImpl(CompletableFuture<ActionResult<T>> future) {
        // Fallback an toàn: lỡ Future gốc bị lỗi do thread pool từ chối (RejectedExecutionException)
        this.future = future.exceptionally(ActionResult::failure);
    }

    @Override
    public void queue() {
        future.thenAccept(result -> {}); // Thực thi mà không làm gì với kết quả trả về
    }

    @Override
    public void queue(@NotNull Consumer<T> success, @NotNull Consumer<Throwable> failure) {
        future.thenAccept(result -> {
            if (result.isSuccess()) {
                // Nhánh thành công: Trả thẳng value ra ngoài
                success.accept(result.getValue());
            } else {
                // Nhánh thất bại: Lấy exception. Nếu là do bị hủy (Cancel), tự tạo Exception báo hủy.
                Throwable ex = result.getException();
                if (ex == null && result.isCancelled()) {
                    ex = new CancellationException("Tác vụ đã bị hủy chủ động.");
                }
                failure.accept(ex);
            }
        });
    }

    @Override
    public void queue(@NotNull Consumer<ActionResult<T>> callback) {
        future.thenAccept(callback);
    }

    @Override
    public @NotNull ResultedAction<T> onSuccess(@NotNull Consumer<T> successCallback) {
        CompletableFuture<ActionResult<T>> successHandledFuture = future.thenApply(result -> {
            // Chỉ kích hoạt callback nếu tác vụ thực sự thành công
            if (result.isSuccess()) {
                try {
                    successCallback.accept(result.getValue());
                } catch (Exception ignored) {
                    // Nuốt lỗi an toàn: Giả sử code trong onSuccess bị lỗi (ví dụ lỗi ghi file log),
                    // nó sẽ không làm hỏng dữ liệu đang truyền xuống hàm queue() ở cuối chuỗi.
                }
            }
            // Trả lại y nguyên result (dù thành công hay thất bại) để đi tiếp con đường của nó
            return result;
        });

        return new ResultedActionImpl<>(successHandledFuture);
    }

    @Override
    public @NotNull ResultedAction<T> onError(@NotNull Consumer<Throwable> failureCallback) {
        CompletableFuture<ActionResult<T>> errorHandledFuture = future.thenApply(result -> {
            // Nếu có lỗi hoặc bị hủy, kích hoạt callback
            if (!result.isSuccess()) {
                Throwable ex = result.getException();
                if (ex == null && result.isCancelled()) {
                    ex = new CancellationException("Tác vụ đã bị hủy chủ động.");
                }

                try {
                    failureCallback.accept(ex);
                } catch (Exception ignored) {
                    // Nuốt lỗi nếu code trong onError tự sinh ra lỗi, để không làm sập chuỗi chính
                }
            }
            // Vẫn phải đẩy result gốc đi tiếp cho các hàm queue() hoặc flatMap() phía sau
            return result;
        });

        return new ResultedActionImpl<>(errorHandledFuture);
    }

    @Override
    public @NotNull ActionResult<T> complete() {
        return future.join();
    }

    @Override
    public @NotNull ActionResult<T> complete(long timeout, @NotNull TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            return ActionResult.failure(e);
        }
    }

    @Override
    public @NotNull CompletableFuture<ActionResult<T>> submit() {
        return future.thenApply(Function.identity());
    }

    @Override
    public <U> @NotNull ResultedAction<U> map(@NotNull Function<T, U> mapper) {
        CompletableFuture<ActionResult<U>> mappedFuture = future.thenApply(result -> {
            // Chỉ chạy mapper nếu kết quả trước đó thành công
            if (result.isSuccess()) {
                try {
                    return ActionResult.success(mapper.apply(result.getValue()));
                } catch (Throwable t) {
                    return ActionResult.failure(t); // Bắt lỗi xảy ra trong lúc map
                }
            }
            // Nếu trước đó đã lỗi, đẩy tiếp lỗi đó xuống dưới
            return ActionResult.failure(result.getException());
        });
        return new ResultedActionImpl<>(mappedFuture);
    }

    @Override
    public <U> @NotNull ResultedAction<U> flatMap(@NotNull Function<T, ResultedAction<U>> mapper) {
        CompletableFuture<ActionResult<U>> flatMappedFuture = future.thenCompose(result -> {
            if (result.isSuccess()) {
                try {
                    return mapper.apply(result.getValue()).submit();
                } catch (Throwable t) {
                    return CompletableFuture.completedFuture(ActionResult.failure(t));
                }
            }
            return CompletableFuture.completedFuture(ActionResult.failure(result.getException()));
        });
        return new ResultedActionImpl<>(flatMappedFuture);
    }

    @Override
    public @NotNull ResultedAction<T> onExecutor(@NotNull Executor executor) {
        return new ResultedActionImpl<>(future.thenApplyAsync(Function.identity(), executor));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }
}