package com.zinoviev.conversion_microservice.core.util;

public class FileKeyUtils {

    // Приватный пустой конструктор для запрета создания экземпляров класса
    private FileKeyUtils() {}

    public static String parseFileNameWithoutExtension(String fileKey) {
        String fileName = parseFileNameWithExtension(fileKey);

        // Ищем последнюю точку в имени файла
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return ""; // Нет расширения или точка в конце
        }

        return fileName.substring(0, lastDot).toLowerCase();
    }

    public static String parseFileExtension(String fileKey) {

        String fileName = parseFileNameWithExtension(fileKey);

        // Ищем последнюю точку в имени файла
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return ""; // Нет расширения или точка в конце
        }

        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private static String parseFileNameWithExtension(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return "";
        }

        // Убираем возможный слеш в конце (если это "папка")
        String normalizedKey = fileKey.endsWith("/")
                ? fileKey.substring(0, fileKey.length() - 1)
                : fileKey;

        // Получаем имя файла (всё после последнего слеша)

        return normalizedKey.substring(
                normalizedKey.lastIndexOf('/') + 1);
    }

    public static String createFileKey(String directoryPath, String messageKey, String fileName, String fileExtension) {
        return String.join("", directoryPath, messageKey, "/", fileName, fileExtension);
    }
}
