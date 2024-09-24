package com.tractionrec.recrec.domain.result;

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
        final boolean multipleResults = this.expressEntities.size() > 1;
        for (Transaction current : this.expressEntities) {
            rows.add(new TransactionQueryOutputRow(this, current, multipleResults));
        }
        return rows;
    }

}
