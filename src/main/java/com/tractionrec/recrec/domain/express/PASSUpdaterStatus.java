package com.tractionrec.recrec.domain.express;

public enum PASSUpdaterStatus {
    Null,
    UpdateInProgress,
    MatchNoChanges,
    MatchAccountChange,
    MatchExpirationChange,
    MatchAccountClosed,
    MatchContactCardholder,
    NoMatchParticipating,
    NoMatchNonParticipating,
    InvalidInfo,
    NoResponse,
    NotAllowed,
    Error,
    PASSUpdaterDisabled,
    NotUpdated,
    IssuerCorrected,
    Fraud,
    Inactivity,
    MerchantBlocked,
    InactiveSeller
}
