package com.tractionrec.recrec.domain.output;

import com.tractionrec.recrec.domain.result.QueryResult;

public abstract class OutputRow {
    public final String merchant;
    public final String id;
    public final String status;
    public final String message;

    protected OutputRow(QueryResult result) {
        this.merchant = result.getItem().merchant();
        this.id = result.getItem().id();
        this.status = result.getStatus().name();
        this.message = result.getExpressResponseMessage();
    }
}
