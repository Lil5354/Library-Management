package com.uef.library.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for the request to return multiple books at once.
 */
@Data
public class ReturnMultipleRequestDTO {
    /**
     * The ID of the reader returning the books. Can be null.
     */
    private String userId;

    /**
     * A list of ISBNs for the books being returned.
     */
    private List<String> isbns;
}
