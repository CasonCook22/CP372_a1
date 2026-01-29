import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;



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
        System.out.println("Sent WELCOME to client");
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
                    out.println("DISCONNECTED");
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
        String[] params = args.split(" ", 4);
        if (params.length < 4) {
            out.println("ERROR Invalid POST command format");
            return;
        }
        try {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            String color = params[2];
            String content = params[3];

            Note newNote = new Note(x, y,board.getNote_width(),board.getNote_height(), content, color, new HashSet<>());
            board.addNote(newNote);
        } catch (NumberFormatException  e) {
            out.println("ERROR Invalid coordinates in POST command");
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        }
    }

    private void handleget(String args) {
        // Implementation for handling GET command
        args = args.trim();

        if (args.isEmpty()) {
            out.println("ERROR Invalid GET command format");
            return;
        }
        if (args.equalsIgnoreCase("PINS")) {
            List<Pin> allPins = board.getAllPins();
            if (allPins.isEmpty()) {
                out.println("NOPINS");
            } else {
                StringBuilder response = new StringBuilder("PINS");
                for (Pin pin : allPins) {
                    response.append(" ").append(pin.getPin_x()).append(",").append(pin.getPin_y());
                }
                out.println(response.toString());
            }
        }
        String color = null;
        Integer x = null;
        Integer y = null;
        String refersTo = null;

        if (args.contains("color=")) {
            int start = args.indexOf("color=") + 6;
            int end = args.indexOf(" ", start);
            color = args.substring(start, end == -1 ? args.length() : end).trim();
        }
        if (args.contains("contains=")) {
            int start = args.indexOf("contains=") + 9;
            int end = args.indexOf(" ", start);
            x = Integer.parseInt(args.substring(start, end).trim());

            start = end + 1;
            end = args.indexOf(" ", start);
            y = Integer.parseInt(args.substring(start, end == -1 ? args.length() : end).trim());
        }
        if (args.contains("refersTo=")) {
            int start = args.indexOf("refersTo=") + 9;
            refersTo = args.substring(start).trim();
        }

        List<Note> filteredNotes = board.getNotes(color,x,y,refersTo);

        if (filteredNotes.isEmpty()) {
            out.println("NONOTES");
        } else {
            for (Note note : filteredNotes) {
                out.println("NOTE " + note.getNote_x() + " " + note.getNote_y() + " " +
                        note.getColor() + " " + note.getContent());
            }
        }
    }


    private void handlepin(String args) {
        // Implementation for handling PIN command
        String[] params = args.split(" ");
        if (params.length < 2) {
            out.println("ERROR Invalid PIN command format");
            return;
        }

        try {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);

            Pin newPin = new Pin(x, y);
            int pins = board.pin(newPin);
            out.println("PINNED: " + pins + " notes pinned at (" + x + "," + y + ")");
        } catch (NumberFormatException e) {
            out.println("ERROR Invalid coordinates in PIN command");
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        }
    }
    private void handleunpin(String args) {
        // Implementation for handling UNPIN command

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