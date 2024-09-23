package com.tractionrec.recrec.domain;

public enum QueryBy {
    RECORD_ID,
    VANTIV_ID,
    SETUP_ID,
    PAYMENT_ACCOUNT,
    BIN_QUERY;

    public <R> R accept(QueryTargetVisitor<R> visitor) {
        switch (this) {
            case RECORD_ID, SETUP_ID, VANTIV_ID -> {
                return visitor.visitTransactionQuery();
            }
            case PAYMENT_ACCOUNT -> {
                return visitor.visitPaymentAccountQuery();
            }
            case BIN_QUERY -> {
                return visitor.visitBINQuery();
            }
        }
        return null;
    }
}
