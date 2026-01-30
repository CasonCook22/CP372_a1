import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.Socket;


public class Board {
    private final int Board_x;
    private final int Board_y;
    private final int note_width;
    private final int note_height;
    private final List<Note> notes;
    private final Set<String> validColors;
    

    public Board(int board_x, int board_y, int note_width, int note_height, List<Note> notes, Set<String> validColors) {
        this.Board_x = board_x;
        this.Board_y = board_y;
        this.note_width = note_width;
        this.note_height = note_height;
        this.notes = new ArrayList<>(notes);
        this.validColors = new HashSet<>(validColors);
    }

    public Set<String> getValidColors() {
        return validColors;
    }

    public int getBoard_x() {
        return Board_x;
    }
    public int getBoard_y() {
        return Board_y;
    }
    public int getNote_width() {
        return note_width;
    }
    public int getNote_height() {
        return note_height;
    }

    public synchronized int clearboard() {
        int count = notes.size();
        notes.clear();
        return count;
    }

    public synchronized int shake() {
        int removed = 0;
        List<Note> toRemove = new ArrayList<>();
        for (Note note : notes) {
            if (!note.ispinned()) {
                toRemove.add(note);
                removed++;
            }
        }
        notes.removeAll(toRemove);
        return removed;
    }

    public synchronized void addNote(Note note) {
        if(!note.validbound(note.getNote_x(), note.getNote_y())){
            throw new IllegalArgumentException("Note is out of board bounds");
        }

        if (!validColors.contains(note.getColor())) {
            throw new IllegalArgumentException("Invalid note color: " + note.getColor());
        }

        for (Note existingNote : notes) {
            if (note.overlap(existingNote)) {
                throw new IllegalArgumentException("Note overlaps with an existing note");
            }
        }
        notes.add(note);
    }

    public synchronized List<Note> getNotes(String color, Integer x, Integer y, String refersTo) {
        List<Note> filteredNotes = new ArrayList<>();
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
            filteredNotes.add(note);
        }
        return filteredNotes;
    }

    public synchronized List<Pin> getAllPins() {
        List<Pin> pins = new ArrayList<>();
        for (Note note : notes) {
            pins.addAll(note.getPins());
        }
        return pins;
    }

    public synchronized int pin(Pin pin) {
        int pinCount = 0;
        for (Note note : notes) {
            if (note.contains(pin.getPin_x(), pin.getPin_y())) {
                note.getPins().add(pin);
                pinCount++;
            }
        }
        return pinCount;
    }

    public synchronized int unpin(int x, int y) {
        int unpinnedCount = 0;
        for (Note note : notes) {
            if (note.getPins().removeIf(p -> p.getPin_x() == x && p.getPin_y() == y)) {
                unpinnedCount++;
            }
        }
        return unpinnedCount;
    }

    public static void main(String[] args) {
        System.out.println("Starting Board server...");
        if (args.length < 6){
            System.err.println("Not enough arguments provided. Please use java ");
            System.exit(1);
        }

        int port;
        int Board_x;
        int Board_y;
        int Note_width;
        int Note_height;
        try {
            port = Integer.parseInt(args[0]);
            Board_x = Integer.parseInt(args[1]);
            Board_y = Integer.parseInt(args[2]);
            Note_width = Integer.parseInt(args[3]);
            Note_height = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in arguments. Please ensure port, board dimensions, and note dimensions are integers.");
            System.exit(1);
            return;
        }

        String[] colorsArray = new String[args.length-5];
        System.arraycopy(args, 5, colorsArray, 0, args.length - 5);

        Board board = new Board(Board_x, Board_y, Note_width, Note_height, new ArrayList<>(), new HashSet<>(List.of(colorsArray)));
        System.out.println("Board created with dimensions: " + Board_x + "x" + Board_y + " and note size: " + Note_width + "x" + Note_height + " colors: " + String.join(", ", colorsArray));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while(true){
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, board);
                clientHandler.start();
            }
        } catch (Exception e){
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }

    }

}
