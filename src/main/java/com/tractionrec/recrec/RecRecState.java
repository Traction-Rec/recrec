package com.tractionrec.recrec;

import com.tractionrec.recrec.domain.QueryBy;

import java.io.File;

public class RecRecState {
    public String accountId;
    public String accountToken;
    public QueryBy queryMode;
    public File inputFile;
    public void reset() {
        this.accountId = null;
        this.accountToken = null;
        this.queryMode = null;
        this.inputFile = null;
    }
}
