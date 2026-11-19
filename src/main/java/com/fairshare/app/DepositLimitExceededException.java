package com.fairshare.app;

public class DepositLimitExceededException extends Exception {

    public DepositLimitExceededException(String message) {
        super(message);
    }
}
