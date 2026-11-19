package com.fairshare.app;

public interface Reviewable {

    void acknowledge();

    void dispute(String comment);

    ReviewStatus getReviewStatus();
}
