package com.example.backend.service;

/**
 * Aggregated average rating and approved review count for a pitch.
 */
public record PitchRatingSummary(Double averageRating, Long reviewCount) {

    public static PitchRatingSummary empty() {
        return new PitchRatingSummary(0d, 0L);
    }
}
