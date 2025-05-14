package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The GameClient class is responsible for connecting to the server and sending commands.
 * It reads commands from the keyboard and sends them to the server.
 */
public class GameClient {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running;

    /**
     * Creates a new GameClient instance.
     *
     * @param serverAddress The server's address
     * @param serverPort The server's port
     */
    public GameClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Connects to the server and starts the client.
     */
    public void start() {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server at " + serverAddress + ":" + serverPort);

            // Initialize input and output streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a thread to handle server responses
            Thread serverListener = new Thread(this::listenToServer);
            serverListener.start();

            // Read commands from keyboard and send to server
            running = true;
            BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
            String command;

            System.out.println("Enter commands (type 'exit' to quit):");

            while (running && (command = keyboardReader.readLine()) != null) {
                // Check if the client wants to exit
                if (command.equalsIgnoreCase("exit")) {
                    out.println("exit");
                    break;
                }

                // Send the command to the server
                out.println(command);
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Listens for responses from the server.
     */
    private void listenToServer() {
        try {
            String serverResponse;
            while (running && (serverResponse = in.readLine()) != null) {
                System.out.println("Server: " + serverResponse);

                // If server stops, exit the client
                if (serverResponse.equals("Server stopped")) {
                    running = false;
                    System.out.println("Server has stopped. Exiting...");
                    break;
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Lost connection to server: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    /**
     * Stops the client and closes all resources.
     */
    public void stop() {
        running = false;

        try {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }

    /**
     * The main method to start the client.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Default server address
        int serverPort = 8099; // Default server port

        // Check if server address and port are specified in command line arguments
        if (args.length > 0) {
            serverAddress = args[0];
        }

        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 8099.");
            }
        }

        GameClient client = new GameClient(serverAddress, serverPort);
        client.start();
    }
}