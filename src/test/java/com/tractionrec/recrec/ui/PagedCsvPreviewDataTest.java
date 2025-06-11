package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.csv.CsvPreviewData;
import com.tractionrec.recrec.csv.CsvFileStats;
import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PagedCsvPreviewData, focusing on edge cases like empty data
 */
public class PagedCsvPreviewDataTest {

    @Test
    public void testEmptyDataPagination() {
        // Create empty CSV preview data
        String[][] emptyData = new String[0][0];
        String[] emptyHeaders = new String[0];
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats emptyStats = new CsvFileStats(0, 0, 0, 0, 0, "UTF-8", false);

        CsvPreviewData emptyPreview = new CsvPreviewData(emptyData, emptyHeaders, emptyIssues, emptyStats, 0);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(emptyPreview, 1000);

        // Test initial state
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(0, pagedData.getTotalPages());
        assertEquals(1000, pagedData.getPageSize());

        // Test navigation methods with empty data
        assertFalse(pagedData.nextPage(), "Should not be able to go to next page with empty data");
        assertFalse(pagedData.previousPage(), "Should not be able to go to previous page with empty data");
        assertFalse(pagedData.goToPage(0), "Should not be able to go to page 0 with empty data");
        assertFalse(pagedData.goToPage(1), "Should not be able to go to page 1 with empty data");
        assertFalse(pagedData.goToRow(0), "Should not be able to go to row 0 with empty data");

        // Test firstPage() - should be safe
        pagedData.firstPage();
        assertEquals(0, pagedData.getCurrentPage());

        // Test lastPage() - this is the main bug we're fixing
        pagedData.lastPage();
        // After fix, currentPage should remain 0 for empty data, not become -1
        assertEquals(0, pagedData.getCurrentPage(), "lastPage() should not set currentPage to -1 for empty data");

        // Test row calculations after lastPage()
        assertEquals(0, pagedData.getCurrentPageStartRow(), "Start row should be 0 for empty data");
        assertEquals(0, pagedData.getCurrentPageEndRow(), "End row should be 0 for empty data");
        assertEquals(0, pagedData.getCurrentPageRowCount(), "Row count should be 0 for empty data");

        // Test data access methods
        String[][] pageData = pagedData.getCurrentPageData();
        assertNotNull(pageData);
        assertEquals(0, pageData.length, "Page data should be empty array");

        // Test page info
        String pageInfo = pagedData.getPageInfo();
        assertNotNull(pageInfo);
        assertTrue(pageInfo.contains("0 rows"), "Page info should mention 0 rows");
    }

    @Test
    public void testSingleRowPagination() {
        // Create CSV with single row
        String[][] singleRowData = {{"value1", "value2"}};
        String[] headers = {"Header1", "Header2"};
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats stats = new CsvFileStats(1, 2, 1, 0, 100, "UTF-8", true);

        CsvPreviewData preview = new CsvPreviewData(singleRowData, headers, emptyIssues, stats, 1);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(preview, 1000);

        // Test initial state
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(1, pagedData.getTotalPages());

        // Test navigation
        assertFalse(pagedData.nextPage(), "Should not be able to go to next page with single page");
        assertFalse(pagedData.previousPage(), "Should not be able to go to previous page from page 0");
        assertTrue(pagedData.goToPage(0), "Should be able to go to page 0");
        assertFalse(pagedData.goToPage(1), "Should not be able to go to page 1 with single page");

        // Test firstPage() and lastPage()
        pagedData.firstPage();
        assertEquals(0, pagedData.getCurrentPage());

        pagedData.lastPage();
        assertEquals(0, pagedData.getCurrentPage(), "lastPage() should set currentPage to 0 for single page");

        // Test row calculations
        assertEquals(0, pagedData.getCurrentPageStartRow());
        assertEquals(1, pagedData.getCurrentPageEndRow());
        assertEquals(1, pagedData.getCurrentPageRowCount());
    }

    @Test
    public void testMultiPagePagination() {
        // Create CSV with multiple rows to test normal pagination
        String[][] multiRowData = new String[25][];
        for (int i = 0; i < 25; i++) {
            multiRowData[i] = new String[]{"value" + i + "_1", "value" + i + "_2"};
        }
        String[] headers = {"Header1", "Header2"};
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats stats = new CsvFileStats(25, 2, 25, 0, 1000, "UTF-8", true);

        CsvPreviewData preview = new CsvPreviewData(multiRowData, headers, emptyIssues, stats, 25);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(preview, 10); // 10 rows per page

        // Should have 3 pages (25 rows / 10 per page = 2.5 -> 3 pages)
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(3, pagedData.getTotalPages());

        // Test navigation to last page
        pagedData.lastPage();
        assertEquals(2, pagedData.getCurrentPage(), "lastPage() should set currentPage to 2 for 3 pages");

        // Test row calculations on last page
        assertEquals(20, pagedData.getCurrentPageStartRow()); // Page 2 starts at row 20
        assertEquals(25, pagedData.getCurrentPageEndRow());   // Ends at row 25
        assertEquals(5, pagedData.getCurrentPageRowCount());  // 5 rows on last page

        // Test firstPage()
        pagedData.firstPage();
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(0, pagedData.getCurrentPageStartRow());
        assertEquals(10, pagedData.getCurrentPageEndRow());
        assertEquals(10, pagedData.getCurrentPageRowCount());
    }

    @Test
    public void testEdgeCaseWithExactPageSize() {
        // Create CSV with exactly pageSize rows
        String[][] exactPageData = new String[10][];
        for (int i = 0; i < 10; i++) {
            exactPageData[i] = new String[]{"value" + i};
        }
        String[] headers = {"Header1"};
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats stats = new CsvFileStats(10, 1, 10, 0, 500, "UTF-8", true);

        CsvPreviewData preview = new CsvPreviewData(exactPageData, headers, emptyIssues, stats, 10);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(preview, 10); // Exactly 10 rows per page

        // Should have exactly 1 page
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(1, pagedData.getTotalPages());

        // Test lastPage()
        pagedData.lastPage();
        assertEquals(0, pagedData.getCurrentPage(), "lastPage() should set currentPage to 0 for single page");

        // Test row calculations
        assertEquals(0, pagedData.getCurrentPageStartRow());
        assertEquals(10, pagedData.getCurrentPageEndRow());
        assertEquals(10, pagedData.getCurrentPageRowCount());
    }

    @Test
    public void testArrayIndexBoundsAfterLastPageBug() {
        // This test specifically checks that we don't get ArrayIndexOutOfBoundsException
        // when accessing data after calling lastPage() on empty data

        String[][] emptyData = new String[0][0];
        String[] emptyHeaders = new String[0];
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats emptyStats = new CsvFileStats(0, 0, 0, 0, 0, "UTF-8", false);

        CsvPreviewData emptyPreview = new CsvPreviewData(emptyData, emptyHeaders, emptyIssues, emptyStats, 0);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(emptyPreview, 1000);

        // Call lastPage() which would set currentPage to -1 before the fix
        pagedData.lastPage();

        // These should not throw ArrayIndexOutOfBoundsException
        assertDoesNotThrow(() -> {
            int startRow = pagedData.getCurrentPageStartRow();
            assertTrue(startRow >= 0, "Start row should not be negative");
        });

        assertDoesNotThrow(() -> {
            int endRow = pagedData.getCurrentPageEndRow();
            assertTrue(endRow >= 0, "End row should not be negative");
        });

        assertDoesNotThrow(() -> {
            int rowCount = pagedData.getCurrentPageRowCount();
            assertTrue(rowCount >= 0, "Row count should not be negative");
        });

        assertDoesNotThrow(() -> {
            String[][] pageData = pagedData.getCurrentPageData();
            assertNotNull(pageData);
            assertEquals(0, pageData.length);
        });
    }

    @Test
    public void testIntegrationWithCsvValidationService() {
        // This test simulates the real-world scenario where CsvValidationService
        // generates an empty preview (e.g., from a CSV with only headers or completely empty file)
        // and then PagedCsvPreviewData is used in the UI

        // Simulate what CsvValidationService.generatePreview() returns for empty data
        String[][] emptyData = new String[0][0];
        String[] headers = {"Header1", "Header2"}; // Headers might exist even with no data
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats stats = new CsvFileStats(0, 2, 0, 0, 50, "UTF-8", true); // Has headers but no data

        CsvPreviewData preview = new CsvPreviewData(emptyData, headers, emptyIssues, stats, 0);
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(preview, 1000);

        // Simulate user interactions that could trigger the bug

        // 1. User clicks "Last Page" button in UI
        pagedData.lastPage();
        assertEquals(0, pagedData.getCurrentPage(), "Should stay on page 0 for empty data");

        // 2. UI tries to get page info for display
        String pageInfo = pagedData.getPageInfo();
        assertNotNull(pageInfo);
        assertTrue(pageInfo.contains("0 rows"), "Page info should show 0 rows");

        // 3. UI tries to get current page data for table display
        String[][] pageData = pagedData.getCurrentPageData();
        assertNotNull(pageData);
        assertEquals(0, pageData.length, "Should return empty array for page data");

        // 4. UI tries to get row count for table model
        assertEquals(0, pagedData.getCurrentPageRowCount(), "Row count should be 0");

        // 5. User tries to navigate to specific page
        assertFalse(pagedData.goToPage(0), "Should not allow navigation to any page with empty data");
        assertFalse(pagedData.goToRow(0), "Should not allow navigation to any row with empty data");

        // 6. Headers should still be available even with empty data
        String[] retrievedHeaders = pagedData.getHeaders();
        assertNotNull(retrievedHeaders);
        assertEquals(2, retrievedHeaders.length);
        assertEquals("Header1", retrievedHeaders[0]);
        assertEquals("Header2", retrievedHeaders[1]);

        // 7. Column count should reflect headers even with no data
        assertEquals(2, pagedData.getColumnCount());
    }

    @Test
    public void testRealWorldEmptyFileScenario() {
        // Test the exact scenario that would cause the original bug:
        // Empty CSV file processed by CsvValidationService

        // This simulates CsvValidationService.generatePreview() returning empty data
        // when processing a completely empty CSV file or CSV with only whitespace
        String[][] emptyData = new String[0][0];
        String[] emptyHeaders = new String[0];
        Map<CellLocation, ValidationIssue> emptyIssues = new HashMap<>();
        CsvFileStats emptyStats = new CsvFileStats(0, 0, 0, 0, 0, "UTF-8", false);

        CsvPreviewData emptyPreview = new CsvPreviewData(emptyData, emptyHeaders, emptyIssues, emptyStats, 0);

        // This is how CsvPreviewWindow creates PagedCsvPreviewData
        PagedCsvPreviewData pagedData = new PagedCsvPreviewData(emptyPreview, 1000); // ROWS_PER_PAGE = 1000

        // Simulate the sequence of operations that would happen in the UI:

        // 1. Window loads and displays initial state
        assertEquals(0, pagedData.getCurrentPage());
        assertEquals(0, pagedData.getTotalPages());

        // 2. User clicks pagination buttons (these should all be disabled, but let's test safety)
        pagedData.firstPage(); // Should be safe
        assertEquals(0, pagedData.getCurrentPage());

        pagedData.lastPage(); // This was the bug - would set currentPage to -1
        assertEquals(0, pagedData.getCurrentPage(), "lastPage() should not cause negative currentPage");

        // 3. UI refreshes page display - these operations should not throw exceptions
        assertDoesNotThrow(() -> {
            String pageInfo = pagedData.getPageInfo();
            String[][] data = pagedData.getCurrentPageData();
            int rowCount = pagedData.getCurrentPageRowCount();
            int startRow = pagedData.getCurrentPageStartRow();
            int endRow = pagedData.getCurrentPageEndRow();

            // All values should be non-negative and consistent
            assertTrue(startRow >= 0, "Start row should not be negative");
            assertTrue(endRow >= 0, "End row should not be negative");
            assertTrue(rowCount >= 0, "Row count should not be negative");
            assertEquals(0, rowCount, "Row count should be 0 for empty data");
            assertEquals(startRow, endRow, "Start and end row should be equal for empty data");
        });
    }
}
