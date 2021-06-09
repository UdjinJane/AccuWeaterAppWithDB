import database.DataBaseOperation;
import enums.Periods;
import java.io.IOException;
import java.sql.*;
import java.util.List;

public class DatabaseSqliteImpl implements DataBaseOperation {
    String filename = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    // Запроc на создание таблицы в базе данных, если такой нет.
    String createTableQuery = "CREATE TABLE IF NOT EXISTS weather (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "city TEXT NOT NULL," +
            "date_time TEXT NOT NULL," +
            "weather_text TEXT NOT NULL," +
            "temperature REAL NOT NULL" +
            ");";

    // Запрос на вставку данных в таблицу. Темплейт.
    String insertWeatherQuery = "INSERT INTO weather (city, date_time, weather_text, temperature) VALUES (?,?,?,?);";
    String selectAllquerry = "SELECT * FROM weather;";

    // Получаем в конструкторе имя файла базы данных из переменной аппликации.
    public DatabaseSqliteImpl(){
        filename = ApplicationGlobalState.getInstance().getDbFileName();
    }

    // Формируем соединение с базой данных.
     Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
        connection.setAutoCommit(true);
        return connection;
    }



    // Создаем таблицу, если в этом есть необходимость.
     void createTableIfNotExists() throws SQLException {
        try (Connection connection = getConnection()) {
            // Контролька на проверку запроса.
            // System.out.println(createTableQuery);
            connection.createStatement().execute(createTableQuery);
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
     }


    void saveDataToTable(String city, String dateTime,String wText, double temperature) throws SQLException {

        try( Connection connection = getConnection();
         PreparedStatement saveWeather = connection.prepareStatement(insertWeatherQuery);) {
            saveWeather.setString(1, city);
            saveWeather.setString(2, dateTime);
            saveWeather.setString(3, wText);
            saveWeather.setDouble(4, temperature);
            saveWeather.execute();
            saveWeather.closeOnCompletion();
        }
        catch (SQLException e) {
            System.out.println("Не могу выполнить запрос на вставку данных.");
            e.printStackTrace();
        }
    }

    void selectAll() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();) {
            ResultSet resultSet = statement.executeQuery(selectAllquerry);
            System.out.println("N | Город | Время | Погода | Температура");
            while ( resultSet.next())
            {
                System.out.println(
                        resultSet.getInt(1) +
                                " | " + resultSet.getString(2) +
                                " | "+ resultSet.getString(3)+
                                " | " + resultSet.getString(4) +
                                " | " + resultSet.getDouble(5)
                );
            }

            statement.closeOnCompletion();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }



    @Override
    public void getWeatherToDB(Periods periods) throws IOException {

    }
}
