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
                    String fixedValue = convertFromScientificNotation(merchantValue.trim());
                    issues.add(ValidationIssue.scientificNotation(
                        new CellLocation(row, MERCHANT_COLUMN),
                        merchantValue,
                        fixedValue
                    ));
                }
            }

            // Also check ID column (less common but possible)
            if (data[row].length > ID_COLUMN) {
                String idValue = data[row][ID_COLUMN];
                if (idValue != null && isScientificNotation(idValue.trim())) {
                    String fixedValue = convertFromScientificNotation(idValue.trim());
                    issues.add(ValidationIssue.scientificNotation(
                        new CellLocation(row, ID_COLUMN),
                        idValue,
                        fixedValue
                    ));
                }
            }
        }

        return issues;
    }

    @Override
    public boolean canAutoFix() {
        return true;
    }

    @Override
    public String[][] autoFix(String[][] data, String[] headers) {
        // Create a copy of the data to avoid modifying the original
        String[][] fixedData = new String[data.length][];

        for (int row = 0; row < data.length; row++) {
            fixedData[row] = new String[data[row].length];
            System.arraycopy(data[row], 0, fixedData[row], 0, data[row].length);

            // Fix merchant column first (most commonly affected)
            if (fixedData[row].length > MERCHANT_COLUMN && fixedData[row][MERCHANT_COLUMN] != null) {
                String merchantValue = fixedData[row][MERCHANT_COLUMN].trim();
                if (isScientificNotation(merchantValue)) {
                    fixedData[row][MERCHANT_COLUMN] = convertFromScientificNotation(merchantValue);
                }
            }

            // Fix ID column
            if (fixedData[row].length > ID_COLUMN && fixedData[row][ID_COLUMN] != null) {
                String idValue = fixedData[row][ID_COLUMN].trim();
                if (isScientificNotation(idValue)) {
                    fixedData[row][ID_COLUMN] = convertFromScientificNotation(idValue);
                }
            }
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
}
