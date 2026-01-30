import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Board
 *
 * Represents the shared bulletin board state.
 * This class is the single synchronization point for all board operations.
 */
public class Board {

    // Board dimensions
    private final int boardX;
    private final int boardY;

    // Fixed note dimensions
    private final int noteWidth;
    private final int noteHeight;

    // All notes currently on the board
    private final List<Note> notes;

    // Set of valid colors defined at server startup
    private final Set<String> validColors;

    public Board(int boardX, int boardY,
                 int noteWidth, int noteHeight,
                 List<Note> notes,
                 Set<String> validColors) {

        this.boardX = boardX;
        this.boardY = boardY;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.notes = new ArrayList<>(notes);
        this.validColors = new HashSet<>(validColors);
    }

    /* -------------------- Getters -------------------- */

    public int getBoard_x() {
        return boardX;
    }

    public int getBoard_y() {
        return boardY;
    }

    public int getNote_width() {
        return noteWidth;
    }

    public int getNote_height() {
        return noteHeight;
    }

    public Set<String> getValidColors() {
        return validColors;
    }

    /* -------------------- Board operations -------------------- */

    /**
     * Removes all notes and pins from the board.
     *
     * @return number of notes removed
     */
    public synchronized int clearboard() {
        int count = notes.size();
        notes.clear();
        return count;
    }

    /**
     * Removes all unpinned notes from the board.
     *
     * @return number of notes removed
     */
    public synchronized int shake() {
        List<Note> toRemove = new ArrayList<>();

        for (Note note : notes) {
            if (!note.ispinned()) {
                toRemove.add(note);
            }
        }

        notes.removeAll(toRemove);
        return toRemove.size();
    }

    /**
     * Adds a new note to the board after validation.
     *
     * @throws IllegalArgumentException with protocol-level meaning
     */
    public synchronized void addNote(Note note) {

        // Bounds check
        if (!note.validbound(boardX, boardY)) {
            throw new IllegalArgumentException(
                "OUT_OF_BOUNDS Note does not fit within board bounds"
            );
        }

        // Color validation
        if (!validColors.contains(note.getColor())) {
            throw new IllegalArgumentException(
                "INVALID_COLOR Color is not supported"
            );
        }

        // Complete overlap check
        for (Note existing : notes) {
            if (note.overlap(existing)) {
                throw new IllegalArgumentException(
                    "COMPLETE_OVERLAP Note completely overlaps an existing note"
                );
            }
        }

        notes.add(note);
    }

    /**
     * Returns notes matching GET filters.
     */
    public synchronized List<Note> getNotes(String color,
                                            Integer x,
                                            Integer y,
                                            String refersTo) {

        List<Note> filtered = new ArrayList<>();

        for (Note note : notes) {

            if (color != null && !note.getColor().equalsIgnoreCase(color)) {
                continue;
            }

            if (x != null && y != null && !note.contains(x, y)) {
                continue;
            }

            if (refersTo != null && !note.getContent().contains(refersTo)) {
                continue;
            }

            filtered.add(note);
        }

        return filtered;
    }

    /**
     * Returns all pins currently on the board.
     */
    public synchronized List<Pin> getAllPins() {
        List<Pin> pins = new ArrayList<>();
        for (Note note : notes) {
            pins.addAll(note.getPins());
        }
        return pins;
    }

    /**
     * Adds a pin at the given coordinate.
     *
     * @return number of notes pinned
     */
    public synchronized int pin(Pin pin) {
        int count = 0;

        for (Note note : notes) {
            if (note.contains(pin.getPin_x(), pin.getPin_y())) {
                note.getPins().add(pin);
                count++;
            }
        }

        return count;
    }

    /**
     * Removes a pin at the given coordinate.
     *
     * @return number of notes unpinned
     */
    public synchronized int unpin(int x, int y) {
        int count = 0;

        for (Note note : notes) {
            if (note.getPins().removeIf(
                p -> p.getPin_x() == x && p.getPin_y() == y
            )) {
                count++;
            }
        }

        return count;
    }

    /* -------------------- Server startup -------------------- */

    public static void main(String[] args) {

        if (args.length < 6) {
            System.err.println(
                "Usage: java Board <port> <boardW> <boardH> <noteW> <noteH> <colors...>"
            );
            System.exit(1);
        }

        int port, boardX, boardY, noteW, noteH;

        try {
            port   = Integer.parseInt(args[0]);
            boardX = Integer.parseInt(args[1]);
            boardY = Integer.parseInt(args[2]);
            noteW  = Integer.parseInt(args[3]);
            noteH  = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("ERROR Invalid numeric startup arguments");
            return;
        }

        Set<String> colors = new HashSet<>();
        for (int i = 5; i < args.length; i++) {
            colors.add(args[i]);
        }

        Board board = new Board(
            boardX,
            boardY,
            noteW,
            noteH,
            new ArrayList<>(),
            colors
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, board);
                handler.start();
            }

        } catch (Exception e) {
            System.err.println("Fatal server error: " + e.getMessage());
        }
    }
}