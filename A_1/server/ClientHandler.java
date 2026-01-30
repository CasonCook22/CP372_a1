import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * Handles all communication with a single connected client.
 * One instance runs per client using a thread-per-connection model.
 */
public class ClientHandler extends Thread {

    // Socket connected to the client
    private final Socket clientSocket;

    // Shared bulletin board instance
    private final Board board;

    // Input and output streams for the socket
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Board board) {
        this.clientSocket = socket;
        this.board = board;
    }

    /**
     * Thread entry point.
     * Performs initial handshake, then processes client commands
     * until the client disconnects or an error occurs.
     */
    @Override
    public void run() {
        try {
            handshake();
            processCommands();
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Sends initial WELCOME message containing board configuration.
     */
    private void handshake() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        String colors = String.join(",", board.getValidColors());

        out.println(
            "WELCOME " +
            board.getBoard_x() + " " +
            board.getBoard_y() + " " +
            board.getNote_width() + " " +
            board.getNote_height() + " " +
            colors
        );
    }

    /**
     * Main request loop.
     * Reads one line at a time and dispatches to the appropriate handler.
     */
    private void processCommands() throws IOException {
        String line;

        while ((line = in.readLine()) != null) {

            String[] parts = line.trim().split(" ", 2);
            String command = parts[0].toUpperCase();
            String args = (parts.length > 1) ? parts[1] : "";

            switch (command) {
                case "POST":
                    handlePost(args);
                    break;
                case "GET":
                    handleGet(args);
                    break;
                case "PIN":
                    handlePin(args);
                    break;
                case "UNPIN":
                    handleUnpin(args);
                    break;
                case "SHAKE":
                    out.println("SHAKED " + board.shake());
                    break;
                case "CLEAR":
                    out.println("CLEARED " + board.clearboard());
                    break;
                case "DISCONNECT":
                    out.println("DISCONNECTED");
                    return;
                default:
                    out.println("ERROR INVALID_COMMAND Unknown command: " + command);
            }
        }
    }

    /**
     * Handles POST command.
     * Syntax: POST x y color content
     */
    private void handlePost(String args) {
        String[] params = args.split(" ", 4);

        if (params.length < 4) {
            out.println("ERROR INVALID_FORMAT POST requires: x y color content");
            return;
        }

        try {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            String color = params[2];
            String content = params[3];

            Note note = new Note(
                x,
                y,
                board.getNote_width(),
                board.getNote_height(),
                content,
                color,
                new HashSet<>()
            );

            board.addNote(note);
            out.println("POSTED");

        } catch (NumberFormatException e) {
            out.println("ERROR INVALID_COORDINATE Coordinates must be integers");
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        }
    }

    /**
     * Handles GET command.
     * Supports GET PINS and filter-based GET.
     */
    private void handleGet(String args) {
        args = args.trim();

        // GET PINS
        if (args.equalsIgnoreCase("PINS")) {
            List<Pin> pins = board.getAllPins();

            if (pins.isEmpty()) {
                out.println("NOPINS");
            } else {
                StringBuilder response = new StringBuilder("PINS");
                for (Pin p : pins) {
                    response.append(" ").append(p.getPin_x()).append(",").append(p.getPin_y());
                }
                out.println(response.toString());
            }
            return;
        }

        String color = null;
        Integer x = null;
        Integer y = null;
        String refersTo = null;

        try {
            if (args.contains("color=")) {
                int start = args.indexOf("color=") + 6;
                int end = args.indexOf(" ", start);
                color = args.substring(start, end == -1 ? args.length() : end);
            }

            if (args.contains("contains=")) {
                int start = args.indexOf("contains=") + 9;
                int end = args.indexOf(" ", start);
                x = Integer.parseInt(args.substring(start, end));

                start = end + 1;
                end = args.indexOf(" ", start);
                y = Integer.parseInt(args.substring(start, end == -1 ? args.length() : end));
            }

            if (args.contains("refersTo=")) {
                int start = args.indexOf("refersTo=") + 9;
                refersTo = args.substring(start);
            }

        } catch (Exception e) {
            out.println("ERROR INVALID_FORMAT Malformed GET parameters");
            return;
        }

        List<Note> results = board.getNotes(color, x, y, refersTo);

        if (results.isEmpty()) {
            out.println("NONOTES");
        } else {
            for (Note n : results) {
                out.println(
                    "NOTE " +
                    n.getNote_x() + " " +
                    n.getNote_y() + " " +
                    n.getColor() + " " +
                    n.getContent()
                );
            }
        }
    }

    /**
     * Handles PIN command.
     * Syntax: PIN x y
     */
    private void handlePin(String args) {
        String[] params = args.split(" ");

        if (params.length != 2) {
            out.println("ERROR INVALID_FORMAT PIN requires: x y");
            return;
        }

        try {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);

            int count = board.pin(new Pin(x, y));

            if (count == 0) {
                out.println("ERROR NO_NOTE_AT_COORDINATE No note contains (" + x + "," + y + ")");
            } else {
                out.println("PINNED " + count);
            }

        } catch (NumberFormatException e) {
            out.println("ERROR INVALID_COORDINATE Coordinates must be integers");
        }
    }

    /**
     * Handles UNPIN command.
     * Syntax: UNPIN x y
     */
    private void handleUnpin(String args) {
        String[] params = args.split(" ");

        if (params.length != 2) {
            out.println("ERROR INVALID_FORMAT UNPIN requires: x y");
            return;
        }

        try {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);

            int count = board.unpin(x, y);

            if (count == 0) {
                out.println("ERROR PIN_NOT_FOUND No pin exists at (" + x + "," + y + ")");
            } else {
                out.println("UNPINNED " + count);
            }

        } catch (NumberFormatException e) {
            out.println("ERROR INVALID_COORDINATE Coordinates must be integers");
        }
    }

    /**
     * Releases all resources associated with this client.
     */
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}