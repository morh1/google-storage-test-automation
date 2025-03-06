package testcases;

import commands.CatCommand;
import utils.*;

import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Paths;

/**
 * Unit tests for the {@link CatCommand} class.
 * This test suite verifies the functionality of the `gsutil cat` command execution,
 * ensuring that file content retrieval from Google Cloud Storage (GCS) works correctly.

 * The tests include:
 * <ul>
 *     <li>Validating retrieval of a single file's content.</li>
 *     <li>Validating concatenation of multiple files' contents.</li>
 * </ul>
 *
 * Files are created locally, uploaded to GCS, tested, and then cleaned up.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class CatCommandTest {

    /** System temporary directory used for file creation. */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /** Paths for test files. */
    private static final String TEST_FILE_PATH_1 = Paths.get(TEMP_DIR, "testfile1.txt").toString();
    private static final String TEST_FILE_PATH_2 = Paths.get(TEMP_DIR, "testfile2.txt").toString();

    /** Unique bucket name for testing, appended with a timestamp to avoid conflicts. */
    private static final String BUCKET_NAME = "cat-bucket" + System.currentTimeMillis();

    /** Command instance for executing `gsutil cat`. */
    private CatCommand catCommand;

    /** Executor for running Google Cloud CLI commands. */
    private GCloudCLIExecutor executor;

    /** Sample content for test files. */
    private final String FILE1_CONTENT = "Hello, this is file1.";
    private final String FILE2_CONTENT = "This is file2 content.";

    /**
     * Initializes the test environment before running any tests.
     * This method sets up the Google Cloud CLI executor, initializes the `CatCommand` instance,
     * and creates a dedicated GCS bucket for testing.
     */
    @BeforeClass
    public void setup() {
        executor = new GCloudCLIExecutor();
        catCommand = new CatCommand(executor, BUCKET_NAME);
        GCloudStorageManager.createBucket(BUCKET_NAME);
    }

    /**
     * Prepares test files before each test method runs.
     * This method:
     * <ul>
     *     <li>Creates local test files.</li>
     *     <li>Uploads them to the GCS bucket.</li>
     * </ul>
     *
     * @throws Exception If an error occurs during file creation or upload.
     */
    @BeforeMethod
    public void createTestFiles() throws Exception {
        // Create local test files
        FileCreator.createFileConttent(TEST_FILE_PATH_1, FILE1_CONTENT);
        FileCreator.createFileConttent(TEST_FILE_PATH_2, FILE2_CONTENT);

        // Upload files to GCS
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_1, "testfile1.txt");
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH_2, "testfile2.txt");
    }

    /**
     * Tests retrieving the content of a single file using `gsutil cat`.
     * This test verifies that the file's content is correctly retrieved from GCS.
     *
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testCatSingleFile() throws Exception {
        String gcsFilePath = "gs://" + BUCKET_NAME + "/testfile1.txt";

        // Execute the command
        String fileContent = catCommand.execute(gcsFilePath);

        // Validate that content is not null
        Assert.assertNotNull(fileContent, "File content should not be null for a single file");

        // Verify the retrieved content
        Assert.assertEquals(fileContent.trim(), FILE1_CONTENT, "File content mismatch for single file");
    }

    /**
     * Tests retrieving the content of multiple files using `gsutil cat`.
     * This test ensures that content from multiple files is correctly concatenated.
     *
     * @throws Exception If an error occurs during command execution.
     */
    @Test
    public void testCatMultipleFiles() throws Exception {
        String gcsFilePath1 = "gs://" + BUCKET_NAME + "/testfile1.txt";
        String gcsFilePath2 = "gs://" + BUCKET_NAME + "/testfile2.txt";

        // Execute the command for multiple files
        String output = catCommand.execute(gcsFilePath1, gcsFilePath2);

        // Validate that content is not null
        Assert.assertNotNull(output, "File content should not be null for multiple files");

        // Verify concatenated content
        String expectedContent = FILE1_CONTENT + FILE2_CONTENT;
        Assert.assertEquals(output.trim(), expectedContent, "File content mismatch for multiple files");
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
