

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Userinterface {

    private final Controller controller = new Controller();

    public void runApplication() {
        Scanner scanner = new Scanner(System.in);
        // Бесконечный цикл, до выхода из программы.
        while (true) {
            // Играюсь с цветом вывода в коносль. (char) 27 + "[31mWarning! " + (char)27 + "[0m"
            // "[35mWarning!". Текст будет пурпурным.
            //30 - черный. 31 - красный. 32 - зеленый. 33 - желтый. 34 - синий. 35 - пурпурный. 36 - голубой. 37 - белый.
            System.out.println((char) 27 + "[34m" + "Введите название города на английском языке" + (char)27 + "[0m");
            String city = scanner.nextLine();

            // Устанавливаем глобальные переменные в инстансе.
            setGlobalCity(city);

            System.out.println("Введите ответ: 1 - Получить текущую погоду, " +
                "2 - Получить погоду на следующие 5 дней, " + "3 - Получить текущую погоду и записать данные в базу; " +
                "Выход (exit) - завершить работу");
            String result = scanner.nextLine();

            // Проверка на выход из приложения.
            checkIsExit(result);

            // Проверка того, что введено.
            try {
                validateUserInput(result);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }


            // Если все хорошо, отправляем введенное в контроллер.
            try {
                notifyController(result);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private void checkIsExit(String result) {
        if (result.toLowerCase().equals("выход") || result.toLowerCase().equals("exit")) {
            System.out.println("Завершаю работу");
            // Статус код в 0. Вышли из приложения.
            System.exit(0);
        }
    }

    // Глобальный город. Перекидываем в переменную выбранный город.
    private void setGlobalCity(String city) {
        ApplicationGlobalState.getInstance().setSelectedCity(city);
    }

    // Валидация введенных данных.
    private void validateUserInput(String userInput) throws IOException {
        // Если ничего не ввели, или ввели более одного символа.
        if (userInput == null || userInput.length() != 1) {
            throw new IOException("Incorrect user input: expected one digit as answer, but actually get " + userInput);
        }

        int answer = 0;
        try {
            // Парсим на интегеры то, что введено.
            answer = Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            throw new IOException("Incorrect user input: character is not numeric!");
        }
    }


    private void notifyController(String input) throws IOException, SQLException {
        controller.onUserInput(input);
    }

}
