import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String receiveLine() throws IOException {
        return in.readLine();
    }

    public void sendLine(String line) {
        out.println(line);
    }

    /**
     * Send a command and collect the server response lines.
     * Blocks for the first response line; then gathers any immediately
     * available additional lines using in.ready() to avoid blocking.
     */
    public List<String> sendAndReceive(String command) throws IOException {
        out.println(command);
        List<String> responseLines = new ArrayList<>();

        String line = in.readLine(); // block for first response
        if (line == null) return responseLines;
        responseLines.add(line);

        // collect any immediately-available additional lines
        while (in.ready()) {
            line = in.readLine();
            if (line == null) break;
            responseLines.add(line);
        }
        return responseLines;
    }

    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
