package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test export naming functionality for RecRecResultsPreview
 */
public class ExportNamingTest {

    @TempDir
    Path tempDir;

    @Test
    public void testGetDefaultExportFileName_WithInputFile() throws Exception {
        // Create a temporary input file
        File inputFile = tempDir.resolve("2025-06-09 Customer query by token Active Accounts.csv").toFile();
        inputFile.createNewFile();

        // Create state with input file
        RecRecState state = new RecRecState();
        state.inputFile = inputFile;
        state.queryMode = QueryBy.RECORD_ID;

        // Create RecRecResultsPreview instance
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("getDefaultExportFileName");
        method.setAccessible(true);
        String result = (String) method.invoke(preview);

        // Verify the result
        assertEquals("2025-06-09 Customer query by token Active Accounts - results.csv", result);
    }

    @Test
    public void testGetDefaultExportFileName_WithoutInputFile() throws Exception {
        // Create state without input file (ad-hoc query scenario)
        RecRecState state = new RecRecState();
        state.inputFile = null;
        state.queryMode = QueryBy.ADHOC_SEARCH;

        // Create RecRecResultsPreview instance
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("getDefaultExportFileName");
        method.setAccessible(true);
        String result = (String) method.invoke(preview);

        // Verify the result follows timestamp pattern
        assertTrue(result.startsWith("ad-hoc_search_results_"));
        assertTrue(result.endsWith(".csv"));
        assertTrue(result.contains("_"));
    }

    @Test
    public void testGetDefaultExportFileName_FileWithoutExtension() throws Exception {
        // Create a temporary input file without extension
        File inputFile = tempDir.resolve("test-file-no-extension").toFile();
        inputFile.createNewFile();

        // Create state with input file
        RecRecState state = new RecRecState();
        state.inputFile = inputFile;
        state.queryMode = QueryBy.RECORD_ID;

        // Create RecRecResultsPreview instance
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("getDefaultExportFileName");
        method.setAccessible(true);
        String result = (String) method.invoke(preview);

        // Verify the result
        assertEquals("test-file-no-extension - results.csv", result);
    }

    @Test
    public void testResolveFileNameCollision_NoCollision() throws Exception {
        // Create a file that doesn't exist
        File nonExistentFile = tempDir.resolve("test-file.csv").toFile();

        // Create RecRecResultsPreview instance
        RecRecState state = new RecRecState();
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("resolveFileNameCollision", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(preview, nonExistentFile);

        // Should return the same file since no collision
        assertEquals(nonExistentFile.getAbsolutePath(), result.getAbsolutePath());
    }

    @Test
    public void testResolveFileNameCollision_WithCollision() throws Exception {
        // Create existing files to simulate collision
        File existingFile = tempDir.resolve("test-file.csv").toFile();
        existingFile.createNewFile();

        File collision1 = tempDir.resolve("test-file (1).csv").toFile();
        collision1.createNewFile();

        // Create RecRecResultsPreview instance
        RecRecState state = new RecRecState();
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("resolveFileNameCollision", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(preview, existingFile);

        // Should return the next available name
        assertEquals(tempDir.resolve("test-file (2).csv").toFile().getAbsolutePath(), result.getAbsolutePath());
        assertFalse(result.exists()); // The resolved file should not exist yet
    }

    @Test
    public void testResolveFileNameCollision_FileWithoutExtension() throws Exception {
        // Create existing file without extension
        File existingFile = tempDir.resolve("test-file").toFile();
        existingFile.createNewFile();

        // Create RecRecResultsPreview instance
        RecRecState state = new RecRecState();
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("resolveFileNameCollision", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(preview, existingFile);

        // Should return the next available name without extension
        assertEquals(tempDir.resolve("test-file (1)").toFile().getAbsolutePath(), result.getAbsolutePath());
        assertFalse(result.exists()); // The resolved file should not exist yet
    }

    @Test
    public void testCsvExtensionPreservation() throws Exception {
        // Test CSV extension preservation and edge cases
        String[][] testCases = {
            {"data.csv", "data - results.csv"},
            {"file.data.csv", "file.data - results.csv"}, // Multiple dots
            {"no-extension", "no-extension - results.csv"} // No extension gets .csv
        };

        for (String[] testCase : testCases) {
            String inputFileName = testCase[0];
            String expectedOutput = testCase[1];

            // Create temporary input file
            File inputFile = tempDir.resolve(inputFileName).toFile();
            inputFile.createNewFile();

            // Create state with input file
            RecRecState state = new RecRecState();
            state.inputFile = inputFile;
            state.queryMode = QueryBy.RECORD_ID;

            // Create RecRecResultsPreview instance
            RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

            // Use reflection to access private method
            Method method = RecRecResultsPreview.class.getDeclaredMethod("getDefaultExportFileName");
            method.setAccessible(true);
            String result = (String) method.invoke(preview);

            // Verify the result
            assertEquals(expectedOutput, result,
                "Failed for input: " + inputFileName + ", expected: " + expectedOutput + ", got: " + result);

            // Clean up
            inputFile.delete();
        }
    }

    @Test
    public void testResolveFileNameCollision_MultipleCollisions() throws Exception {
        // Create multiple existing files to simulate multiple collisions
        File existingFile = tempDir.resolve("popular-name.csv").toFile();
        existingFile.createNewFile();

        for (int i = 1; i <= 5; i++) {
            File collision = tempDir.resolve("popular-name (" + i + ").csv").toFile();
            collision.createNewFile();
        }

        // Create RecRecResultsPreview instance
        RecRecState state = new RecRecState();
        RecRecResultsPreview preview = new RecRecResultsPreview(state, null);

        // Use reflection to access private method
        Method method = RecRecResultsPreview.class.getDeclaredMethod("resolveFileNameCollision", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(preview, existingFile);

        // Should return the next available name
        assertEquals(tempDir.resolve("popular-name (6).csv").toFile().getAbsolutePath(), result.getAbsolutePath());
        assertFalse(result.exists()); // The resolved file should not exist yet
    }
}
