package com.fairshare.app;

import java.time.LocalDate;

public class Inspection implements Reviewable, Storable {
    private final long id;
    private final long spaceId;
    private final InspectionType type;
    private final LocalDate date;
    private ReviewStatus reviewStatus;   // not final: it changes over time
    private String reviewComment;        // empty unless disputed

    public Inspection(long id, long spaceId, InspectionType type, LocalDate date) {
        this.id = id;
        this.spaceId = spaceId;
        this.type = type;
        this.date = date;
        this.reviewStatus = ReviewStatus.PENDING;   // every new inspection waits for review
        this.reviewComment = "";
    }

    @Override
    public void acknowledge() {
        reviewStatus = ReviewStatus.ACKNOWLEDGED;
    }

    @Override
    public void dispute(String comment) {
        reviewStatus = ReviewStatus.DISPUTED;
        reviewComment = comment;
    }

    @Override
    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public long getId() {
        return id;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public InspectionType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    @Override
    public String toString() {
        return "Inspection{id=" + id + ", spaceId=" + spaceId + ", type=" + type
                + ", date=" + date + ", review=" + reviewStatus + "}";
    }


    @Override
    public String toCsvLine() {
        return id + "," + spaceId + "," + type + "," + date + "," + reviewStatus + "," + reviewComment;
    }

    public static Inspection fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        Inspection inspection = new Inspection(Long.parseLong(parts[0]), Long.parseLong(parts[1]),
                InspectionType.valueOf(parts[2]), LocalDate.parse(parts[3]));
        ReviewStatus status = ReviewStatus.valueOf(parts[4]);
        if (status == ReviewStatus.ACKNOWLEDGED) {
            inspection.acknowledge();
        } else if (status == ReviewStatus.DISPUTED) {
            inspection.dispute(parts[5]);
        }
        return inspection;
    }

}
