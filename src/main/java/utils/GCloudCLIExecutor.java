package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

/**
 * Utility class for executing Google Cloud CLI commands.
 * This class detects the operating system and executes shell commands accordingly.
 * It captures both standard output and error output and includes timeout handling
 * to prevent long-running commands from stalling execution.
 *
 * @author Mor Hanania
 * @version 1.1
 * @since 2025-03-06
 */
public class GCloudCLIExecutor {

    private static final int COMMAND_TIMEOUT = 60; // Timeout in seconds

    /**
     * Executes a shell command using the appropriate OS-specific shell.
     * This method determines whether the system is Windows or Linux/macOS and
     * executes the command accordingly. The output is captured and returned.
     * If the command runs longer than the timeout period, it is terminated.
     *
     * @param command The command to be executed.
     * @return The trimmed output of the command execution.
     * @throws Exception If the command fails, times out, or returns a non-zero exit code.
     */
    public static String executeCommand(String command) throws Exception {
        ProcessBuilder builder;

        // Determine the correct shell based on the operating system
        if (isWindows()) {
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            builder = new ProcessBuilder("bash", "-c", command);
        }

        builder.redirectErrorStream(true); // Merge stdout and stderr

        Process process = builder.start();

        // Use a separate thread to read the process output
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executor.submit(() -> {
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            return output.toString().trim();
        });

        try {
            // Wait for command execution within timeout
            String output = outputFuture.get(COMMAND_TIMEOUT, TimeUnit.SECONDS);

            // Wait for process to finish and check exit code
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command failed: " + command +
                        "\nExit Code: " + exitCode + "\nOutput: " + output);
            }

            return output;
        } catch (TimeoutException e) {
            process.destroy(); // Kill process if timeout occurs
            throw new RuntimeException("Command timed out: " + command);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Determines if the system is running Windows.
     *
     * @return {@code true} if the operating system is Windows, otherwise {@code false}.
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
