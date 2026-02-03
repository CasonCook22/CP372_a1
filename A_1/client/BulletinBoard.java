<<<<<<< Updated upstream
import javax.swing.JFrame;

public class BulletinBoard extends JFrame {
=======
import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class BulletinBoard extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea displayArea;
    //private JInternalFrame boardFrame;
    private JTextField xField, yField, colorField, contentField;
    private JButton postButton, getButton, pinButton, unpinButton, shakeButton, clearButton, disconnectButton, textClearButton;

    
>>>>>>> Stashed changes
    public BulletinBoard() {
        setTitle("Bulletin Board");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
<<<<<<< Updated upstream
        setVisible(true);
    }

=======
        setLayout(new BorderLayout());

        //try to connect to server
        try {
            socket = new Socket("localhost", 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcome = in.readLine();
            System.out.println("Server: " + welcome);
            // parse welcome and configure board panel later after creation
            // store welcome line in a field for initialize
            this.welcomeLine = welcome;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error connecting to server: " + e.getMessage());
        }

        //GUI setup
        displayArea = new JTextArea(10,40);
        displayArea.setEditable(false);
        
        BoardPanel boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(600, 400));

        // Create a center panel to hold both board and display area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(displayArea), BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        //INPUTS
        JPanel inputPanel = new JPanel(new GridLayout(5,2,5,5));

        inputPanel.add(new JLabel("X:"));
        xField = new JTextField();
        inputPanel.add(xField);
        
        inputPanel.add(new JLabel("Y:"));
        yField = new JTextField();
        inputPanel.add(yField);
        
        inputPanel.add(new JLabel("Color:"));
        colorField = new JTextField();
        inputPanel.add(colorField);
        
        inputPanel.add(new JLabel("Content:"));
        contentField = new JTextField();
        inputPanel.add(contentField);

        //BUTTONS
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 5, 5));

        postButton = new JButton("Post Note");
        postButton.addActionListener(e -> new Thread(this::handlePost).start());
        buttonPanel.add(postButton);

        getButton = new JButton("Get Notes");
        getButton.addActionListener(e -> new Thread(this::handleGet).start());

        buttonPanel.add(getButton);

        pinButton = new JButton("Pin Note");
        pinButton.addActionListener(e -> new Thread(this::handlePin).start());
        buttonPanel.add(pinButton);

        unpinButton = new JButton("Unpin Note");
        unpinButton.addActionListener(e -> new Thread(this::handleUnpin).start());
        buttonPanel.add(unpinButton);

        shakeButton = new JButton("Shake Board");
        shakeButton.addActionListener(e -> new Thread(this::handleShake).start());
        buttonPanel.add(shakeButton);

        clearButton = new JButton("Clear Board");
        clearButton.addActionListener(e -> new Thread(this::handleClear).start());
        buttonPanel.add(clearButton);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> new Thread(this::handleDisconnect).start());
        buttonPanel.add(disconnectButton);

        textClearButton = new JButton("Clear Text");
        textClearButton.addActionListener(e -> handleTextClear());
        buttonPanel.add(textClearButton);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
        // initialize board panel using welcome message and GET
        if (this.welcomeLine != null) {
            parseWelcomeToPanel(this.welcomeLine, boardPanel);
        }
        this.boardPanel = boardPanel;
        initializeNotes();
    }

    // keep references
    private String welcomeLine = null;
    private BoardPanel boardPanel;

    private void initializeNotes(){
        // retrieve notes and pins from server and update panel
        new Thread(this::handleGet).start();
    }

    private void handleGet() {
        out.println("GET");
        try {
            java.util.List<String> lines = new java.util.ArrayList<>();
            String line = in.readLine(); // block for first response line
            if (line == null) return;
            lines.add(line);
            // read any immediately-available additional lines
            while (in.ready()) {
                line = in.readLine();
                if (line == null) break;
                lines.add(line);
            }

            // append server messages to display area
            for (String msg : lines) {
                SwingUtilities.invokeLater(() -> displayArea.append(msg + "\n"));
            }

            // parse notes & pins and update board panel
            getNotesToDisplay(lines, boardPanel);

        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Error reading server response: " + e.getMessage())
            );
        }
    }

    /**
     * Parse server GET/WELCOME lines and update the BoardPanel.
     * Expects lines such as:
     * - "WELCOME <boardX> <boardY> <noteW> <noteH> <colors...>"
     * - "NOTE x y color content..."
     * - "NONOTES" or "PINS x,y x,y..." / "NOPINS"
     */
    private void getNotesToDisplay(java.util.List<String> lines, BoardPanel panel) {
        java.util.List<BoardPanel.NoteData> noteList = new java.util.ArrayList<>();
        java.util.List<Point> pinList = new java.util.ArrayList<>();

        for (String l : lines) {
            if (l == null || l.isEmpty()) continue;
            if (l.startsWith("NOTE ")) {
                // NOTE x y color content
                String[] parts = l.split(" ", 5);
                if (parts.length >= 5) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        String color = parts[3];
                        String content = parts[4];
                        int nw = panel.getNoteWidth();
                        int nh = panel.getNoteHeight();
                        noteList.add(new BoardPanel.NoteData(x, y, nw, nh, color, content));
                    } catch (NumberFormatException ignored) { }
                }
            } else if (l.startsWith("PINS")) {
                // PINS x,y x,y ...
                String rest = l.substring(4).trim();
                if (!rest.isEmpty()) {
                    String[] coords = rest.split(" ");
                    for (String c : coords) {
                        String[] xy = c.split(",");
                        if (xy.length == 2) {
                            try {
                                int px = Integer.parseInt(xy[0]);
                                int py = Integer.parseInt(xy[1]);
                                pinList.add(new Point(px, py));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            } else if (l.startsWith("NONOTES") || l.startsWith("NOPINS")) {
                // nothing to add
            }
        }

        // The server doesn't send note dimensions with GET responses, so don't override note size here.
        panel.setNotes(noteList);
        panel.setPins(pinList);
    }

    private void parseWelcomeToPanel(String welcome, BoardPanel panel) {
        if (welcome == null) return;
        // WELCOME boardX boardY noteW noteH colors
        if (!welcome.startsWith("WELCOME")) return;
        String[] parts = welcome.split(" ", 6);
        if (parts.length >= 5) {
            try {
                int boardX = Integer.parseInt(parts[1]);
                int boardY = Integer.parseInt(parts[2]);
                int noteW = Integer.parseInt(parts[3]);
                int noteH = Integer.parseInt(parts[4]);
                panel.setBoardSize(boardX, boardY);
                panel.setNoteSize(noteW, noteH);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void refreshBoard(){
        new Thread(this::handleGet).start();
    }


    private void handlePin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("PIN " + x + " " + y);
        out.println(command);

        try {
            String response = in.readLine();
            SwingUtilities.invokeLater(() ->
                displayArea.append("Server response: " + response + "\n")
            );
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }

    private void handleUnpin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("UNPIN " + x + " " + y);
        out.println(command);

        try {
            String response = in.readLine();
            SwingUtilities.invokeLater(() ->
                displayArea.append("Server response: " + response + "\n")
            );
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }

    private void handleShake() {
        out.println("SHAKE");
        try {
            String response = in.readLine();
            SwingUtilities.invokeLater(() ->
                displayArea.append("Server response: " + response + "\n")
            );
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }

    private void handleClear() {
        out.println("CLEAR");
        try {
            String response = in.readLine();
            SwingUtilities.invokeLater(() ->
                displayArea.append("Server response: " + response + "\n")
            );
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }

    private void handleTextClear() {
        xField.setText("");
        yField.setText("");
        colorField.setText("");
        contentField.setText("");
    }

    private void handleDisconnect() {
        try {
            socket.close();
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Disconnected from server.")
            );
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error disconnecting: " + e.getMessage())
            );
        }
    }

    private void handlePost() {
        String x = xField.getText();
        String y = yField.getText();
        String color = colorField.getText();
        String content = contentField.getText();

        String command = ("POST " + x + " " + y + " " + color + " " + content);
        out.println(command);

        try {
            String response = in.readLine();
            SwingUtilities.invokeLater(() ->
                displayArea.append("Server response: " + response + "\n")
            );
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }


>>>>>>> Stashed changes
    public static void main(String[] args) {
        new BulletinBoard();
    }
    
}
