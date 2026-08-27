package com.taxgap.dto;

/**
 * One transaction as received in the upload request.
 *
 * The fields are Strings on purpose. If a value is missing or badly formatted
 * (for example a bad date or a non-numeric amount), we can record a validation
 * failure for that single row instead of failing to read the whole batch.
 */
public class TransactionDto {

    private String transactionId;
    private String date;
    private String customerId;
    private String amount;
    private String taxRate;
    private String reportedTax;
    private String transactionType;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getReportedTax() {
        return reportedTax;
    }

    public void setReportedTax(String reportedTax) {
        this.reportedTax = reportedTax;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
