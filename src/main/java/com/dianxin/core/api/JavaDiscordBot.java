package com.dianxin.core.api;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.dianxin.core.api.annotations.core.UsingLikeBukkitLogback;
import com.dianxin.core.api.annotations.lifecycle.RegisterToriService;
import com.dianxin.core.api.exceptions.ServiceUnavailableException;
import com.dianxin.core.api.handler.console.ConsoleCommandManager;
import com.dianxin.core.api.meta.BotMeta;
import com.dianxin.core.api.utils.services.ToriServices;
import com.dianxin.core.api.utils.lifecycle.VersionController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

/**
 * Represents a base class for creating Discord bots using JDA with an extended
 * lifecycle system, console command support, and customizable startup options.
 * <p>
 * Users of this API should extend this class and override the desired methods such as:
 * <ul>
 *     <li>{@link #onEnable()} - Called after the bot finishes starting.</li>
 *     <li>{@link #onDisable()} - Called before the bot shuts down.</li>
 *     <li>{@link #registerConsoleCommands()} - Register custom console commands.</li>
 *     <li>{@link #getIntents()} - Provide required {@link GatewayIntent} values.</li>
 *     <li>{@link #getActivity()} - Define the bot presence/activity.</li>
 * </ul>
 *
 * <p>
 * The class also automatically manages:
 * <ul>
 *     <li>JDA initialization and shutdown</li>
 *     <li>Console command listener</li>
 *     <li>Presence and intents setup</li>
 *     <li>Basic logging flow</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * public class MyBot extends JavaDiscordBot {
 *     public MyBot() {
 *         super("YOUR_TOKEN", "MyBot");
 *     }
 *
 *     @Override
 *     public void onEnable() {
 *         getLogger().info("MyBot is ready!");
 *     }
 *
 *     @Override
 *     protected EnumSet<GatewayIntent> getIntents() {
 *         return IntentContext.getDefaultIntents();
 *     }
 *
 *     @Override
 *     protected Activity getActivity() {
 *         return Activity.playing("Hello world!");
 *     }
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public abstract class JavaDiscordBot {

    /**
     * The JDA instance representing the bot connection.
     */
    private JDA jda;

    /**
     * Display name of the bot, used for logs.
     */
    private final @NotNull String botName;

    /**
     * Meta of bot
     */
    private final @NotNull BotMeta meta;

    /**
     * Logger instance for this bot class.
     */
    private final Logger logger;

    /**
     * Manager responsible for handling console commands.
     */
    private final ConsoleCommandManager consoleManager = new ConsoleCommandManager();

    /**
     * Bot token used to authenticate with Discord.
     */
    private final String botToken;

    private volatile boolean started = false;

    /**
     * Creates a new Discord bot instance.
     *
     * @param token   The bot token from Discord Developer Portal.
     * @param meta    Meta bot.
     */
    public JavaDiscordBot(String token, @NotNull BotMeta meta) {
        trySetupBukkitLikeLogback();
        this.botToken = token;

        this.meta = meta;
        this.botName = meta.getBotName();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Starts the bot, initializes JDA, loads intents/activity,
     * registers console commands, and begins console listening.
     *
     * <p>This method blocks until the JDA session is ready.
     *
     * @throws InterruptedException If the current thread is interrupted.
     */
    public synchronized void start() throws InterruptedException {
        if (started) {
            throw new IllegalStateException("Bot đã được start rồi!");
        }

        try {
            VersionController.checkCompatibilityOrThrow();
        } catch (ServiceUnavailableException e) {
            LoggerFactory.getLogger(getClass()).error("❌ {}", e.getMessage(), e);
            System.exit(-1);
            return;
        }

        started = true;

        JDABuilder jdaBuilder;
        EnumSet<GatewayIntent> intents = getIntents();
        Activity activity = getActivity();

        if(intents == null) {
            logger.warn("⚠️ Bot {} không có GatewayIntents — có thể sẽ không nhận được event nào.", botName);
            jdaBuilder = JDABuilder.createDefault(botToken);
        } else {
            jdaBuilder = JDABuilder.createDefault(botToken, intents);
        }

        if(activity != null) {
            jdaBuilder.setActivity(activity);
        }

        this.jda = jdaBuilder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        if(this.getClass().isAnnotationPresent(RegisterToriService.class)) {
            ToriServices.initialize(this);
            logger.info(
                    "Đang sử dụng ToriService v{} (API version: {})",
                    ToriServices.getToriServiceVersion(),
                    ToriServices.getToriServiceApiVersion()
            );
        }

        logger.info("✅ Bot {} đã khởi động thành công.", botName);
        logger.info("Link mời bot: {}", jda.getInviteUrl());

        // Đăng ký lệnh console custom
        registerConsoleCommands();

        // Bắt đầu lắng nghe console
        consoleManager.startListening(this);

        onEnable();
    }

    /**
     * Called when the bot fully starts and is ready.
     * Override to initialize listeners, commands, database, etc.
     */
    public void onEnable() { }

    /**
     * Called before the bot shuts down.
     * Override to close resources or save data.
     */
    public void onDisable() { }

    /**
     * Gracefully shuts down the bot, updating its status to offline,
     * calling {@link #onDisable()}, and closing the JDA connection.
     */
    public void onShutdown() {
        onDisable();
        logger.info("⏹ Đang tắt bot {}...", botName);
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);

        /*
         * Shutdown các task khác ngoại trừ bot.onShutdown()
         * Xem {@link InternalServer#shutdown()}
         */
        // DianxinCore.getServer().getScheduler().shutdown();

        jda.shutdown();
    }

    /**
     * Provides the list of gateway intents required for the bot.
     *
     * @return An {@link EnumSet} of intents or {@code null} to use default JDA behavior.
     */
    protected EnumSet<GatewayIntent> getIntents() {
        return null;
    }

    /**
     * Defines the bot's presence/activity shown on Discord.
     *
     * @return A {@link Activity} instance or {@code null} for no activity.
     */
    protected Activity getActivity() {
        return null;
    }

    /**
     * Override this to register custom console commands using {@link ConsoleCommandManager}.
     */
    protected void registerConsoleCommands() {
        // Bot subclasses override
    }

    /**
     * @return The active JDA instance.
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * @return The friendly name of the bot.
     */
    public @NotNull String getBotName() {
        return botName;
    }

    /**
     * @return Meta of the bot
     */
    public @NotNull BotMeta getMeta() {
        return meta;
    }

    /**
     * @return Logger instance of the bot.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return The console command manager.
     */
    public ConsoleCommandManager getConsoleManager() {
        return consoleManager;
    }

    private void invokeLifecycleAnnotation(Class<? extends Annotation> ann) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(ann)) continue;

            if (method.getParameterCount() != 0) {
                throw new IllegalStateException(
                        "@" + ann.getSimpleName() + " method must have no params: " + method.getName());
            }

            try {
                method.setAccessible(true);
                method.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking @" + ann.getSimpleName(), e);
            }
        }
    }

    private void trySetupBukkitLikeLogback() {
        UsingLikeBukkitLogback ann = this.getClass().getAnnotation(UsingLikeBukkitLogback.class);
        if (ann == null) return;

        String basePackage = ann.basePackage();

        Path logbackFile = Paths.get("logback.xml");

        if (Files.notExists(logbackFile)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("logback-template.xml")) {
                if (in == null) {
                    throw new IllegalStateException("Không tìm thấy logback-template.xml trong API");
                }
                String xml = new String(
                        in.readAllBytes(),
                        StandardCharsets.UTF_8).replace("${BASE_PACKAGE}", basePackage);
                Files.writeString(logbackFile, xml);
            } catch (IOException e) {
                throw new RuntimeException("Không thể tạo logback.xml", e);
            }
        }
        reloadLogback(); // reload logback
    }

    private void reloadLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        try {
            configurator.doConfigure("logback.xml");
        } catch (JoranException e) {
            throw new RuntimeException("Load logback.xml thất bại", e);
        }
    }
}
