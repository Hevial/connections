package client;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Background console reader that pushes completed lines into a queue.
 * Provides non-blocking poll and a method to clear queued lines.
 */
public final class InputReader {

    private static final BlockingQueue<String> QUEUE = new LinkedBlockingQueue<>();
    private static volatile boolean running = false;

    private InputReader() {
    }

    public static void start() {
        if (running)
            return;
        running = true;
        Thread t = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running) {
                    String line = null;
                    try {
                        if (!scanner.hasNextLine()) {
                            Thread.sleep(50);
                            continue;
                        }
                        line = scanner.nextLine();
                    } catch (Throwable ignored) {
                    }
                    if (line != null) {
                        QUEUE.offer(line);
                    }
                }
            } catch (Throwable ignored) {
            }
        }, "input-reader");
        t.setDaemon(true);
        t.start();
    }

    public static String pollLine(long timeoutMillis) {
        try {
            return QUEUE.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static void clearQueue() {
        QUEUE.clear();
    }

    public static void stop() {
        running = false;
    }
}
