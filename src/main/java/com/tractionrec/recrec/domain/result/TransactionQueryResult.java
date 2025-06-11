package com.tractionrec.recrec.domain.result;

import com.tractionrec.recrec.domain.QueryBy;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.output.TransactionQueryOutputRow;

import java.util.ArrayList;
import java.util.List;

public class TransactionQueryResult extends QueryResult<Transaction, TransactionQueryOutputRow> {
    public TransactionQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage) {
        this(item, status, expressResponseMessage, new ArrayList<>());
    }
    public TransactionQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage, List<Transaction> expressEntities) {
        super(item, status, expressResponseMessage, expressEntities, TransactionQueryOutputRow.class);
    }

    public List<TransactionQueryOutputRow> getOutputRows() {
        if(this.expressEntities.isEmpty()) {
            return List.of(new TransactionQueryOutputRow(this));
        }
        List<TransactionQueryOutputRow> rows = new ArrayList<>(this.expressEntities.size());

        // For ad-hoc queries, "multiple results" is misleading since there's only one search criteria
        // In CSV queries: one input row → multiple results = potential duplicate charge (meaningful)
        // In ad-hoc queries: one search criteria → multiple results = normal behavior (misleading)
        final Boolean multipleResults = this.item.mode() == QueryBy.ADHOC_SEARCH ? null : this.expressEntities.size() > 1;

        for (Transaction current : this.expressEntities) {
            rows.add(new TransactionQueryOutputRow(this, current, multipleResults));
        }
        return rows;
    }

}
