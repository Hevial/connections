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

    /**
     * Starts the background reader thread if not already running. The reader
     * continuously reads lines from System.in and enqueues them for consumption
     * by the application.
     */
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

    /**
     * Polls for a completed input line from the background reader.
     *
     * @param timeoutMillis maximum time to wait in milliseconds
     * @return a trimmed input line or {@code null} if the timeout expired or
     *         the thread was interrupted
     */
    public static String pollLine(long timeoutMillis) {
        try {
            return QUEUE.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Clears any queued lines that have not yet been consumed.
     */
    public static void clearQueue() {
        QUEUE.clear();
    }

    public static void stop() {
        running = false;
    }

}
