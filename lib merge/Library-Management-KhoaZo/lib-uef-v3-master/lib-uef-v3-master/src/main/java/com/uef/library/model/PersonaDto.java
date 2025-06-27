package com.uef.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDto {
    private String name;
    private String displayName;
    private String description;
    private String fileName;
    private boolean deletable;
    private String icon;
    private boolean active;
    private LocalDateTime createdAt;

    public PersonaDto(String name, String displayName, String description, String fileName, boolean deletable, String icon) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.fileName = fileName;
        this.deletable = deletable;
        this.icon = icon;
        this.active = false;
    }
}