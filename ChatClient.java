import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This is the ChatClient class which starts a client and sends message to the server
 *
 * @author Anuj Mohanbabu Tukade, atukade@purdue.edu
 * @version 11/24/2018
 */

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    public static boolean logout = false;
    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    public ChatClient(String username, int port) {
        this.username = username;
        this.port = port;
        this.server = "localhost";
    }

    public ChatClient(String username) {
        this.username = username;
        this.port = 1500;
        this.server = "localhost";
    }

    public ChatClient() {

        this.username = "Anonymous";
        this.port = 1500;
        this.server = "localhost";
    }

    /*
     * This starts the Chat Client
     */

    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("The specified server is not yet started.");
            System.exit(0);
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        // Create client and start it
        ChatClient client = null;
        switch (args.length) {
            case 0:
                client = new ChatClient();
                break;
            case 1:
                client = new ChatClient(args[0]);
                break;
            case 2:
                client = new ChatClient(args[0], Integer.parseInt(args[1]));
                break;
            case 3:
                client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);
                break;
        }
        if (client != null) {
            client.start();
        } else {
            System.out.println("please enter the specified input (username,portNumber,serverAddress)");
            return;
        }


        Scanner sc = new Scanner(System.in);
        String msg;

        while (sc.hasNextLine()) {
            msg = sc.nextLine();
            if (msg.length() == 0) {

            }
            else {
                String[] split = msg.split(" ");

                if (split[0].equals("/msg")) {
                    if (split.length == 1) {
                        System.out.println("Please enter the correct format (/msg Username Message) to send a direct message.");
                    } else {
                        String x = "";
                        for (int i = 2; i < split.length; i++) {
                            x += split[i] + " ";
                        }
                        if (split[1].equals(client.username)) {
                            System.out.println("You cannot direct message yourself.");
                        } else {
                            client.sendMessage(new ChatMessage(2, x, split[1]));
                        }
                    }
                } else if (msg.equals("/list")) {
                    client.sendMessage(new ChatMessage(3, msg));
                } else if (msg.equals("/logout")) {
                    client.sendMessage(new ChatMessage(1, msg));
                    logout = true;
                } else {
                    client.sendMessage(new ChatMessage(0, msg));
                }
            }
        }
        // Send an empty message to the server

    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {

            while (true) {
                try {
                    String msg = "";
                    if (!socket.isClosed()) {
                        try {
                            msg = (String) sInput.readObject();
                            if (msg.equals("The username already exists.")) {
                                System.out.println(msg);
                                System.exit(0);
                            }
                        } catch (EOFException e) {
                            if (logout) {
                                System.exit(0);
                            } else {
                                System.out.println("Server has abruptly closed the connection. Please try again later.");
                                System.exit(0);
                            }

                        }

                    } else
                        break;
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {

                    System.out.println("Server has abruptly closed the connection. Please try again later.");
                    System.exit(0);
                }

            }
        }
    }
}
