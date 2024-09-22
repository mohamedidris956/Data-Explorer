import java.sql.*;

public class DatabaseConnector {
    private String url = "jdbc:mysql://localhost:3306/population";
    private String username = "root";
    private String password = "";

    // Functional interface for processing ResultSet
    @FunctionalInterface
    public interface ResultSetProcessor {
        void process(ResultSet rs) throws SQLException;
    }

    // Constructor to handle the driver loading
    public DatabaseConnector() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method to obtain a connection to the database
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    // Method to execute a query and process the ResultSet with a given processor
    public void executeQuery(String query, ResultSetProcessor processor) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            processor.process(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Example usage or testing method. This can be removed or kept for testing.
    public static void main(String[] args) {
        DatabaseConnector dbConnector = new DatabaseConnector();
        dbConnector.executeQuery("SELECT * FROM census", resultSet -> {
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5));
                // Utilize the retrieved data as needed
            }
        });
    }
}
