package testcases;

import commands.RmCommand;
import commands.RmCommandStatus;
import utils.*;

import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Paths;

/**
 * Unit tests for the {@link RmCommand} class.
 * This test suite verifies the functionality of the `gsutil rm` command execution,
 * ensuring that files are correctly deleted from Google Cloud Storage (GCS).
 * The tests include:
 * <ul>
 *     <li>Validating successful deletion of an existing file.</li>
 *     <li>Handling deletion of a non-existent file.</li>
 * </ul>
 *
 * Files are created locally, uploaded to GCS, tested, and then cleaned up.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class RmCommandTest {

    /** System temporary directory used for file creation. */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /** Paths for test files. */
    private static final String TEST_FILE_PATH_1 = Paths.get(TEMP_DIR, "testfile1.txt").toString();
    private static final String TEST_FILE_PATH_2 = Paths.get(TEMP_DIR, "testfile2.txt").toString();

    /** Unique bucket name for testing, appended with a timestamp to avoid conflicts. */
    private static final String BUCKET_NAME = "rm-bucket" + System.currentTimeMillis();

    /** Command instance for executing `gsutil rm`. */
    private RmCommand rmCommand;

    /** Executor for running Google Cloud CLI commands. */
    private GCloudCLIExecutor executor;

    /** Expected file sizes for test files (in bytes). */
    private static final int FILE1_SIZE = 1024;
    private static final int FILE2_SIZE = 2048;

    /**
     * Initializes the test environment before running any tests.
     * This method sets up the Google Cloud CLI executor, initializes the `RmCommand` instance,
     * and creates a dedicated GCS bucket for testing.
     */
    @BeforeClass
    public void setup() {
        executor = new GCloudCLIExecutor();
        rmCommand = new RmCommand(executor, BUCKET_NAME);
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
    public void createTestResources() throws Exception {
        // Create local test files
        FileCreator.createFile(TEST_FILE_PATH_1, FILE1_SIZE);
        FileCreator.createFile(TEST_FILE_PATH_2, FILE2_SIZE);

        // Upload files to GCS
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_1, "testfile1.txt");
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_2, "testfile2.txt");
    }

    /**
     * Tests successful deletion of a single file from Google Cloud Storage.
     * This test ensures that an existing file is correctly removed using `gsutil rm`.
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testDeleteSingleFile() throws Exception {
        String gcsFilePath = "gs://" + BUCKET_NAME + "/testfile1.txt";

        // Ensure the file exists before attempting deletion
        Assert.assertTrue(GCloudStorageManager.fileExists(BUCKET_NAME, "testfile1.txt"),
                "Test file does not exist before deletion!");

        // Execute file deletion
        RmCommandStatus status = rmCommand.execute(gcsFilePath);

        // Validate file deletion
        Assert.assertEquals(status, RmCommandStatus.CONTENT_OUTPUT, "File deletion failed!");
    }

    /**
     * Tests handling of deletion for a non-existent file.
     * This test ensures that attempting to delete a file that does not exist
     * correctly returns the appropriate status without throwing unexpected exceptions.
     *
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testDeleteNonExistentFile() throws Exception {
        String nonExistentFile = "gs://" + BUCKET_NAME + "/nonexistent-file.txt";

        // Attempt to delete a non-existent file
        RmCommandStatus status = rmCommand.execute(nonExistentFile);

        // Validate that the command correctly identifies a missing file
        Assert.assertEquals(status, RmCommandStatus.DOES_NOT_EXIST,
                "Expected FILE_NOT_FOUND but got: " + status);
    }

    /**
     * Cleans up local test files after each test method execution.
     * This method removes the temporary test files created for testing.
     *
     * @throws Exception If an error occurs during file deletion.
     */
    @AfterMethod
    public void cleanup() throws Exception {
        // Delete local test files
        new File(TEST_FILE_PATH_1).delete();
        new File(TEST_FILE_PATH_2).delete();
    }
}
