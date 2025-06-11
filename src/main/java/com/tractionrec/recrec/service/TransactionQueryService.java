package com.tractionrec.recrec.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tractionrec.recrec.domain.AdhocQueryItem;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.express.TransactionQueryResponse;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionQueryService extends QueryService {

    private TransactionQueryService(Boolean isProduction, TemplateEngine templateEngine) {
        super(isProduction, templateEngine);
    }

    public TransactionQueryResult queryForTransaction(String accountId, String accountToken, QueryItem item) {
        return queryForTransaction(accountId, accountToken, item, null);
    }

    public TransactionQueryResult queryForTransaction(String accountId, String accountToken, QueryItem item, AdhocQueryItem adhocItem) {
        String requestBody = getTxBody(accountId, accountToken, item, adhocItem);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getReportingUri())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("SOAPAction", "https://reporting.elementexpress.com/TransactionQuery")
                .header("Content-Type", "text/xml")
                .timeout(Duration.ofSeconds(60)) // Request timeout
                .build();
        try {
            HttpResponse<String> response = executeRequestWithRetry(request);
            if(response.statusCode() != 200) {
                return new TransactionQueryResult(item, ResultStatus.ERROR, "Status Code: " + response.statusCode());
            }
            final String responseBody = response.body();
            final String responseElement = responseBody.substring(responseBody.indexOf("<response>"), responseBody.indexOf("</TransactionQueryResponse>")).trim();

            final TransactionQueryResponse queryResponse = mapper.readValue(responseElement, TransactionQueryResponse.class);
            if(queryResponse.responseCode == 90) {
                return new TransactionQueryResult(item, ResultStatus.NOT_FOUND, queryResponse.responseMessage);
            }
            if(queryResponse.responseCode != 0) {
                return new TransactionQueryResult(item, ResultStatus.ERROR, queryResponse.responseMessage);
            }
            final String unescapedReportingData = StringEscapeUtils.unescapeXml(queryResponse.reportingData);

            // Parse the transaction data with robust error handling
            List<Transaction> results = parseTransactionData(unescapedReportingData, item);
            if (results == null) {
                return new TransactionQueryResult(item, ResultStatus.ERROR, "Failed to parse transaction data");
            }

            return new TransactionQueryResult(item, ResultStatus.SUCCESS, queryResponse.responseMessage, results);
        } catch (JsonParseException ex) {
            System.err.println("JSON/XML Parse Error in TransactionQueryService:");
            System.err.println("Query Item: " + item);
            System.err.println("Error: " + ex.getMessage());
            if (ex.getLocation() != null) {
                System.err.println("Error location: line " + ex.getLocation().getLineNr() +
                                 ", column " + ex.getLocation().getColumnNr() +
                                 ", character " + ex.getLocation().getCharOffset());
            }
            ex.printStackTrace();
            return new TransactionQueryResult(item, ResultStatus.ERROR,
                "XML parsing failed: " + ex.getMessage() +
                (ex.getLocation() != null ? " at position " + ex.getLocation().getCharOffset() : ""));
        } catch (Exception ex) {
            System.err.println("Unexpected error in TransactionQueryService:");
            System.err.println("Query Item: " + item);
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return new TransactionQueryResult(item, ResultStatus.ERROR, "Query failed: " + ex.getMessage());
        }
    }

    public static TransactionQueryService forProduction(TemplateEngine templateEngine) {
        return new TransactionQueryService(true, templateEngine);
    }

    public static TransactionQueryService forTest(TemplateEngine templateEngine) {
        return new TransactionQueryService(false, templateEngine);
    }

    private String getTxBody(String accountId, String accountToken, QueryItem item) {
        return getTxBody(accountId, accountToken, item, null);
    }

    private String getTxBody(String accountId, String accountToken, QueryItem item, AdhocQueryItem adhocItem) {
        TemplateOutput output = new StringOutput();
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("accountId", accountId);
        templateParams.put("accountToken", accountToken);
        templateParams.put("queryItem", item);
        templateParams.put("adhocQueryItem", adhocItem); // This can be null for CSV queries
        templateEngine.render("transactionQueryPOSTBody.jte", templateParams, output);
        return output.toString();
    }

    private URI getReportingUri() {
        return isProduction ? URI.create("https://reporting.elementexpress.com/express.asmx") : URI.create("https://certreporting.elementexpress.com/express.asmx");
    }

    /**
     * Parse transaction data with robust error handling and XML preprocessing
     */
    private List<Transaction> parseTransactionData(String xmlData, QueryItem item) {
        try {
            // Validate basic XML structure
            if (!isValidXmlStructure(xmlData)) {
                System.err.println("Invalid XML structure detected for query: " + item);
                logXmlSample(xmlData, "Invalid XML Structure");
                return null;
            }

            // Preprocess XML to handle common issues
            String cleanedXml = preprocessXml(xmlData);

            // Attempt to parse with size limits for safety
            if (cleanedXml.length() > 50_000_000) { // 50MB limit
                System.err.println("XML response too large (" + cleanedXml.length() + " chars) for query: " + item);
                return null;
            }

            // Parse the cleaned XML
            List<Transaction> results = mapper.readValue(cleanedXml, new TypeReference<List<Transaction>>() {});

            System.out.println("Successfully parsed " + results.size() + " transactions for query: " + item);
            return results;

        } catch (JsonParseException ex) {
            System.err.println("JSON Parse Error in parseTransactionData:");
            System.err.println("Query Item: " + item);
            System.err.println("Error: " + ex.getMessage());

            if (ex.getLocation() != null) {
                long errorPos = ex.getLocation().getCharOffset();
                System.err.println("Error at character position: " + errorPos);
                logXmlSample(xmlData, "Parse Error Context", errorPos);
            }
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            System.err.println("Unexpected error in parseTransactionData:");
            System.err.println("Query Item: " + item);
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Basic XML structure validation
     */
    private boolean isValidXmlStructure(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return false;
        }

        // Check for basic XML structure
        String trimmed = xml.trim();
        if (!trimmed.startsWith("<") || !trimmed.endsWith(">")) {
            return false;
        }

        // Check for balanced brackets (basic validation)
        int openBrackets = 0;
        for (char c : trimmed.toCharArray()) {
            if (c == '<') openBrackets++;
            else if (c == '>') openBrackets--;
            if (openBrackets < 0) return false;
        }

        return openBrackets == 0;
    }

    /**
     * Preprocess XML to handle common parsing issues
     */
    private String preprocessXml(String xml) {
        if (xml == null) return null;

        return xml
            // Fix unescaped ampersands (but not already escaped ones)
            .replaceAll("&(?![a-zA-Z0-9#]{1,8};)", "&amp;")
            // Remove or escape problematic characters in element names
            .replaceAll("<([^>]*?)\\*([^>]*?)>", "<$1_ASTERISK_$2>")
            // Handle potential null bytes
            .replaceAll("\\x00", "")
            // Normalize line endings
            .replaceAll("\\r\\n", "\\n")
            .replaceAll("\\r", "\\n");
    }

    /**
     * Log a sample of XML around an error position for debugging
     */
    private void logXmlSample(String xml, String context) {
        logXmlSample(xml, context, -1);
    }

    private void logXmlSample(String xml, String context, long errorPosition) {
        if (xml == null) {
            System.err.println(context + ": XML is null");
            return;
        }

        System.err.println("=== " + context + " ===");
        System.err.println("XML length: " + xml.length() + " characters");

        if (errorPosition >= 0 && errorPosition < xml.length()) {
            // Show context around the error
            int start = Math.max(0, (int)errorPosition - 200);
            int end = Math.min(xml.length(), (int)errorPosition + 200);

            System.err.println("Context around error position " + errorPosition + ":");
            System.err.println("..." + xml.substring(start, end) + "...");

            // Point to the exact error character
            int relativePos = (int)errorPosition - start;
            StringBuilder pointer = new StringBuilder();
            for (int i = 0; i < relativePos + 3; i++) { // +3 for "..."
                pointer.append(" ");
            }
            pointer.append("^ ERROR HERE");
            System.err.println(pointer.toString());
        } else {
            // Show beginning and end of XML
            int sampleSize = 500;
            if (xml.length() <= sampleSize * 2) {
                System.err.println("Full XML: " + xml);
            } else {
                System.err.println("XML start: " + xml.substring(0, sampleSize) + "...");
                System.err.println("XML end: ..." + xml.substring(xml.length() - sampleSize));
            }
        }
        System.err.println("=== End " + context + " ===");
    }
}
