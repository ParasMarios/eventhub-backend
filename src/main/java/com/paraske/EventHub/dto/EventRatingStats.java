package com.paraske.EventHub.dto;

public class EventRatingStats {
    private Double averageOverall;
    private Double averageOrganization;
    private Double averageContent;
    private Double averageVenue;
    private Long totalReviews;

    public EventRatingStats(Double averageOverall, Double averageOrganization, Double averageContent, Double averageVenue, Long totalReviews) {
        this.averageOverall = averageOverall;
        this.averageOrganization = averageOrganization;
        this.averageContent = averageContent;
        this.averageVenue = averageVenue;
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

    public Double getAverageOrganization() {
        return averageOrganization;
    }

    public void setAverageOrganization(Double averageOrganization) {
        this.averageOrganization = averageOrganization;
    }

    public Double getAverageContent() {
        return averageContent;
    }

    public void setAverageContent(Double averageContent) {
        this.averageContent = averageContent;
    }

    public Double getAverageVenue() {
        return averageVenue;
    }

    public void setAverageVenue(Double averageVenue) {
        this.averageVenue = averageVenue;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
