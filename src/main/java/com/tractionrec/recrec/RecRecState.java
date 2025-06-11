package com.tractionrec.recrec;

import com.tractionrec.recrec.csv.CsvValidationResult;
import com.tractionrec.recrec.domain.AdhocQueryItem;
import com.tractionrec.recrec.domain.QueryBy;
import com.tractionrec.recrec.domain.result.QueryResult;

import java.io.File;
import java.util.List;

public class RecRecState {
    public String accountId;
    public String accountToken;
    public QueryBy queryMode;
    public File inputFile;
    public CsvValidationResult validationResult;
    public AdhocQueryItem adhocQueryItem;
    public List<QueryResult<?, ?>> queryResults;

    public void reset() {
        this.accountId = null;
        this.accountToken = null;
        this.queryMode = null;
        this.inputFile = null;
        this.validationResult = null;
        this.adhocQueryItem = null;
        this.queryResults = null;
    }
}
