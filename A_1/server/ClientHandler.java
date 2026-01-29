import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;



public class ClientHandler extends Thread {
    // One thread per client connection to handle communication
    private final Socket clientSocket;
    private final Board board;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Board board) {
        this.clientSocket = socket;
        this.board = board;
    }

    @Override
    public void run() {
        try {
            handshake();
            processCommands();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }

    }

    private void handshake() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        String colors = String.join(",", board.getValidColors());
        out.println("WELCOME " + board.getBoard_x() + " " + board.getBoard_y() + " " +
                board.getNote_width() + " " + board.getNote_height() + " " + colors);
    }

    private void processCommands() throws IOException {
        String clientMessage;
        while ((clientMessage = in.readLine()) != null) {
            System.out.println("Received: " + clientMessage);
            String[] parts = clientMessage.split(" ",2);
            String command = parts[0].toUpperCase();
            String args = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "POST":
                    handlepost(args);
                    break;
                case "GET":
                    handleget(args);
                    break;
                case "PIN":
                    handlepin(args);
                    break;
                case "UNPIN":
                    handleunpin(args);
                    break;
                case "SHAKE":
                    out.println("SHAKED " + board.shake());
                    break;
                case "CLEAR":
                    out.println("CLEARED " + board.clearboard());
                    break;
                case "DISCONNECT":
                    handledisconnect();
                    return;
                default:
                    out.println("ERROR Unknown command: " + command);
                    continue;
            }
            out.println(clientMessage.trim());
        }
    }

    private void handlepost(String args) {
        // Implementation for handling POST command
    }

    private void handleget(String args) {
        // Implementation for handling GET command
    }
    private void handlepin(String args) {
        // Implementation for handling PIN command
    }
    private void handleunpin(String args) {
        // Implementation for handling UNPIN command
    }

    private void handledisconnect() {
        // Implementation for handling DISCONNECT command
    }

    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }



}