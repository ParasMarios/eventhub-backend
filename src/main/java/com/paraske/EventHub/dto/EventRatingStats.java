package com.paraske.EventHub.dto;

public class EventRatingStats {
    private Double averageOverall;
    private Long totalReviews;

    public EventRatingStats(Double averageOverall, Long totalReviews) {
        this.averageOverall = averageOverall;
        this.totalReviews = totalReviews;
    }

    public EventRatingStats() {
    }

    public Double getAverageOverall() {
        return averageOverall;
    }

    public void setAverageOverall(Double averageOverall) {
        this.averageOverall = averageOverall;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
