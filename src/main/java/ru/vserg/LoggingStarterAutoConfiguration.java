package ru.vserg;

public class LoggingStarterAutoConfiguration {

    public static void println(String input) {
        System.out.println("Вызвано из gradle библиотеки: " + input);
    }
}