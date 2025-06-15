package com.uef.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "marquee_message")
public class MarqueeMessage {

    @Id
    private Long id = 1L;

    @Lob
    private String content;

    private boolean enabled;

    public MarqueeMessage() {
        this.id = 1L;
    }
}