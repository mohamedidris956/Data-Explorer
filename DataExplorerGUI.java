
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataExplorerGUI extends JFrame {
    private JTextField queryInputField;
    private JTextArea queryResultArea;

    public DataExplorerGUI() {
        setTitle("Data Explorer");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        DatabaseConnector dbConnector = new DatabaseConnector(); // Create the database connector

        queryInputField = new JTextField();
        JButton queryButton = new JButton("Run Query");
        queryResultArea = new JTextArea();
        queryResultArea.setEditable(false);

        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeQuery(queryInputField.getText().trim());
            }
        });

        add(queryInputField, BorderLayout.NORTH);
        add(new JScrollPane(queryResultArea), BorderLayout.CENTER);
        add(queryButton, BorderLayout.SOUTH);
    }

    private void executeQuery(String query) {
        // Implement the query execution logic here.
        // Use the DatabaseConnection utility to get a connection and run your query.
        // Update queryResultArea with the results.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataExplorerGUI gui = new DataExplorerGUI();
            gui.setVisible(true);
        });
    }
}

