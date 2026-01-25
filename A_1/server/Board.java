import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class Board {
    private final int Board_x;
    private final int Board_y;
    private final List<Note> notes;
    private final Set<String> validColors;

    public Board(int board_x, int board_y, List<Note> notes, Set<String> validColors) {
        this.Board_x = board_x;
        this.Board_y = board_y;
        this.notes = new ArrayList<>(notes);
        this.validColors = new HashSet<>(validColors);
    }

    public int clearboard() {
        int count = notes.size();
        notes.clear();
        return count;
    }

    public int getBoard_x() {
        return Board_x;
    }
    public int getBoard_y() {
        return Board_y;
    }


}
