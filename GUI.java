import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.*;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

public class GUI extends JFrame {
    private JComboBox<String> sexComboBox; // Dropdown for Sex category
    private JComboBox<Integer> yearComboBox; // Dropdown for Census year
    private JTextField queryInputField; // Search/Filter field
    private JTextArea factsTextArea; // Displays interesting facts
    private JTable resultsTable;
    private JButton executeButton;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private DatabaseConnector dbConnector;
    private JPanel chartPanel; // Panel to display the chart

    public GUI() {
        setTitle("Data Explorer");
        setSize(1000, 800); // Adjusted size to accommodate new components
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        dbConnector = new DatabaseConnector();
        queryInputField = new JTextField();
        executeButton = new JButton("Filter Data");
        factsTextArea = new JTextArea(5, 20);
        factsTextArea.setEditable(false);

        // Initialize the table and its sorter
        tableModel = new DefaultTableModel();
        resultsTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);

        // Setup sex category combo box with items based on the dataset
        sexComboBox = new JComboBox<>(new String[]{"Both sexes", "Male", "Female"});
        // Setup year combo box with years based on the dataset
        yearComboBox = new JComboBox<>(new Integer[]{2011, 2016}); // Example years from the data provided

        // Setup the facts panel
        JPanel factsPanel = new JPanel(new BorderLayout());
        factsPanel.add(new JScrollPane(factsTextArea), BorderLayout.CENTER);
        factsPanel.setBorder(BorderFactory.createTitledBorder("Interesting Facts"));

        // Setup the chart panel (placeholder)
        chartPanel = new JPanel(new BorderLayout());

        // Top panel for filters
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Sex:"));
        filterPanel.add(sexComboBox);
        filterPanel.add(new JLabel("Census Year:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(queryInputField);
        filterPanel.add(executeButton);

        // Add components to the frame
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        add(factsPanel, BorderLayout.EAST);
        add(chartPanel, BorderLayout.WEST);

        // Action listener for the executeButton to apply filters and update the chart
        executeButton.addActionListener(e -> {
            applyFilters();
            updateChart();
        });

        // Load initial data
        executeQuery("SELECT * FROM census"); // Replace with your actual table name
    }


    private void applyFilters() {
        String selectedSex = (String) sexComboBox.getSelectedItem();
        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();

        RowFilter<DefaultTableModel, Object> sexFilter = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // If "Both sexes" is selected, don't filter on sex
                if (selectedSex.equals("Both sexes")) {
                    return true; // include row
                } else {
                    // Otherwise, filter on the selected sex
                    return entry.getValue(0).equals(selectedSex);
                }
            }
        };

        RowFilter<DefaultTableModel, Object> yearFilter = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                return entry.getValue(2).toString().equals(selectedYear.toString());
            }
        };

        sorter.setRowFilter(RowFilter.andFilter(Arrays.asList(sexFilter, yearFilter)));

        // Update facts and chart after applying filters
        updateFacts();
        updateChart();
    }


    private void updateFacts() {
        int totalMoved = 0;
        int totalPopulation = 0; // Assuming you want to show total population as a fact

        for (int viewRow = 0; viewRow < resultsTable.getRowCount(); viewRow++) {
            int modelRow = resultsTable.convertRowIndexToModel(viewRow);
            int value = (int) tableModel.getValueAt(modelRow, 6); // "Value" column is at index 6 (adjust if needed)

            // Assuming "All Persons Who Moved" is a statistic you're interested in
            String statisticLabel = (String) tableModel.getValueAt(modelRow, 3);
            if (statisticLabel.equals("All Persons Who Moved")) {
                totalMoved += value;
            }
            totalPopulation += value; // Add to total population count for each row
        }

        // Update the text area with the interesting facts
        String factsText = "Total number of people who moved: " + totalMoved +
                "\nTotal population: " + totalPopulation; // Add other facts as needed
        factsTextArea.setText(factsText);
    }


    private void updateChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Aggregate data for the selected filters
        for (int viewRow = 0; viewRow < resultsTable.getRowCount(); viewRow++) {
            int modelRow = resultsTable.convertRowIndexToModel(viewRow);
            String statisticLabel = (String) tableModel.getValueAt(modelRow, 3);
            int value = (Integer) tableModel.getValueAt(modelRow, 5);

            dataset.addValue(value, "Value", statisticLabel);
        }

        // Create and update the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Statistics",
                "Statistic Label",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartPanel.validate();
    }

    private void executeQuery(String query) {

        dbConnector.executeQuery(query, rs -> {
            try {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    tableModel.setColumnIdentifiers(new String[columnCount]);
                    for (int i = 1; i <= columnCount; i++) {
                        try {
                            tableModel.addColumn(metaData.getColumnName(i));
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                });

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    SwingUtilities.invokeLater(() -> tableModel.addRow(row));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}











