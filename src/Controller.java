

import database.DataBaseOperation;
import enums.Functionality;
import enums.Periods;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// Реализация бизнес логики.

public class Controller {

    WeatherProvider weatherProvider = new AccuWeatherProvider();
    DataBaseOperation dataBaseOperation = new AccuWeatherProvider();

    // Мапа int >> inam.
    Map<Integer, Functionality> variantResult = new HashMap();
// В нумериках соотносим переменные со значениями.
    public Controller() {
        variantResult.put(1, Functionality.GET_CURRENT_WEATHER);
        variantResult.put(2, Functionality.GET_WEATHER_IN_NEXT_5_DAYS);
        variantResult.put(3, Functionality.GET_CURRENT_WEATHER_TO_DB);

    }

    // Опять парсим введеное.
    public void onUserInput(String input) throws IOException, SQLException {
        int command = Integer.parseInt(input);
        if (!variantResult.containsKey(command)) {
            throw new IOException("There is no command for command-key " + command);
        }
    // Свичуемся по вариантам запроса.
        switch (variantResult.get(command)) {
            case GET_CURRENT_WEATHER:
                getCurrentWeather();
                break;
            case GET_WEATHER_IN_NEXT_5_DAYS:
                getWeatherIn5Days();
                break;
            case GET_CURRENT_WEATHER_TO_DB:
                getCurrentWeatherToDB();

        }
    }

    public void getCurrentWeatherToDB() throws IOException, SQLException {
        // weatherProvider.getWeatherToDB(Periods.NOW);
        dataBaseOperation.getWeatherToDB(Periods.NOW);

    }

    public void getCurrentWeather() throws IOException {
        weatherProvider.getWeather(Periods.NOW);
    }

    // Получаем погода на пять дней.
    public void getWeatherIn5Days()throws IOException {
        weatherProvider.getWeather(Periods.FIVE_DAYS);
    }
}
