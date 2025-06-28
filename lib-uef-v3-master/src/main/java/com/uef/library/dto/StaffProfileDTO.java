package com.uef.library.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
@Data
@Builder
public class StaffProfileDTO {
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
}