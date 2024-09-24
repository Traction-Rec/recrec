package com.tractionrec.recrec.domain;

public interface QueryTargetVisitor<R> {
    R visitTransactionQuery();

    R visitPaymentAccountQuery();

    R visitBINQuery();
}
