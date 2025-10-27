package SoftwareBus.Bus;

import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Bus {
    /**
     * For Server BUS instance, how many clients are connected
     */
    private static int numClients = 0;
    /**
     * For Server BUS instance, a list of all the clients' ObjectOutputStreams
     */
    private ArrayList<ObjectOutputStream> clients = new ArrayList<>();


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
    /**
     * The id number associated with the instance of BUS, incremented for each client
     */
    private int id;

    /**
     * Maps topics' messages to recipients.
     * Example: Topic 1 subtopic 2 has one message waiting for recipient with id 6
     * Topic 1 Subtopic 2: <6,{1,2,1,0,0,0}
     */
    private static ConcurrentMap<Topic, ArrayList<Pair<Integer, Message>>> table;

    /**
     * SoftwareBus.ToasterTest.Bus may be instantiated by both 'servers' and 'clients'. If a bus server is already
     * instantiated on the given port, remaining calls to the bus constructor will result
     * in a client bus instance.
     *
     * @param port The port this server/client bus instance will communicate on
     */
    public Bus(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            table = new ConcurrentHashMap<>();
            new Thread(() -> {
                while (true) {
                    try {
                        Socket s = serverSocket.accept();
                        clients.add(new ObjectOutputStream(s.getOutputStream()));
                        run(new ObjectInputStream(s.getInputStream()), numClients);
                        numClients++;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        } catch (BindException e) {

            try {
                socket = new Socket("localhost", port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                id = numClients;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Given a recipient client or server id, send them a message m over the respective
     * ObjectOuputStream
     *
     * @param id the id of the message recipient
     * @param m  the message to send the recipient
     */
    private void send(int id, Message m) {
        try {
            clients.get(id).writeObject(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start process associated with each bus, responsible for handling the interpretation of
     * incoming messages as subscription and publication notices, or as requests for
     * messages.
     *
     * @param in The ObjectInputStream messages are read from
     * @param id The id associated with the incoming messages
     */
    private void run(ObjectInputStream in, int id) {
        new Thread(() -> {
            while (true) {
                Message msg;
                while (true) {
                    try {
                        if (((msg = (Message) in.readObject()) != null)) {
//                            queue.put(new Pair<>(id, msg));
                            if (msg.topic() == -1) {
                                switch (msg.subtopic()) {
                                    case 1 -> {
                                        internalSub(new Topic(msg.bodyOne(), msg.bodyTwo()), id);
                                    }
                                    case 2 -> {
                                        internalPublish(new Message(msg.fullBody()));
                                    }
                                    case 3 -> {
                                        send(id, internalGetMessage(new Topic(msg.bodyOne(), msg.bodyTwo()), id));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }).start();
    }

    //TODO Delete this (used for testing)
    public int getID() {
        return id;
    }

    /**
     * Allow a user to subscribe to a Topic
     *
     * @param t the topic the user wishes to subscribe to
     */
    public void subscribe(Topic t) {
        try {
            out.writeObject(new Message(new int[]{-1, 1, t.topic(), t.subtopic(), 0, 0}));
            Object m = in.readObject();

            if (m instanceof Message msg && msg.topic() != -1) {
                throw new RuntimeException("Subscription failed");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allow a user to publish a message, which contains the topic of the message being published
     *
     * @param m the message to publish
     */
    public void publish(Message m) {
        try {
            out.writeObject(new Message(new int[]{-1, 2, m.topic(), m.subtopic(), m.bodyOne(), m.bodyTwo()})); //assume that the message is max length of 2 in body
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allow a user to receive the oldest message published to them according to
     * the specified topic
     *
     * @param t the topic whose oldest unread message will be returned
     * @return the message requested, may be null if no such message has been published
     */
    public Message getMessage(Topic t) {
        try {
            out.writeObject(new Message(new int[]{-1, 3, t.topic(), t.subtopic(), 0, 0}));
            Object obj = in.readObject();
            return (obj == null) ? null : (Message) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given the id of the user who is asking for a message of the given topic, determine
     * which message they should be given.
     *
     * @param t  the topic of message being requested
     * @param id the id of the user requesting their message
     * @return the message which is the oldest published to the given topic which
     * the user has not yet received, null if no such message exists
     */
    private Message internalGetMessage(Topic t, int id) {
        for (Pair<Integer, Message> p : table.get(t)) {
            if (p.getKey() == id) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * Given the id of a user who wants to subscribe to a topic and the topic
     * they want to subscribe to, create a new entry in the table for this subscription
     *
     * @param t  the topic the user would like to subscribe to
     * @param id the id of the user requesting subscription
     */
    private void internalSub(Topic t, int id) {
        table.putIfAbsent(t, new ArrayList<>());
        table.get(t).add(new Pair<>(id, null));
        send(id, new Message(new int[]{-1, 1, 1, 0, 0, 0}));
    }

    /**
     * Given a message which a user would like to publish, modify entries to the
     * table adding this message to the 'inboxes' of users.
     *
     * @param m the message being published
     */
    private void internalPublish(Message m) {
        Topic t = new Topic(m.topic(), m.subtopic());
        if (table.containsKey(t)) {
            for (int i = 0; i < table.get(t).size(); i++) {
                table.get(t).set(i, new Pair<>(table.get(t).get(i).getKey(), m));
            }
        }
    }
}
