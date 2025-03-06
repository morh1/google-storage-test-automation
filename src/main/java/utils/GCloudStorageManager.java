package utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GCloudStorageManager {
    private static final String PROJECT_FILE = "src/main/java/utils/project_id.txt"; // Read project ID from this file
    private static final String PROJECT_ID = readProjectId(); // Read dynamically
   private static final Storage storage = getStorageInstance();


    private static Storage getStorageInstance() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            return StorageOptions.newBuilder()
                    .setProjectId(PROJECT_ID)  // ✅ Connect to the correct project
                    .setCredentials(credentials)  // ✅ Use service account credentials
                    .build()
                    .getService();
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to initialize Google Cloud Storage: " + e.getMessage(), e);
        }
    }
    public static String readProjectId() {
        try {
            return new String(Files.readAllBytes(Paths.get(PROJECT_FILE))).trim();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read project ID", e);
        }
    }


    public static void createBucket(String bucketName) {

        // Create the bucket
        Bucket bucket = storage.create(BucketInfo.newBuilder(bucketName)
                .setStorageClass(StorageClass.STANDARD)
                .setLocation("us-central1")  // ✅ Ensure correct region
                .build());

        System.out.println("✅ Successfully created bucket: " + bucket.getName());
    }

    public static void uploadFile(String bucketName, String localFilePath, String destinationBlobName) throws Exception {

        if (!Files.exists(Paths.get(localFilePath))) {
            throw new RuntimeException("❌ Local file does not exist: " + localFilePath);
        }

        BlobId blobId = BlobId.of(bucketName, destinationBlobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        byte[] bytes = Files.readAllBytes(Paths.get(localFilePath));
        storage.create(blobInfo, bytes);

        System.out.println("✅ File uploaded to: gs://" + bucketName + "/" + destinationBlobName);
    }

    // ✅ Delete a file from the bucket
    public static boolean deleteFile(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            return blob.delete();
        }
        return false;
    }
    public static boolean fileExists(String bucketName, String fileName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(BlobId.of(bucketName, fileName));
        return blob != null && blob.exists(); // ✅ Returns true if file exists
    }
}
