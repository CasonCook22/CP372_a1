import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private int boardWidth;
    private int boardHeight;
    private int noteWidth;
    private int noteHeight;
    private String[] validColors;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String welcome = readline();
        parseWelcome(welcome);
    }

    public String readline() throws IOException {
        return in.readLine();
    }

    public void sendline(String line) {
        out.println(line);
    }

    private void parseWelcome(String welcomeLine) {
        // Example WELCOME line:
        // WELCOME 600 400 100 100 yellow,pink,blue,green
        String[] parts = welcomeLine.split(" ");
        if (parts.length < 6 || !parts[0].equals("WELCOME")) {
            throw new IllegalArgumentException("Invalid WELCOME message: " + welcomeLine);
        }
        boardWidth = Integer.parseInt(parts[1]);
        boardHeight = Integer.parseInt(parts[2]);
        noteWidth = Integer.parseInt(parts[3]);
        noteHeight = Integer.parseInt(parts[4]);
        validColors = parts[5].split(",");
    }

    /**
     * Send a command and collect the server response lines.
     * Blocks for the first response line; then gathers any immediately
     * available additional lines using in.ready() to avoid blocking.
     */
    public synchronized String sendAndReceive(String command) throws IOException {
        // Ensure only one request is in flight at a time to avoid interleaving
        out.println(command);
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line + "\n");
            System.out.println("[Client] read: " + line);
        }
        return sb.toString();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public int getNoteWidth() { return noteWidth; }
    public int getNoteHeight() { return noteHeight; }
    public String[] getValidColors() { return validColors; }

    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
