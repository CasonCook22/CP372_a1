import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class BulletinBoard extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea displayArea;
    private JInternalFrame boardFrame;
    private JTextField xField, yField, colorField, contentField;
    private JButton postButton, getButton, pinButton, unpinButton, shakeButton, clearButton, disconnectButton, textClearButton;

    
    public BulletinBoard() {
        setTitle("Bulletin Board");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //try to connect to server
        try {
            socket = new Socket("localhost", 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcome = in.readLine();
            System.out.println("Server: " + welcome);
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
        initializeNotes();
    }

    private void initializeNotes(){
        handleGet();
    }

    private void handleGet() {
        out.println("GET");
        try {
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                String msg = line;
                SwingUtilities.invokeLater(() ->
                    displayArea.append(msg + "\n")
                );
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Error reading server response: " + e.getMessage())
            );
        }
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
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage())
            );
        }
    }


    public static void main(String[] args) {
        new BulletinBoard();
    }
    
}
