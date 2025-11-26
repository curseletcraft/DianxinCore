package com.dianxin.core.api.handler.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConsoleCommandManager {

    private final Map<String, AbstractConsoleCommand> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger("ConsoleCommandManager");

    /** Đăng ký command */
    public void register(AbstractConsoleCommand cmd) {
        commands.put(cmd.getCommandLine().toLowerCase(), cmd);
        logger.info("Đã đăng ký console command: {}", cmd.getCommandLine());
    }

    /** Bắt đầu đọc console */
    public void startListening(Object bot) {
        Scanner sc = new Scanner(System.in);

        Thread t = new Thread(() -> {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] split = line.split(" ");
                String name = split[0].toLowerCase();
                String[] args = Arrays.copyOfRange(split, 1, split.length);

                AbstractConsoleCommand cmd = commands.get(name);

                if (cmd != null) {
                    try {
                        cmd.execute(args);
                    } catch (Exception e) {
                        logger.error("Lỗi khi xử lý lệnh '{}': {}", name, e.getMessage());
                    }
                } else {
                    logger.warn("Không tìm thấy lệnh '{}'", name);
                }
            }
        }, "ConsoleCommandListener");

        t.setDaemon(true);
        t.start();
        logger.info("ConsoleCommandListener đã khởi động.");
    }
}
