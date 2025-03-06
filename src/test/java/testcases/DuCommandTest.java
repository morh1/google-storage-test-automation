package testcases;

import commands.DuCommand;
import utils.*;

import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unit tests for the {@link DuCommand} class.
 * This test suite verifies the functionality of the `gsutil du` command execution,
 * ensuring that file sizes are correctly retrieved from Google Cloud Storage (GCS).
 * The tests include:
 * <ul>
 *     <li>Validating file size retrieval for a single file.</li>
 *     <li>Validating total file size calculation for multiple files in a GCS bucket.</li>
 * </ul>
 *
 * Files are created locally, uploaded to GCS, tested, and then cleaned up.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class DuCommandTest {

    /** System temporary directory used for file creation. */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /** Paths for test files. */
    private static final String TEST_FILE_PATH_1 = Paths.get(TEMP_DIR, "testfile1.txt").toString();
    private static final String TEST_FILE_PATH_2 = Paths.get(TEMP_DIR, "testfile2.txt").toString();

    /** Unique bucket name for testing, appended with a timestamp to avoid conflicts. */
    private static final String BUCKET_NAME = "du-bucket" + System.currentTimeMillis();

    /** Command instance for executing `gsutil du`. */
    private DuCommand duCommand;

    /** Executor for running Google Cloud CLI commands. */
    private GCloudCLIExecutor executor;

    /** Expected file sizes for test files (in bytes). */
    private static final int FILE1_SIZE = 1024;
    private static final int FILE2_SIZE = 2048;

    /**
     * Initializes the test environment before running any tests.
     * This method sets up the Google Cloud CLI executor, initializes the `DuCommand` instance,
     * and creates a dedicated GCS bucket for testing.
     */
    @BeforeClass
    public void setup() {
        executor = new GCloudCLIExecutor();
        duCommand = new DuCommand(executor, BUCKET_NAME);
        GCloudStorageManager.createBucket(BUCKET_NAME);
    }

    /**
     * Prepares test files before each test method runs.
     * This method:
     * <ul>
     *     <li>Creates local test files with predefined sizes.</li>
     *     <li>Uploads them to the GCS bucket.</li>
     * </ul>
     *
     * @throws Exception If an error occurs during file creation or upload.
     */
    @BeforeMethod
    public void createTestFiles() throws Exception {
        // Create local test files
        FileCreator.createFile(TEST_FILE_PATH_1, FILE1_SIZE);
        FileCreator.createFile(TEST_FILE_PATH_2, FILE2_SIZE);

        // Upload files to GCS
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_1, "testfile1.txt");
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_2, "testfile2.txt");
    }

    /**
     * Tests retrieving the file size of a single file using `gsutil du`.
     * This test verifies that the file size is correctly retrieved from GCS.
     *
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testDuSingleFile() throws Exception {
        String gcsFilePath = "gs://" + BUCKET_NAME + "/testfile1.txt";

        // Execute the command
        Map<String, String> fileSizeMap = duCommand.execute(gcsFilePath);

        // Validate that file size map is not null
        Assert.assertNotNull(fileSizeMap, "File size map should not be null for a single file");

        // Ensure the file path is present in the map
        Assert.assertTrue(fileSizeMap.containsKey(gcsFilePath), "File path should be in the map");

        // Get reported file size
        String reportedSize = fileSizeMap.get(gcsFilePath);
        Assert.assertNotNull(reportedSize, "Reported size should not be null");

        // Parse and validate file size
        int reportedSizeInt = Integer.parseInt(reportedSize.trim());
        Assert.assertEquals(reportedSizeInt, FILE1_SIZE, "Size mismatch for single file");
    }

    /**
     * Tests retrieving the total file size of multiple files using `gsutil du -s`.
     * This test ensures that the total size of all files in the bucket is correctly calculated.
     *
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testDuMultipleFiles() throws Exception {
        String gcsBucketPath = "gs://" + BUCKET_NAME;

        // Execute the command for total size calculation
        Map<String, String> fileSizeMap = duCommand.execute("-s " + gcsBucketPath);

        // Normalize and validate keys in the file size map
        Set<String> normalizedKeys = fileSizeMap.keySet().stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        Assert.assertNotNull(fileSizeMap, "File size map should not be null");

        // Ensure bucket path is found in the map
        Assert.assertTrue(normalizedKeys.contains(gcsBucketPath),
                "File path should be in the map, found: " + normalizedKeys);

        // Retrieve and validate total file size
        String reportedSize = fileSizeMap.getOrDefault(gcsBucketPath, "").trim();
        Assert.assertNotNull(reportedSize, "Reported size should not be null");

        int reportedSizeInt = Integer.parseInt(reportedSize);
        int expectedTotalSize = FILE1_SIZE + FILE2_SIZE;

        Assert.assertEquals(reportedSizeInt, expectedTotalSize,
                "Size mismatch. Expected: " + expectedTotalSize + ", Actual: " + reportedSizeInt);
    }

    /**
     * Cleans up test files after each test method execution.
     * This method removes:
     * <ul>
     *     <li>Local temporary test files.</li>
     *     <li>Uploaded files from the GCS bucket.</li>
     * </ul>
     *
     * @throws Exception If an error occurs during file deletion.
     */
    @AfterMethod
    public void cleanup() throws Exception {
        new File(TEST_FILE_PATH_1).delete();
        new File(TEST_FILE_PATH_2).delete();

        // Remove files from Google Cloud Storage
        GCloudStorageManager.deleteFile(BUCKET_NAME, "testfile1.txt");
        GCloudStorageManager.deleteFile(BUCKET_NAME, "testfile2.txt");
    }
}
