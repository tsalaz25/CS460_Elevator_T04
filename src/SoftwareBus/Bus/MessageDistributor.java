package SoftwareBus.Bus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class MessageDistributor {

    /**
     * For Server BUS instance, a list of all the clients' ObjectOutputStreams
     */
    private BlockingDeque<ObjectOutputStream> clients = new LinkedBlockingDeque<>();

    /**
     * As a client, the socket to talk to the BUS server instance
     */
    private Socket socket;
    /**
     * As a client, the ObjectInputStream to receive messages from the server BUS instance
     */
    private ObjectInputStream in;
    /**
     * As a client, the ObjectOutputStream to send messages to the server BUS instance
     */
    private ObjectOutputStream out;

    public MessageDistributor(int port) { //TODO maybe delete prot
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (true) {
                    try {
                        Socket s = serverSocket.accept();
                        clients.add(new ObjectOutputStream(s.getOutputStream()));
                        run(new ObjectInputStream(s.getInputStream()));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
            socket = new Socket("localhost", port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (BindException e) {
            try {
                socket = new Socket("localhost", port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendToAll(Message m) {
        for (ObjectOutputStream client : clients) {
            synchronized (client) {
                try {
                    client.writeObject(m);
                    client.flush();
                    client.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void run(ObjectInputStream in) {
        new Thread(() -> {
            while (true) {
                try {
                    Object obj = in.readObject();
                    if (obj == null) {
                        sendToAll(null);
                    } else {
                        sendToAll((Message) obj);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();
    }

    public Message nextMessage() {
        try {
            Object obj = in.readObject();
            return (obj == null) ? null : (Message) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Message m) {
        try {
            out.writeObject(m);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
