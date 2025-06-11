package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation rule to detect and fix scientific notation in Merchant and ID fields
 * Primarily focuses on Merchant column as this is most commonly affected by Excel exports
 */
public class ScientificNotationRule implements ValidationRule {

    // Pattern to detect scientific notation (e.g., 1.23E+15, 4.56e-10, 7E5)
    private static final Pattern SCIENTIFIC_NOTATION_PATTERN =
        Pattern.compile("^[+-]?\\d+\\.?\\d*[eE][+-]?\\d+$");

    // Column indices to check (0 = Merchant, 1 = ID)
    private static final int MERCHANT_COLUMN = 0;
    private static final int ID_COLUMN = 1;

    @Override
    public List<ValidationIssue> validate(String[][] data, String[] headers) {
        List<ValidationIssue> issues = new ArrayList<>();

        for (int row = 0; row < data.length; row++) {
            // Check merchant column first (most commonly affected by Excel exports)
            if (data[row].length > MERCHANT_COLUMN) {
                String merchantValue = data[row][MERCHANT_COLUMN];
                if (merchantValue != null && isScientificNotation(merchantValue.trim())) {
                    issues.add(createScientificNotationError(
                        new CellLocation(row, MERCHANT_COLUMN),
                        merchantValue.trim(),
                        "Merchant"
                    ));
                }
            }

            // Also check ID column (less common but possible)
            if (data[row].length > ID_COLUMN) {
                String idValue = data[row][ID_COLUMN];
                if (idValue != null && isScientificNotation(idValue.trim())) {
                    issues.add(createScientificNotationError(
                        new CellLocation(row, ID_COLUMN),
                        idValue.trim(),
                        "ID"
                    ));
                }
            }
        }

        return issues;
    }

    @Override
    public boolean canAutoFix() {
        return false; // Never auto-fix scientific notation due to precision loss risk
    }

    @Override
    public String[][] autoFix(String[][] data, String[] headers) {
        // No auto-fix for scientific notation due to precision loss risk
        // Return data unchanged
        String[][] fixedData = new String[data.length][];
        for (int row = 0; row < data.length; row++) {
            fixedData[row] = new String[data[row].length];
            System.arraycopy(data[row], 0, fixedData[row], 0, data[row].length);
        }
        return fixedData;
    }

    @Override
    public String getRuleName() {
        return "Scientific Notation Detection";
    }

    @Override
    public String getDescription() {
        return "Detects and converts scientific notation in Merchant and ID fields (primarily Merchant column)";
    }

    /**
     * Check if a string represents scientific notation
     */
    private boolean isScientificNotation(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return SCIENTIFIC_NOTATION_PATTERN.matcher(value).matches();
    }

    /**
     * Convert scientific notation to regular number format
     */
    private String convertFromScientificNotation(String scientificValue) {
        try {
            BigDecimal decimal = new BigDecimal(scientificValue);
            return decimal.toPlainString();
        } catch (NumberFormatException e) {
            // If conversion fails, return original value
            return scientificValue;
        }
    }

    /**
     * Create a scientific notation error with helpful guidance
     */
    private ValidationIssue createScientificNotationError(CellLocation location, String scientificValue, String fieldName) {
        String guidance = generateUserGuidance(fieldName);

        return new ValidationIssue(
            com.tractionrec.recrec.domain.IssueSeverity.ERROR,
            com.tractionrec.recrec.domain.IssueType.SCIENTIFIC_NOTATION,
            String.format("%s field contains scientific notation: '%s'", fieldName, scientificValue),
            location,
            guidance,
            false, // Never auto-fixable
            scientificValue,
            null
        );
    }

    /**
     * Generate user guidance for fixing scientific notation
     */
    private String generateUserGuidance(String fieldName) {
        return String.format(
            "Manual correction required. %s field in scientific notation may have lost precision. " +
            "To prevent this: 1) Format Excel column as 'Text' before pasting data, " +
            "2) Use apostrophe prefix ('123456) to force text format, or " +
            "3) Save as CSV (Comma delimited) with columns pre-formatted as Text.",
            fieldName
        );
    }

    /**
     * Analyze scientific notation patterns to detect common scenarios
     */
    public ScientificNotationAnalysis analyzePatterns(String[][] data, String[] headers) {
        List<String> merchantScientificValues = new ArrayList<>();
        List<String> idScientificValues = new ArrayList<>();

        for (String[] row : data) {
            if (row.length > MERCHANT_COLUMN && row[MERCHANT_COLUMN] != null) {
                String merchantValue = row[MERCHANT_COLUMN].trim();
                if (isScientificNotation(merchantValue)) {
                    merchantScientificValues.add(merchantValue);
                }
            }

            if (row.length > ID_COLUMN && row[ID_COLUMN] != null) {
                String idValue = row[ID_COLUMN].trim();
                if (isScientificNotation(idValue)) {
                    idScientificValues.add(idValue);
                }
            }
        }

        return new ScientificNotationAnalysis(merchantScientificValues, idScientificValues);
    }

    /**
     * Analysis result for scientific notation patterns
     */
    public static class ScientificNotationAnalysis {
        private final List<String> merchantValues;
        private final List<String> idValues;

        public ScientificNotationAnalysis(List<String> merchantValues, List<String> idValues) {
            this.merchantValues = merchantValues;
            this.idValues = idValues;
        }

        public boolean hasAllSameMerchantValues() {
            return merchantValues.size() > 1 &&
                   merchantValues.stream().distinct().count() == 1;
        }

        public boolean hasAllSameIdValues() {
            return idValues.size() > 1 &&
                   idValues.stream().distinct().count() == 1;
        }

        public String getCommonMerchantValue() {
            return hasAllSameMerchantValues() ? merchantValues.get(0) : null;
        }

        public String getCommonIdValue() {
            return hasAllSameIdValues() ? idValues.get(0) : null;
        }

        public int getMerchantScientificCount() {
            return merchantValues.size();
        }

        public int getIdScientificCount() {
            return idValues.size();
        }
    }
}
