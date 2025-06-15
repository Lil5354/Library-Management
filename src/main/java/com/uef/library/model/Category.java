package com.uef.library.model;

// === IMPORT THÊM DÒNG NÀY ===
import com.fasterxml.jackson.annotation.JsonManagedReference;
// ===========================

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonManagedReference // <<< THÊM DÒNG NÀY
    private List<Book> books;
}