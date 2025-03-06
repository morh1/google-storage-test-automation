package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
/**
 * Utility class for creating and managing files.
 * This class provides methods to create empty files of a specified size
 * and create text files with predefined content.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class FileCreator {

    public static File createFile(String path, int sizeInBytes) throws IOException {
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = new byte[sizeInBytes];
            fos.write(data);
        }
        return file;
    }
    public static void createFileConttent(String filePath, String content) throws IOException {
        Files.createDirectories(Paths.get(filePath).getParent()); // Ensure the directory exists
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
