package com.fairshare.app;

import java.math.BigDecimal;

public class DepositDeduction implements Reviewable {

    private final long id;
    private final long tenancyId;
    private final BigDecimal amount;
    private final String reason;
    private DeductionStatus status;
    private String tenantComment;

    public DepositDeduction(long id, long tenancyId, BigDecimal amount, String reason) {
        this.id = id;
        this.tenancyId = tenancyId;
        this.amount = amount;
        this.reason = reason;
        this.status = DeductionStatus.PROPOSED;   // never reduces the deposit yet
        this.tenantComment = "";
    }

    @Override
    public void acknowledge() {
        status = DeductionStatus.ACCEPTED;   // tenant agrees
    }

    @Override
    public void dispute(String comment) {
        status = DeductionStatus.DISPUTED;
        tenantComment = comment;
    }

    @Override
    public ReviewStatus getReviewStatus() {
        // translate our own status into the shared ReviewStatus
        if (status == DeductionStatus.ACCEPTED) {
            return ReviewStatus.ACKNOWLEDGED;
        }
        if (status == DeductionStatus.DISPUTED || status == DeductionStatus.REJECTED) {
            return ReviewStatus.DISPUTED;
        }
        return ReviewStatus.PENDING;
    }

    public void reject() {
        status = DeductionStatus.REJECTED;   // landlord withdraws it
    }

    public long getId() {
        return id;
    }

    public long getTenancyId() {
        return tenancyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public DeductionStatus getStatus() {
        return status;
    }

    public String getTenantComment() {
        return tenantComment;
    }

    @Override
    public String toString() {
        return "DepositDeduction{id=" + id + ", tenancyId=" + tenancyId + ", amount=" + amount
                + ", reason=" + reason + ", status=" + status + "}";
    }
}
