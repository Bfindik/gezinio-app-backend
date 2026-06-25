package com.example.gezinio.profile.dto;

import jakarta.validation.constraints.*;

public class CreateReviewRequest {

    @NotNull
    private Long tourId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
    private String comment;

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
