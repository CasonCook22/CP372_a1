import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.awt.event.*;
import java.io.*;

/*BUGS:
When posting a 2nd note, the first note disappears until GET is clicked again.
cant reqeust or dont know how to request get color = red, maybe make the text field show after pressing post, get pin, get notes or pin
pin gui
disconnect should close window or return to connection panel
*/


public class BulletinBoard extends JFrame {
    private NetworkClient client;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JPanel connectPanel;

    private JTextArea displayArea;
    //private JInternalFrame boardFrame;
    private JTextField xField, yField, colorField, contentField;
    private JButton postButton, getButton, pinButton, unpinButton, shakeButton, clearButton, disconnectButton, textClearButton;

    public BulletinBoard() {
        super("Network Bulletin Board");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        connectPanel = createConnectPanel();
        mainPanel.add(connectPanel, "CONNECT");

        add(mainPanel, BorderLayout.CENTER);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null && client.isConnected()){
                    try {
                        client.disconnect();
                    } catch (IOException ex) {
                        // Log the error but allow the window to close
                        System.err.println("Error disconnecting: " + ex.getMessage());
                    }
                }
            }
        });
    }

    private JPanel createConnectPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JTextField hostField = new JTextField("", 15);
        JTextField portField = new JTextField("", 15);
        JButton connectButton = new JButton("Connect");
        JLabel statusLabel = new JLabel(" ");

        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Host:"), gbc);

        gbc.gridx = 1;
        panel.add(hostField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Port:"), gbc);

        gbc.gridx = 1;
        panel.add(portField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(connectButton, gbc);

        gbc.gridy = 3;
        panel.add(statusLabel, gbc);

        connectButton.addActionListener(e -> {
            String host = hostField.getText().trim();
            int port;

            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid port number");
                return;
            }
            try {
                client = new NetworkClient();
                client.connect(host, port);
                statusLabel.setText("Connected to " + host + ":" + port);

                // Initialize the board UI on the EDT after successful handshake
                SwingUtilities.invokeLater(() -> {
                    initialize();
                    cardLayout.show(mainPanel, "BOARD");
                });

            } catch (IOException ex) {
                statusLabel.setText("Connection failed: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                statusLabel.setText("Protocol error: " + ex.getMessage());
            }
        });

        return panel;
    }
    public void initialize() {

        // GUI setup
        displayArea = new JTextArea(10,40);
        displayArea.setEditable(false);

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(600, 400));

        // Configure panel sizes from handshake (if available)
        if (client != null && client.isConnected()) {
            try {
                boardPanel.setBoardSize(client.getBoardWidth(), client.getBoardHeight());
                boardPanel.setNoteSize(client.getNoteWidth(), client.getNoteHeight());
            } catch (Exception ignored) {}
        }

        // Create a center panel to hold both board and display area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(displayArea), BorderLayout.SOUTH);

        // INPUTS
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

        // BUTTONS
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

        // assemble into a board container and add to the card layout
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.add(centerPanel, BorderLayout.CENTER);
        boardContainer.add(inputPanel, BorderLayout.NORTH);
        boardContainer.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(boardContainer, "BOARD");

        setVisible(true);
        // initialize board panel using welcome message and GET
        this.boardPanel = boardPanel;
        initializeNotes();
    }
    // keep references
    private BoardPanel boardPanel;

    private void initializeNotes(){
        // retrieve notes and pins from server and update panel
        new Thread(this::handleGet).start();
    }

    private void handleGet() {
        try {
            SwingUtilities.invokeLater(() -> displayArea.append("DEBUG: sending GET\n"));
            java.util.List<String> lines = client.sendAndReceive("GET");

            SwingUtilities.invokeLater(() -> displayArea.append("DEBUG: received " + lines.size() + " lines\n"));

            // append server messages to display area
            for (String msg : lines) {
                SwingUtilities.invokeLater(() -> displayArea.append(msg + "\n"));
            }

            // parse notes & pins and update board panel
            getNotesToDisplay(lines, boardPanel);

        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Error communicating with server: " + e.getMessage())
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
            if (l == null || l.isEmpty() || l.equals("END")) continue;
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

    private void refreshBoard(){
        // short delay to avoid racing the server immediately after a POST
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            handleGet();
        }).start();
    }

    private void handlePin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("PIN " + x + " " + y);

        try {
            java.util.List<String> response = client.sendAndReceive(command);
            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append("Server response: " + r + "\n");
            });
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handleUnpin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("UNPIN " + x + " " + y);

        try {
            java.util.List<String> response = client.sendAndReceive(command);
            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append("Server response: " + r + "\n");
            });
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handleShake() {
        try {
            java.util.List<String> response = client.sendAndReceive("SHAKE");
            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append("Server response: " + r + "\n");
            });
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handleClear() {
        try {
            java.util.List<String> response = client.sendAndReceive("CLEAR");
            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append("Server response: " + r + "\n");
            });
            refreshBoard();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
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
            java.util.List<String> response = client.sendAndReceive("DISCONNECT");
            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append(r + "\n");
                JOptionPane.showMessageDialog(this, "Disconnected from server.");
            });
            client.disconnect();
        } catch (IOException e)
         {
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

        try {
            java.util.List<String> response = client.sendAndReceive(command);

            SwingUtilities.invokeLater(() -> {
                for (String r : response) displayArea.append(r + "\n");
                displayArea.append("DEBUG: POST response received, refreshing board...\n");
            });

            if (!response.isEmpty() && response.get(0).startsWith("ERROR")) {
                showError(response.get(0));
            } else {
                refreshBoard();
            }

        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }
    private void showError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Server Error", JOptionPane.ERROR_MESSAGE);
            displayArea.append(error + "\n");
        });
    }

    public static void main(String[] args) {

        BulletinBoard board = new BulletinBoard();
        board.setVisible(true);
    }
    
}
