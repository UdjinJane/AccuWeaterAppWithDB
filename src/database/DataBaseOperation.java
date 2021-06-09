package database;
import enums.Periods;
import java.io.IOException;
import java.sql.SQLException;

public interface DataBaseOperation {

     void getWeatherToDB(Periods periods) throws IOException, SQLException;
    // void getAllFromDb() throws IOException;
    // boolean saveWeatherData(WeatherData weatherData) throws SQLException;
    // List<WeatherData> getAllSavedData() throws IOException;
}

