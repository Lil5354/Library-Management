// File: com/uef/library/dto/DashboardStatsDTO.java
package com.uef.library.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private long borrowedBooks;
    private long overdueBooks;
    private long borrowsToday;
    private long returnsToday;
    private long newReadersToday;
}