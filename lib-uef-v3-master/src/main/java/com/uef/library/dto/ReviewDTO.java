package com.uef.library.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long bookId;
    private int rating;
    private String comment;

    // Thêm trường để hiển thị
    private String reviewerName;
    private String reviewDate;
}