package client;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Background console reader that pushes completed lines into a queue.
 *
 * <p>This utility starts a single daemon thread which reads lines from
 * {@code System.in} and enqueues them into an internal {@link BlockingQueue}.
 * Other components may poll the queue using {@link #pollLine(long)} to obtain
 * user input without blocking the main UI thread. The class provides helpers
 * to start/stop the background thread and to clear any queued lines.</p>
 *
 * <p>Designed for simple CLI applications where input should be consumable
 * asynchronously (for example to allow interleaved notifications).</p>
 */
public final class InputReader {

    /**
     * Internal queue holding completed lines read from the console. Consumers
     * should use {@link #pollLine(long)} to retrieve entries.
     */
    private static final BlockingQueue<String> QUEUE = new LinkedBlockingQueue<>();

    /**
     * Indicates whether the background reader thread should keep running.
     */
    private static volatile boolean running = false;

    /**
     * Private constructor to prevent instantiation.
     */
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
     * @return a trimmed input line, or {@code null} if the timeout expired or
     *         the waiting thread was interrupted
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

    /**
     * Stops the background reader thread. This method signals the reader to
     * exit; the underlying thread will terminate once it next checks the
     * {@code running} flag. After calling {@code stop()} callers may optionally
     * call {@link #clearQueue()} to drop any unprocessed input.
     */
    public static void stop() {
        running = false;
    }

}
