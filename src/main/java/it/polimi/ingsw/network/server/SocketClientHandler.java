package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.TryAgainException;
import it.polimi.ingsw.network.client.SocketClient;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageType;
import it.polimi.ingsw.view.VirtualView;
import it.polimi.ingsw.network.client.Client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClientHandler implementation that handles the communication between a Client and the Server.
 */

public class SocketClientHandler implements ClientHandler, Runnable{

    private final Socket client;
    private final SocketServer socketServer;
    private boolean connected;
    private final Object inputLock;
    private final Object outputLock;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private VirtualView virtualView;

    /**
     * SocketClientHandler constructor.
     *
     * @param socketServer the socket of the {@link Server}.
     * @param client the socket of the {@link Client}.
     */

    public SocketClientHandler(SocketServer socketServer, Socket client) {
        this.socketServer = socketServer;
        this.client = client;
        this.connected = true;
        this.inputLock = new Object();
        this.outputLock = new Object();
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException ex) {
            Server.LOGGER.severe("Error from socketClientHandler"+ ex.getClass().getSimpleName()
                    + ": " + ex.getMessage());
        }
    }

    /**
     * Sets the {@link VirtualView}.
     *
     * @param virtualView the {@link VirtualView} that has to be set.
     */

    public void setVirtualView(VirtualView virtualView) {
        this.virtualView = virtualView;
    }

    /**
     * Returns the {@link VirtualView}.
     *
     * @return the actual {@link VirtualView}.
     */

    public VirtualView getVirtualView() {
        return virtualView;
    }

    /**
     * Returns the {@link SocketClient}.
     *
     * @return the actual {@link SocketClient}.
     */

    @Override
    public Socket getSocketClient() { return client; }

    /**
     * Starts the {@link SocketClientHandler} to handle the connection with the {@link SocketClient}.
     */

    @Override
    public void run() {
        try {
            handleClientConnection();
        } catch (IOException ex) {
            Server.LOGGER.severe("Client " + client.getInetAddress() + " connection dropped. \n" +
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
            disconnect();
        }
    }

    /**
     * Reads messages from the associated client after deserializing the message; if it is a login request, it adds
     * the {@link SocketClient} to the client list, otherwise it just sends the message to the {@link SocketServer}
     * -> {@link Server} -> {@link GameController}.
     *
     * @throws IOException if there are IOErrors.
     */

    public void handleClientConnection() throws IOException {
        Server.LOGGER.info("Client connected from " + client.getInetAddress());
        try{
            while(!Thread.currentThread().isInterrupted() && client.getInetAddress().isReachable(10000)) {
                synchronized (inputLock) {
                    if (in != null && connected) {
                        Message message;
                        message = (Message) in.readObject();
                        Server.LOGGER.info("Message: " + message.getClass().getSimpleName());
                        if (message.getMessageType() == MessageType.LOGIN_REQUEST) {
                            try {
                                socketServer.addClient(message.getNickname(), this);
                                virtualView.showExistingGames(socketServer.getServer().getGameControllerMap());
                            } catch (TryAgainException e) {
                                Server.LOGGER.warning("Nickname has already been chosen.");
                                virtualView.showGenericMessage("Nickname has already been chosen.");
                                virtualView.askNickname();
                            }
                        } else {
                            Server.LOGGER.info("Received: " + message.getClass().getSimpleName());
                            socketServer.getMessage(message);
                        }
                    }
                }
            }
        }
        catch(EOFException ignored){}
        catch(IOException | ClassNotFoundException ex){
            Server.LOGGER.severe("Invalid stream from client. \n" +
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Returns a flag that indicates if the {@link SocketClient} is still connected.
     *
     * @return {@code true} if the client is still connected, {@code false} otherwise.
     */

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * {@link SocketClientHandler} disconnection method.
     */

    @Override
    public void disconnect() {
        if (connected) {
            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException ex) {
                Server.LOGGER.severe(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
            connected = false;
            Thread.currentThread().interrupt();
            socketServer.onDisconnect(this);
        }
    }

    /**
     * Sends a message to the associated {@link SocketClient}.
     *
     * @param message the message to be sent.
     */

    @Override
    public void sendMessage(Message message) {
        try {
            synchronized (outputLock) {
                out.writeObject(message);
                out.reset();
                Server.LOGGER.info("Sent: "+message.getClass().getSimpleName());
            }
        } catch (IOException ex) {
            Server.LOGGER.severe(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            disconnect();
        }
    }

    /**
     * Sends a message to the associated {@link SocketClient}, and quits the ended match.
     *
     * @param message the message to be sent.
     */

    @Override
    public void sendMessageAndQuit(Message message) {
        try {
            synchronized (outputLock) {
                out.writeObject(message);
                out.reset();
                Server.LOGGER.info("Sent: "+message.getClass().getSimpleName());
                socketServer.onQuit(this);
            }
        } catch (IOException ex) {
            Server.LOGGER.severe(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            disconnect();
        }
    }
}
