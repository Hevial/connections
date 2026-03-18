package server;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Maintains a thread-safe registry that maps user identifiers to their
 * notification socket addresses.
 *
 * The registry is implemented with a
 * {@link java.util.concurrent.ConcurrentHashMap}
 * and is safe for concurrent access from multiple threads. Entries can be
 * added via {@link #register(String, java.net.InetSocketAddress)} and removed
 * via {@link #unregister(String)}. The {@link #getAllAddresses()} method
 * returns a live collection view of the current addresses.
 */
public class NotificationRegistry {

    /**
     * Internal mapping of userId -> notification socket address.
     *
     * Uses {@link java.util.concurrent.ConcurrentHashMap} to allow safe
     * concurrent updates and reads.
     */
    private static final ConcurrentHashMap<String, InetSocketAddress> registry = new ConcurrentHashMap<>();

    private static final Logger LOG = Logger.getLogger(NotificationRegistry.class.getName());

    /**
     * Registers or updates the notification socket address for the given
     * user identifier.
     *
     * If either {@code userId} or {@code addr} is {@code null} the method
     * does nothing. This method overwrites any existing mapping for the
     * same {@code userId}.
     *
     * @param userId the identifier of the user to register; may not be {@code null}
     * @param addr   the {@link InetSocketAddress} to which notifications for the
     *               given user should be sent; may not be {@code null}
     */
    public static void register(String userId, InetSocketAddress addr) {
        if (userId == null || addr == null)
            return;
        registry.put(userId, addr);
        LOG.info("registered " + userId + " -> " + addr);
    }

    /**
     * Unregisters the given user identifier, removing any associated
     * notification address.
     *
     * If {@code userId} is {@code null} this method does nothing.
     *
     * @param userId the identifier of the user to remove; may be {@code null}
     */
    public static void unregister(String userId) {
        if (userId == null)
            return;
        if (registry.remove(userId) != null)
            LOG.info("unregistered " + userId);

    }

    /**
     * Returns a collection view of all registered notification addresses.
     *
     * The returned collection is backed by the underlying map's values view
     * and may reflect concurrent modifications to the registry. Iteration
     * over the collection is safe for concurrent use, but callers should be
     * aware that the contents may change if other threads register or
     * unregister addresses.
     *
     * @return a {@link Collection} of currently registered
     *         {@link InetSocketAddress}
     */
    public static Collection<InetSocketAddress> getAllAddresses() {
        return registry.values();
    }

}
