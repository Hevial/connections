package server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

import models.Request;
import models.Response;
import server.db.DBManager;
import server.handlers.RequestHandler;

/**
 * Worker responsible for handling a single client TCP connection.
 *
 * <p>The worker reads JSON requests from the socket, dispatches them to the
 * provided {@link RequestHandler}, and writes JSON responses back to the
 * client. It also maintains a per-connection {@link Session} instance that is
 * used to track authentication state for the connected user.</p>
 */
public class ServerWorker implements Runnable {
    private final RequestHandler requestHandler;
    private final SocketChannel clientSocket;
    private Session session;

    public ServerWorker(RequestHandler requestHandler, SocketChannel clientSocket) {
        this.requestHandler = requestHandler;
        this.clientSocket = clientSocket;
        this.session = new Session(null, null); // Initialize session with no user
    }

    @Override
    public void run() {
        String clientIp = null;
        try {
            clientIp = clientSocket.getRemoteAddress().toString();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (clientSocket.read(buffer) != -1) {

                // flip the buffer to start from the beginning
                buffer.flip();

                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String requestStr = new String(bytes, StandardCharsets.UTF_8);

                // DEBUG: print the raw request string
                System.out.println("Recived req: " + requestStr + " from " + clientIp);

                // Clear the buffer for the next read
                buffer.clear();

                // Parsing JSON
                Request req = new Gson().fromJson(requestStr, Request.class);
                if (req == null) {
                    System.err.println("Error: Received null request from client: " + clientIp);
                    continue;
                }

                // Handle the request and get response
                Response jsonResponse = requestHandler.handleRequest(req, session);
                jsonResponse.setSessionUsername(session.getUsername());

                // Send response back to client
                String responseStr = new Gson().toJson(jsonResponse);
                ByteBuffer respBuffer = ByteBuffer.wrap(responseStr.getBytes(StandardCharsets.UTF_8));
                clientSocket.write(respBuffer);

            }

        } catch (java.net.SocketException se) {
            System.out.println("Connessione resettata dal client: " + clientIp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // If the session has a logged-in user, log them out
            if (session.isAuthenticated()) {
                DBManager.getInstance().logoutUser(session.getUserId());
                // ensure notification registry cleaned up
                NotificationRegistry.unregister(session.getUserId());

                try {
                    if (clientSocket != null && clientSocket.isOpen()) {
                        clientSocket.close();
                    }
                    if (clientIp != null) {
                        System.out.println("Client disconnected: " + clientIp);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
