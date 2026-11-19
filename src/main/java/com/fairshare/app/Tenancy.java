package com.fairshare.app;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Tenancy implements Storable{

    private final long id;
    private final long tenantId;
    private final long spaceId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal monthlyRent;
    private final BigDecimal agreedDeposit;
    private TenancyStatus status;   // changes over time

    public Tenancy(long id, long tenantId, long spaceId, LocalDate startDate,
                   LocalDate endDate, BigDecimal monthlyRent, BigDecimal agreedDeposit) {
        this.id = id;
        this.tenantId = tenantId;
        this.spaceId = spaceId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.agreedDeposit = agreedDeposit;
        this.status = TenancyStatus.PENDING;
    }

    public void setStatus(TenancyStatus status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getTenantId() {
        return tenantId;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public BigDecimal getAgreedDeposit() {
        return agreedDeposit;
    }

    public TenancyStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Tenancy{id=" + id + ", tenantId=" + tenantId + ", spaceId=" + spaceId
                + ", " + startDate + " to " + endDate + ", rent=" + monthlyRent
                + ", deposit=" + agreedDeposit + ", status=" + status + "}";
    }

    @Override
    public String toCsvLine() {
        return id + "," + tenantId + "," + spaceId + "," + startDate + "," + endDate
                + "," + monthlyRent + "," + agreedDeposit + "," + status;
    }

    public static Tenancy fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        Tenancy tenancy = new Tenancy(Long.parseLong(parts[0]), Long.parseLong(parts[1]),
                Long.parseLong(parts[2]), LocalDate.parse(parts[3]), LocalDate.parse(parts[4]),
                new BigDecimal(parts[5]), new BigDecimal(parts[6]));
        tenancy.setStatus(TenancyStatus.valueOf(parts[7]));   // restore the saved status
        return tenancy;
    }

}
