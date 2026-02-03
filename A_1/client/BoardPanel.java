import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {

    private int boardWidth = 600;
    private int boardHeight = 400;
    private int noteWidth = 100;
    private int noteHeight = 100;

    private final List<NoteData> notes = new ArrayList<>();
    private final List<Point> pins = new ArrayList<>();

    public static class NoteData {
        public final int x, y, w, h;
        public final String color;
        public final String content;

        public NoteData(int x, int y, int w, int h, String color, String content) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.color = color; this.content = content;
        }
    }

    public void setBoardSize(int w, int h) {
        this.boardWidth = w;
        this.boardHeight = h;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        revalidate();
        repaint();
    }

    public void setNoteSize(int w, int h) {
        this.noteWidth = w*5;
        this.noteHeight = h*5;
        repaint();
    }

    public int getNoteWidth() { return noteWidth; }
    public int getNoteHeight() { return noteHeight; }

    public void setNotes(List<NoteData> newNotes) {
        synchronized (notes) {
            notes.clear();
            notes.addAll(newNotes);
        }
        repaint();
    }

    public void setPins(List<Point> newPins) {
        synchronized (pins) {
            pins.clear();
            pins.addAll(newPins);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.setColor(new Color(240, 240, 200)); // Cork-like color
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw notes
        synchronized (notes) {
            for (NoteData n : notes) {
                Color noteColor = parseColor(n.color);
                g2d.setColor(noteColor);
                g2d.fillRect(n.x*3, n.y*3, n.w, n.h);
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                // Draw content with simple clipping
                String[] lines = n.content.split("\\n");
                int textX = n.x + 5;
                int textY = n.y + 15;
                for (String line : lines) {
                    g2d.drawString(line, textX*3, textY*3);
                    textY += 14;
                    if (textY > n.y + n.h - 5) break;
                }
            }
        }

        // Draw pins
        g2d.setColor(Color.RED);
        synchronized (pins) {
            for (Point p : pins) {
                int size = 8;
                g2d.fillOval(p.x - size/2, p.y - size/2, size, size);
            }
        }
    }

    private Color parseColor(String c) {
        if (c == null) return new Color(255, 235, 180);
        switch (c.toLowerCase()) {
            case "red": return new Color(255, 100, 100);
            case "green": return new Color(100, 255, 100);
            case "blue": return new Color(100, 100, 255);
            //case "pink": return new Color(255, 200, 220);
            default: return new Color(255, 235, 180);
        }
    }

}