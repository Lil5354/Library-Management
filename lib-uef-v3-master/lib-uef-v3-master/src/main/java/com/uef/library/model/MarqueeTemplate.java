package com.uef.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarqueeTemplate {
    private String id;
    private String name;
    private String content;
}