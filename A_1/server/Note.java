import java.util.HashSet;
import java.util.Set;


public class Note {
    private final int Note_x;
    private final int Note_y;
    private final int width;
    private final int height;
    private final String content;
    private final String color;
    
    private final Set<Pin> pins;


    public Note(int note_x, int note_y, int note_width, int note_height, String content, String color, Set<Pin> pins) {
        this.Note_x = note_x;
        this.Note_y = note_y;
        this.width = note_width;
        this.height = note_height;
        this.content = content;
        this.color = color;
        this.pins = new HashSet<>();
    }

    public int getNote_x() {
        return Note_x;
    }
    public int getNote_y() {
        return Note_y;
    }
    public String getContent() {
        return content;
    }
    public String getColor() {
        return color;
    }
    public Set<Pin> getPins() {
        return pins;
    }
    public boolean contains(int x, int y) {
        return x >= Note_x && x < Note_x + width && y >= Note_y && y < Note_y + height;
    }

    public boolean validbound(int Board_x, int Board_y) {
        return Note_x >= 0 && Note_y >= 0 && (Note_x + width) <= Board_x && (Note_y + height) <= Board_y;
    }

    public boolean overlap(Note other) {
        return this.Note_x == other.Note_x && this.Note_y == other.Note_y && this.width == other.width && this.height == other.height;
    }



    public boolean ispinned() {
        return !pins.isEmpty();
    }
    
}
