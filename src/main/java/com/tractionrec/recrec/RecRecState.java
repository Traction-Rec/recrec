package com.tractionrec.recrec;

import com.tractionrec.recrec.domain.QueryBy;
import com.tractionrec.recrec.service.QueryService;

import java.io.File;

public class RecRecState {
    public final QueryService queryService;
    public String accountId;
    public String accountToken;
    public QueryBy queryMode;
    public File inputFile;
    public RecRecState(QueryService queryService) {
        this.queryService = queryService;
    }
    public void reset() {
        this.accountId = null;
        this.accountToken = null;
        this.queryMode = null;
        this.inputFile = null;
    }
}
