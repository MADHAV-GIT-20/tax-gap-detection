package com.taxgap.dto;

import com.taxgap.entity.Transaction;

import java.util.List;

/** The response after processing a batch upload. */
public class BatchUploadResponse {

    private int received;
    private int succeeded;
    private int failed;
    private int exceptionsRaised;
    private List<Transaction> results;

    public int getReceived() {
        return received;
    }

    public void setReceived(int received) {
        this.received = received;
    }

    public int getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getExceptionsRaised() {
        return exceptionsRaised;
    }

    public void setExceptionsRaised(int exceptionsRaised) {
        this.exceptionsRaised = exceptionsRaised;
    }

    public List<Transaction> getResults() {
        return results;
    }

    public void setResults(List<Transaction> results) {
        this.results = results;
    }
}
