import javax.swing.JPanel;
import java.awt.*;

public class BoardPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw background
        g2d.setColor(new Color(240, 240, 200)); // Cork-like color
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw notes as rectangles
        g2d.setColor(new Color(255, 200, 100)); // Note color
        g2d.fillRect(50, 50, 100, 100); // x, y, width, height
        
        // Draw text on notes
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("My Note", 60, 100);
        
        // Draw pins
        g2d.setColor(Color.RED);
        g2d.fillOval(140, 40, 10, 10); // x, y, width, height
    }
}