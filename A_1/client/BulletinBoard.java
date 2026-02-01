import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class BulletinBoard extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JInternalFrame boardFrame;
    private JTextField xField, yField, colorField, contentField;
   private JButton postButton, getButton, pinButton, unpinButton, shakeButton, clearButton, disconnectButton;

    
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
        JPanel buttonPanel = new JPanel(new FlowLayout());

        postButton = new JButton("Post Note");
        postButton.addActionListener(e -> handlePost());
        buttonPanel.add(postButton);

        getButton = new JButton("Get Notes");
        getButton.addActionListener(e -> handleGet());
        buttonPanel.add(getButton);

        pinButton = new JButton("Pin Note");
        pinButton.addActionListener(e -> handlePin());
        buttonPanel.add(pinButton);

        unpinButton = new JButton("Unpin Note");
        unpinButton.addActionListener(e -> handleUnpin());
        buttonPanel.add(unpinButton);

        shakeButton = new JButton("Shake Board");
        shakeButton.addActionListener(e -> handleShake());
        buttonPanel.add(shakeButton);

        clearButton = new JButton("Clear Board");
        clearButton.addActionListener(e -> handleClear());
        buttonPanel.add(clearButton);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> handleDisconnect());
        buttonPanel.add(disconnectButton);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }

    private void handleGet() {
        out.println("GET");
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }

    private void handlePin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("PIN " + x + " " + y);
        out.println(command);

        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }

    private void handleUnpin() {
        String x = xField.getText();
        String y = yField.getText();

        String command = ("UNPIN " + x + " " + y);
        out.println(command);

        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }

    private void handleShake() {
        out.println("SHAKE");
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }

    private void handleClear() {
        out.println("CLEAR");
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }

    private void handleDisconnect() {
        try {
            socket.close();
            JOptionPane.showMessageDialog(this, "Disconnected from server.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error disconnecting: " + e.getMessage());
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
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, " - Error reading server response: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        new BulletinBoard();
    }
    
}
