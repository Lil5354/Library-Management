// src/main/java/com/uef.library.model/NotificationTemplate.java
package com.uef.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    private String id;
    private String name;
    private String title;
    private String message;
}