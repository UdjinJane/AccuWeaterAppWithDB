

import enums.Periods;

import java.io.IOException;

// Прикручиваем интерфейс.
public interface WeatherProvider {

    void getWeather(Periods periods) throws IOException;

}
