package com.tractionrec.recrec.csv;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.tractionrec.recrec.csv.rules.ContentValidationRule;
import com.tractionrec.recrec.csv.rules.EmptyColumnRule;
import com.tractionrec.recrec.csv.rules.ScientificNotationRule;
import com.tractionrec.recrec.csv.rules.ValidationRule;
import com.tractionrec.recrec.domain.CellLocation;
import com.tractionrec.recrec.domain.ValidationStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Service for validating CSV files and generating preview data
 */
public class CsvValidationService {

    private static final int MAX_PREVIEW_ROWS = 100;
    private static final long MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024; // 100MB

    private final List<ValidationRule> validationRules;

    public CsvValidationService() {
        this.validationRules = Arrays.asList(
            new ScientificNotationRule(),
            new ContentValidationRule(),
            new EmptyColumnRule()
        );
    }

    /**
     * Validate a CSV file and return comprehensive results
     */
    public CsvValidationResult validateCsv(File csvFile) {
        try {
            // Basic file checks
            if (!csvFile.exists() || !csvFile.canRead()) {
                return CsvValidationResult.invalid("File does not exist or cannot be read");
            }

            if (csvFile.length() > MAX_FILE_SIZE_BYTES) {
                return CsvValidationResult.invalid("File is too large (max 100MB)");
            }

            // Detect encoding
            String encoding = detectEncoding(csvFile);

            // Parse CSV
            ParsedCsvData parsedData = parseCsvFile(csvFile, encoding);

            // Generate file stats
            CsvFileStats stats = generateStats(csvFile, parsedData, encoding);

            // Run validation rules
            List<ValidationIssue> allIssues = new ArrayList<>();
            ScientificNotationRule.ScientificNotationAnalysis patternAnalysis = null;

            for (ValidationRule rule : validationRules) {
                List<ValidationIssue> ruleIssues = rule.validate(parsedData.data, parsedData.headers);
                allIssues.addAll(ruleIssues);

                // Run pattern analysis for scientific notation rule
                if (rule instanceof ScientificNotationRule scientificRule) {
                    patternAnalysis = scientificRule.analyzePatterns(parsedData.data, parsedData.headers);
                }
            }

            // Determine overall status
            ValidationStatus status = determineOverallStatus(allIssues);
            boolean canProceed = status != ValidationStatus.INVALID &&
                               allIssues.stream().noneMatch(issue ->
                                   issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.ERROR);

            String summaryMessage = generateSummaryMessage(allIssues, status);

            return new CsvValidationResult(allIssues, stats, status, canProceed, summaryMessage, patternAnalysis);

        } catch (Exception e) {
            return CsvValidationResult.invalid("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Generate preview data for UI display
     */
    public CsvPreviewData generatePreview(File csvFile, int maxRows) {
        try {
            String encoding = detectEncoding(csvFile);
            ParsedCsvData parsedData = parseCsvFile(csvFile, encoding, Math.min(maxRows, MAX_PREVIEW_ROWS));
            CsvFileStats stats = generateStats(csvFile, parsedData, encoding);

            // Run validation on preview data to identify cell issues
            Map<CellLocation, ValidationIssue> cellIssues = new HashMap<>();
            for (ValidationRule rule : validationRules) {
                List<ValidationIssue> issues = rule.validate(parsedData.data, parsedData.headers);
                for (ValidationIssue issue : issues) {
                    if (issue.getLocation() != null) {
                        cellIssues.put(issue.getLocation(), issue);
                    }
                }
            }

            // Count total rows in file for truncation indicator
            int totalRows = countTotalRows(csvFile, encoding);

            return new CsvPreviewData(parsedData.data, parsedData.headers, cellIssues, stats, totalRows);

        } catch (Exception e) {
            // Return empty preview on error
            return new CsvPreviewData(new String[0][0], new String[0], new HashMap<>(),
                                    new CsvFileStats(0, 0, 0, 0, 0, "unknown", false), 0);
        }
    }

    /**
     * Replace all instances of a specific scientific notation value with user-provided replacement
     */
    public File replaceScientificNotationValue(File csvFile, String scientificValue, String replacementValue) {
        try {
            String encoding = detectEncoding(csvFile);
            ParsedCsvData parsedData = parseCsvFile(csvFile, encoding);

            // Replace all instances of the scientific notation value
            String[][] fixedData = new String[parsedData.data.length][];
            for (int row = 0; row < parsedData.data.length; row++) {
                fixedData[row] = new String[parsedData.data[row].length];
                System.arraycopy(parsedData.data[row], 0, fixedData[row], 0, parsedData.data[row].length);

                // Replace in merchant column (trim for comparison to handle whitespace)
                if (fixedData[row].length > 0 && fixedData[row][0] != null &&
                    scientificValue.equals(fixedData[row][0].trim())) {
                    fixedData[row][0] = replacementValue;
                }

                // Replace in ID column (trim for comparison to handle whitespace)
                if (fixedData[row].length > 1 && fixedData[row][1] != null &&
                    scientificValue.equals(fixedData[row][1].trim())) {
                    fixedData[row][1] = replacementValue;
                }
            }

            // Create output file
            File outputFile = new File(csvFile.getParent(),
                getReplacedFileName(csvFile.getName()));

            // Write fixed data to new file
            writeFixedCsvFile(outputFile, fixedData, parsedData.headers, encoding);

            return outputFile;

        } catch (Exception e) {
            throw new RuntimeException("Failed to replace scientific notation in CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Fix common issues automatically
     */
    public File fixCommonIssues(File csvFile, ValidationOptions options) {
        try {
            // Parse the original file
            String encoding = detectEncoding(csvFile);
            ParsedCsvData parsedData = parseCsvFile(csvFile, encoding);

            // Apply auto-fixes
            String[][] fixedData = parsedData.data;

            // Apply each validation rule's auto-fix if enabled
            for (ValidationRule rule : validationRules) {
                if (rule.canAutoFix()) {
                    if (rule instanceof ScientificNotationRule && options.isAutoFixScientificNotation()) {
                        fixedData = rule.autoFix(fixedData, parsedData.headers);
                    } else if (rule instanceof ContentValidationRule && options.isAutoFixWhitespace()) {
                        fixedData = rule.autoFix(fixedData, parsedData.headers);
                    }
                }
            }

            // Create output file
            File outputFile = new File(csvFile.getParent(),
                getFixedFileName(csvFile.getName()));

            // Write fixed data to new file
            writeFixedCsvFile(outputFile, fixedData, parsedData.headers, encoding);

            return outputFile;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fix CSV file: " + e.getMessage(), e);
        }
    }

    private String detectEncoding(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(fis);
            CharsetMatch match = detector.detect();
            return match != null ? match.getName() : StandardCharsets.UTF_8.name();
        } catch (IOException e) {
            return StandardCharsets.UTF_8.name();
        }
    }

    private ParsedCsvData parseCsvFile(File file, String encoding) throws IOException {
        return parseCsvFile(file, encoding, Integer.MAX_VALUE);
    }

    private ParsedCsvData parseCsvFile(File file, String encoding, int maxRows) throws IOException {
        Charset charset = Charset.forName(encoding);

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreEmptyLines(false) // We want to detect empty lines
                .parse(Files.newBufferedReader(file.toPath(), charset))) {

            List<String[]> dataList = new ArrayList<>();
            String[] headers = null;

            // Get headers
            List<String> headerNames = parser.getHeaderNames();
            if (headerNames != null && !headerNames.isEmpty()) {
                headers = headerNames.toArray(new String[0]);
            }

            // Parse data rows
            int rowCount = 0;
            for (CSVRecord record : parser) {
                if (rowCount >= maxRows) {
                    break;
                }

                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                dataList.add(row);
                rowCount++;
            }

            String[][] data = dataList.toArray(new String[0][]);
            return new ParsedCsvData(data, headers);
        }
    }

    private CsvFileStats generateStats(File file, ParsedCsvData parsedData, String encoding) {
        int totalRows = parsedData.data.length;
        int totalColumns = totalRows > 0 ? parsedData.data[0].length : 0;
        int emptyRows = 0;

        // Count empty rows
        for (String[] row : parsedData.data) {
            boolean isEmpty = true;
            for (String cell : row) {
                if (cell != null && !cell.trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                emptyRows++;
            }
        }

        int dataRows = totalRows - emptyRows;
        boolean hasHeader = parsedData.headers != null;

        return new CsvFileStats(totalRows, totalColumns, dataRows, emptyRows,
                              file.length(), encoding, hasHeader);
    }

    private int countTotalRows(File file, String encoding) {
        try {
            return (int) Files.lines(file.toPath(), Charset.forName(encoding)).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private ValidationStatus determineOverallStatus(List<ValidationIssue> issues) {
        if (issues.isEmpty()) {
            return ValidationStatus.VALID;
        }

        boolean hasErrors = issues.stream().anyMatch(issue ->
            issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.ERROR);

        if (hasErrors) {
            return ValidationStatus.ERROR;
        } else {
            return ValidationStatus.WARNING;
        }
    }

    private String generateSummaryMessage(List<ValidationIssue> issues, ValidationStatus status) {
        if (issues.isEmpty()) {
            return "CSV file validation passed successfully";
        }

        long errorCount = issues.stream().filter(issue ->
            issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.ERROR).count();
        long warningCount = issues.stream().filter(issue ->
            issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.WARNING).count();

        if (errorCount > 0) {
            return String.format("Found %d errors and %d warnings. Errors must be fixed before processing.",
                                errorCount, warningCount);
        } else {
            return String.format("Found %d warnings. File can be processed but review is recommended.",
                                warningCount);
        }
    }

    /**
     * Generate a filename for the fixed CSV file
     */
    private String getFixedFileName(String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExt = originalFileName.substring(0, dotIndex);
            String extension = originalFileName.substring(dotIndex);
            return nameWithoutExt + "_fixed" + extension;
        } else {
            return originalFileName + "_fixed";
        }
    }

    /**
     * Generate a filename for the replaced CSV file
     */
    private String getReplacedFileName(String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExt = originalFileName.substring(0, dotIndex);
            String extension = originalFileName.substring(dotIndex);
            return nameWithoutExt + "_replaced" + extension;
        } else {
            return originalFileName + "_replaced";
        }
    }

    /**
     * Write fixed CSV data to a file
     */
    private void writeFixedCsvFile(File outputFile, String[][] data, String[] headers, String encoding) throws IOException {
        Charset charset = Charset.forName(encoding);

        try (var writer = Files.newBufferedWriter(outputFile.toPath(), charset)) {
            // Write headers if present
            if (headers != null) {
                writer.write(String.join(",", headers));
                writer.newLine();
            }

            // Write data rows
            for (String[] row : data) {
                // Escape values that contain commas or quotes
                String[] escapedRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    String value = row[i] != null ? row[i] : "";
                    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                        // Escape quotes and wrap in quotes
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    escapedRow[i] = value;
                }
                writer.write(String.join(",", escapedRow));
                writer.newLine();
            }
        }
    }

    /**
     * Internal class to hold parsed CSV data
     */
    private static class ParsedCsvData {
        final String[][] data;
        final String[] headers;

        ParsedCsvData(String[][] data, String[] headers) {
            this.data = data;
            this.headers = headers;
        }
    }
}
