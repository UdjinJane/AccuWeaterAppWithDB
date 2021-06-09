

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import database.DataBaseOperation;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import enums.Periods;
import watherresponse.WeatherResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// В класс имплементирован интерфейс WeatherProvider.
public class AccuWeatherProvider implements WeatherProvider, DataBaseOperation {

    private static final String BASE_HOST = "dataservice.accuweather.com";
    private static final String FORECAST_ENDPOINT = "forecasts";
    private static final String CURRENT_CONDITIONS_ENDPOINT = "currentconditions";
    private static final String SEGMENT_DAY = "daily";
    private static final String SEGMENT_QTY = "5day";
    private static final String API_VERSION = "v1";
    private static final String API_KEY = ApplicationGlobalState.getInstance().getApiKey();

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void getWeatherToDB(Periods periods) throws IOException, SQLException {

        DatabaseSqliteImpl databaseSqliteImpl = new DatabaseSqliteImpl();
        String cityKey = detectCityKey();
        HttpUrl url = new HttpUrl.Builder().scheme("http")
                .host(BASE_HOST)
                .addPathSegment(CURRENT_CONDITIONS_ENDPOINT)
                .addPathSegment(API_VERSION)
                .addPathSegment(cityKey)
                .addQueryParameter("apikey", API_KEY)
                .build();
        Request request = new Request.Builder()
                .addHeader("accept", "application/json")
                .url(url)
                .build();
        Response response = client.newCall(request).execute();

        String jsonResponse = response.body().string();
        // System.out.println(jsonResponse);
        // Делаем юзерфрендли вывод.
        if (objectMapper.readTree(jsonResponse).size() > 0) {
            String dateTime = objectMapper.readTree(jsonResponse).get(0).at("/LocalObservationDateTime").asText();
            String weatherText = objectMapper.readTree(jsonResponse).get(0).at("/WeatherText").asText();
            double temperature = objectMapper.readTree(jsonResponse).get(0).at("/Temperature/Metric/Value").asDouble();
            String temperatureUnit = objectMapper.readTree(jsonResponse).get(0).at("/Temperature/Metric/Unit").asText();
            System.out.println("Погода на дату: " + dateTime + "\n"
                    + "На улице: " + weatherText + "\n"
                    + "Температура: " + temperature + temperatureUnit);
            // Работаем с базой данных.
            // Создаем новую таблицу, если старая отсутствует.

            databaseSqliteImpl.createTableIfNotExists();
            databaseSqliteImpl.saveDataToTable(ApplicationGlobalState.getInstance().getSelectedCity(), dateTime, weatherText, temperature);

            System.out.println("Занесено в базу данных");
            databaseSqliteImpl.selectAll();


            // Работаем с базой данных\.

        }  else throw new IOException("Сервер не вернул данных");

        System.out.println("Программа заканчивает работу.");
        System.exit(0);

    }

    @Override
    public void getWeather(Periods periods) throws IOException {
        String cityKey = detectCityKey();

// Свичум по варианту ответа на вопрос "текщая погода или на пять дней".
      switch (periods){
          case NOW:
              HttpUrl url = new HttpUrl.Builder().scheme("http")
                  .host(BASE_HOST)
                  .addPathSegment(CURRENT_CONDITIONS_ENDPOINT)
                  .addPathSegment(API_VERSION)
                  .addPathSegment(cityKey)
                  .addQueryParameter("apikey", API_KEY)
                  .build();
              Request request = new Request.Builder()
                      .addHeader("accept", "application/json")
                      .url(url)
                      .build();
              Response response = client.newCall(request).execute();

              String jsonResponse = response.body().string();
              // System.out.println(jsonResponse);
              // Делаем юзерфрендли вывод.
              if (objectMapper.readTree(jsonResponse).size() > 0) {
                  String dateTime = objectMapper.readTree(jsonResponse).get(0).at("/LocalObservationDateTime").asText();
                  String weatherText = objectMapper.readTree(jsonResponse).get(0).at("/WeatherText").asText();
                  String temperature = objectMapper.readTree(jsonResponse).get(0).at("/Temperature/Metric/Value").asText();
                  String temperatureUnit = objectMapper.readTree(jsonResponse).get(0).at("/Temperature/Metric/Unit").asText();
                  System.out.println("Погода на дату: " + dateTime + "\n"
                            + "На улице: " + weatherText + "\n"
                            + "Температура: " + temperature + temperatureUnit);
              }  else throw new IOException("Сервер не вернул данных");
              System.out.println("Программа заканчивает работу.");


              // Создать класс WeatherResponse, десериализовать ответ сервера в экземпляр класса
              // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Заглушка ошибок.
              // Пишем в файл.
              // System.out.println("Пишем в файл респонс сервера.");

              System.out.println(jsonResponse);
              System.out.println("Провожу десерелизацию в класс WeatherResponse");

              WeatherResponse[] weatherResponse = objectMapper.readValue(jsonResponse, WeatherResponse[].class);

              // Закомментированные ниже методы также успешно отрабатывают.
              // List<WeatherResponse> weatherResponse = objectMapper.readValue(jsonResponse, new TypeReference<List<WeatherResponse>>() {});
              // List<WeatherResponse> weatherResponse = objectMapper.readValue(jsonResponse,objectMapper.getTypeFactory().constructCollectionType(List.class, WeatherResponse.class));
              // System.out.println(weatherResponse.toString());
              // Контролька в файл.
               objectMapper.writeValue(new File("weatherJson.json"),  weatherResponse );
               System.out.println("Программа заканчивает работу.");
               System.exit(0);



        // На пять дней. Урл имеет другие сегменты!!!
          case FIVE_DAYS:
              HttpUrl urlFive = new HttpUrl.Builder().scheme("http")
                      .host(BASE_HOST)
                      .addPathSegment(FORECAST_ENDPOINT)
                      .addPathSegment(API_VERSION)
                      .addPathSegment(SEGMENT_DAY)
                      .addPathSegment(SEGMENT_QTY)
                      .addPathSegment(cityKey)
                      .addQueryParameter("apikey", API_KEY)
                      .build();
              Request requestFive = new Request.Builder()
                      .addHeader("accept", "application/json")
                      .url(urlFive)
                      .build();
              Response responseFive = client.newCall(requestFive).execute();


              String jsonResponseFive = responseFive.body().string();
              // По скольку на пять дней, то цикл 0-5.
              // Делаем юзерфрендли вывод.
              for (int i=0; i<5; i++) {

                  if (objectMapper.readTree(jsonResponseFive).size() > 0){
                      String dateTime = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Date").asText();
                      String tempMin = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Temperature/Minimum/Value").asText();
                      String tempMax = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Temperature/Maximum/Value").asText();
                      String tempUnit = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Temperature/Maximum/Unit").asText();
                      String dateDay = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Day/IconPhrase").asText();
                      String dateNight = objectMapper.readTree(jsonResponseFive).at("/DailyForecasts").get(i).at("/Night/IconPhrase").asText();
                      System.out.println("---------------- " + (i+1) + " день ----------------");

                      System.out.println("Погода на дату: " + dateTime + "\n"
                                + "Температура в диапазоне: " + tempMin + " - " + tempMax + " (" + tempUnit + ")\n"
                                + "Погодные явления: " + "Днем - " + dateDay + ", Ночью - " + dateNight
                      );

                  } else throw new IOException("Сервер не вернул данных на " + i + " день");

          }

//            System.out.println("Провожу десерелизацию в класс WeatherResponse");
//            //  Создать класс WeatherResponse, десериализовать ответ сервера в экземпляр класса
//            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            WeatherResponse weatherResponse = objectMapper.readValue(jsonResponseFive, WeatherResponse.class);
//            System.out.println(weatherResponse.toString());
//            System.out.println("Программа заканчивает работу.");
//            System.exit(0);
     }


        }



    // Защита от некорректного города.И формируем красивый заголовок.
    public String detectCityKey() throws IOException {
        String selectedCity = ApplicationGlobalState.getInstance().getSelectedCity();

        HttpUrl detectLocationURL = new HttpUrl.Builder()
            .scheme("http")
            .host(BASE_HOST)
            .addPathSegment("locations")
            .addPathSegment(API_VERSION)
            .addPathSegment("cities")
            .addPathSegment("autocomplete")
            .addQueryParameter("apikey", API_KEY)
            .addQueryParameter("q", selectedCity)
            .build();

        Request request = new Request.Builder()
            .addHeader("accept", "application/json")
            .url(detectLocationURL)
            .build();
        // Спрашиваем про наш город.
        Response response = client.newCall(request).execute();


        // Если ответ не суксесс, выкидаываем экс.
        if (!response.isSuccessful()) {
            throw new IOException("Невозможно прочесть информацию о городе. " +
                "Код ответа сервера = " + response.code() + " тело ответа = " + response.body().string());
        }
        String jsonResponse = response.body().string();
        System.out.println("Произвожу поиск города " + selectedCity);

        if (objectMapper.readTree(jsonResponse).size() > 0) {
            String cityName = objectMapper.readTree(jsonResponse).get(0).at("/LocalizedName").asText();
            String countryName = objectMapper.readTree(jsonResponse).get(0).at("/Country/LocalizedName").asText();
            System.out.println("Найден город " + cityName + " в стране " + countryName);
        } else throw new IOException("Server returns 0 cities");

        return objectMapper.readTree(jsonResponse).get(0).at("/Key").asText();
    }
}
