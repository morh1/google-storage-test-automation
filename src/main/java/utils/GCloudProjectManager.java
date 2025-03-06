package utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.FileWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import utils.BillingManager;

public class GCloudProjectManager {

    private static final String PROJECT_FILE = "src/main/java/utils/project_id.txt"; // Store project ID

    // ✅ Create a project (No need for billing link)
    public static void main(String[] args) {
        int randomNum = new Random().nextInt(900000) + 100000; // Ensures a 6-digit number
        String projectId = "gcs-project-" + randomNum; // Example: "java-gcs-123456"
        String projectName = "Test";

        //createProject(projectId,projectName);
        // Link project to billing
        BillingManager.configureProjectBilling("extreme-gecko-452717-v6");

        File file = new File(PROJECT_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("extreme-gecko-452717-v6");
                writer.flush();
                String content = new String(Files.readAllBytes(Paths.get(PROJECT_FILE)));
                System.out.println("File Content: " + content);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save project ID", e);
            }

    }

    public static void createProject(String projectId, String projectName) {
        try {

            // 🔹 Create project using gcloud CLI (No organization required)
            String createProjectCmd = "gcloud projects create " + projectId + " --name=\"" + projectName + "\"";
            GCloudCLIExecutor.executeCommand(createProjectCmd);

            System.out.println("✅ Project created successfully: " + projectId);

        } catch (Exception e) {
            System.err.println("❌ Failed to create project: " + e.getMessage());
        }

    }

}
