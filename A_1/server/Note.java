public class Note {
    private final int Note_x;
    private final int Note_y;
    private final int width;
    private final int height;
    private final String content;
    private final String color;


    public Note(int note_x, int note_y, int width, int height, String content, String color) {
        this.Note_x = note_x;
        this.Note_y = note_y;
        this.width = width;
        this.height = height;
        this.content = content;
        this.color = color;
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

    public boolean validbound(int Board_x, int Board_y) {
        return Note_x >= 0 && Note_y >= 0 && (Note_x + width) <= Board_x && (Note_y + height) <= Board_y;
    }

    public boolean overlap(Note other) {
        return this.Note_x == other.Note_x && this.Note_y == other.Note_y && this.width == other.width && this.height == other.height;
    }
    
}
