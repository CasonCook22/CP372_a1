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

        // Create a center panel to hold both board and display area
        JPanel centerPanel = new JPanel(new BorderLayout());
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
    }
    // keep references


    private void handleGet() {
        String x = xField.getText();
        String y = yField.getText();
        String color = colorField.getText();
        String content = contentField.getText();
        String command = "GET";

        if (!color.isEmpty()) command += " color=" + color;
        if (!x.isEmpty() && !y.isEmpty()) command += " contains=" + x + " " + y;
        if (!content.isEmpty()) command += " refersTo=" + content;
    
        try {
            String lines = client.sendAndReceive(command);
            // append server messages to display area
            SwingUtilities.invokeLater(() -> displayArea.append(lines + "\n"));
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handlePin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("PIN " + x + " " + y);

        try {
            String response = client.sendAndReceive(command);
            SwingUtilities.invokeLater(() -> {
                displayArea.append("Server response: " + response + "\n");
            });
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
            String response = client.sendAndReceive(command);
            SwingUtilities.invokeLater(() -> {
                displayArea.append("Server response: " + response + "\n");
            });
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handleShake() {
        try {
            String response = client.sendAndReceive("SHAKE");
            SwingUtilities.invokeLater(() -> {
                displayArea.append("Server response: " + response + "\n");
            });
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error communicating with server: " + e.getMessage())
            );
        }
    }

    private void handleClear() {
        try {
            String response = client.sendAndReceive("CLEAR");
            SwingUtilities.invokeLater(() -> {
                displayArea.append("Server response: " + response + "\n");
            });
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
            String response = client.sendAndReceive("DISCONNECT");
            SwingUtilities.invokeLater(() -> {
                displayArea.append(response + "\n");
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
            String response = client.sendAndReceive(command);

            SwingUtilities.invokeLater(() -> {
                displayArea.append(response + "\n");
            });
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
