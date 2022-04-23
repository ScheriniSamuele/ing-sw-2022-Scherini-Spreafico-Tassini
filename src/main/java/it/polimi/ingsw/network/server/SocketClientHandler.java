package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClientHandler implements ClientHandler, Runnable{

    private final Socket client;
    private final SocketServer socketServer;

    private boolean connected;

    private final Object inputLock;
    private final Object outputLock;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketClientHandler(SocketServer socketServer, Socket client) {
        this.socketServer = socketServer;
        this.client = client;
        this.connected = true;
        this.inputLock = new Object();
        this.outputLock = new Object();
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.out.println("Error from socketClientHandler");
        }
    }


    @Override
    public void run() {
        try {
            handleClientConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        The method reads messages from the associated client after deserializing
        the message, if it is a login request it adds the client to our client list
        else it just sends the message to the socketServer -> Server -> GameController
        FIXME: During testing it launches an Exception despite working correctly
     */
    public void handleClientConnection() throws IOException {
        System.out.println("Client connected from "+client.getInetAddress());
        try{
            while(!Thread.currentThread().isInterrupted()) {
                synchronized (inputLock) {
                    Message message = (Message) in.readObject();
                    System.out.println("Message:"+message);
                    if (message != null && message.getMessageType() == MessageType.LOGIN_REQUEST) {
                        socketServer.addClient(message.getNickname(), this);
                    } else {
                        socketServer.getMessage(message);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        client.close();
    }

    /*
        boolean, return true if the client is connected.
     */
    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void disconnect() {
        if (connected) {
            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            connected = false;
            Thread.currentThread().interrupt();
            socketServer.onDisconnect(this);
        }
    }

    @Override
    public void sendMessage(Message message) {
        try {
            synchronized (outputLock) {
                out.writeObject(message);
                out.reset();
                System.out.println("Sent: "+message.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }


}
