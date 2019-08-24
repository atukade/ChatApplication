

import java.io.*;
import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the ChatServer class which is how the ChatClient can connect to and start to chat.
 *
 * @author Anuj Mohanbabu Tukade, atukade@purdue.edu
 * @version 11/24/2018
 */

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    String fileName;
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
    String strDate = format.format(date);
    static ChatFilter cf = null;


    private ChatServer(int port) {
        this.port = port;
        System.out.println(strDate + " Server waiting for clients on port " + port + ".");
    }

    public ChatServer() {
        this.port = 1500;
        System.out.println(strDate + " Server waiting for clients on port 1500.");
    }

    public ChatServer(int port, String fileName) {
        ArrayList<String> words = new ArrayList<>();
        this.port = port;
        this.fileName = fileName;
        File f = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            System.out.println("The specified file is not the correct one.");
            System.exit(0);
        }
        System.out.println("Banned Words File: " + f.getName() + "\n" + "Banned words:");
        for (int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i));
        }
        System.out.println();
        System.out.println(strDate + " Sever waiting for clients on port " + port + ".");
    }

    /*
     * This is what starts the ChatServer.
     */

    private void start() {

        try {

            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;

        switch (args.length) {
            case 0:
                server = new ChatServer();
                break;
            case 1:
                server = new ChatServer(Integer.parseInt(args[0]));
                break;
            case 2:
                server = new ChatServer(Integer.parseInt(args[0]), args[1]);
                cf = new ChatFilter(args[1]);
                break;
            default:
                server = new ChatServer(1500);
                break;
        }

        server.start();
        System.out.println();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        boolean exits = false;

        @Override
        public void run() {
            for (int i = 0; i < clients.size() - 1; i++) {
                if (clients.get(i).username.equalsIgnoreCase(username)) {
                    exits = true;
                    try {
                        sOutput.writeObject("The username already exists.");
                        remove(id);
                        uniqueId--;
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            // Read the username sent to you by client
            if (!exits) {
                try {
                    if (socket.getLocalAddress().toString().equals("/127.0.0.1"))
                        sOutput.writeObject("Connection accepted " + Inet6Address.getByName("") + ":" + port + "\n");
                    else
                        sOutput.writeObject("Connection accepted " + socket.getLocalAddress().toString().substring(1) + ":" + port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(strDate + " " + this.username + " has just connected.");
                System.out.println(strDate + " Server waiting for clients on port " + port + ".");
                outerloop:
                while (true) {
                    try {
                        cm = (ChatMessage) sInput.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println(this.username + " disconnected.");
                        remove(id);
                        return;
                    }
                    if (cm != null) {
                        if (cm.getType() == 0) {
                            if (cf == null)
                                System.out.println(strDate + " " + username + ": " + cm.getMessage());
                            else if (cf != null)
                                System.out.println(strDate + " " + username + ": " + cf.filter(cm.getMessage()));
                            // Send message back to the client
                            try {
                                if (cf == null)
                                    broadcast(cm.getMessage());
                                else if (cf != null)
                                    broadcast(cf.filter(cm.getMessage()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (cm.getType() == 1) {
                            System.out.println(strDate + " " + username + " disconnected with a LOGOUT message");
                            try {
                                sOutput.writeObject("Server has closed the connection\n");
                                remove(id);
                                close();
                                break outerloop;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (cm.getType() == 2) {
                            if (cf == null) {
                                System.out.println(strDate + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage());
                                try {
                                    sOutput.writeObject(strDate + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage() + "\n");
                                    directMessage(cm.getMessage(), cm.getRecipient());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (cf != null) {
                                System.out.println(strDate + " " + username + " -> " + cm.getRecipient() + ": " + cf.filter(cm.getMessage()));
                                try {
                                    sOutput.writeObject(strDate + " " + username + " -> " + cm.getRecipient() + ": " + cf.filter(cm.getMessage()) + "\n");
                                    directMessage(cm.getMessage(), cm.getRecipient());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if (cm.getType() == 3) {
                            ArrayList<String> names = new ArrayList<>();
                            for (int i = 0; i < clients.size(); i++) {
                                names.add(clients.get(i).username);
                            }
                            names.remove(username);

                            String res = "";
                            for (int i = 0; i < names.size(); i++) {
                                res += "\n" + "        -" + names.get(i);
                            }
                            if (names.size() == 0) {
                                try {
                                    sOutput.writeObject(strDate + " There are no active users other than you.\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    sOutput.writeObject(strDate + " The following users are currently available.");
                                    sOutput.writeObject(res + "\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
            }
        }

        private synchronized void broadcast(String message) throws IOException {

            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).writeMessage(strDate + " " + username + ": " + message + "\n");
            }
        }

        private synchronized boolean writeMessage(String msg) throws IOException {
            if (!socket.isConnected())
                return false;
            sOutput.writeObject(msg);
            return true;
        }

        private synchronized void remove(int id) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id == id) {
                    clients.remove(i);
                    i--;
                }
            }
        }

        private synchronized void close() throws IOException {
            sInput.close();
            sOutput.close();
            socket.close();

        }

        private synchronized void directMessage(String message, String username) throws IOException {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username)) {
                    clients.get(i).writeMessage(strDate + " " + this.username + " -> " + username + ": " + cf.filter(message) + "\n");
                }
            }

        }
    }
}
