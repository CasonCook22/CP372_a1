import javax.swing.JFrame;

public class BulletinBoard extends JFrame {
    public BulletinBoard() {
        setTitle("Bulletin Board");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new BulletinBoard();
    }
    
}
