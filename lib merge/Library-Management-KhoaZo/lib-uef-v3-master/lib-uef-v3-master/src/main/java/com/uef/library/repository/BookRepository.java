package com.uef.library.repository;

import com.uef.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    // <<< MERGE: THÊM TỪ ĐỒ ÁN 2 >>>
    List<Book> findAllByIdIn(List<Long> ids);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 1 (CHO BÁO CÁO) >>>
    @Query("SELECT new map(c.name as categoryName, COUNT(b.id) as bookCount) " +
            "FROM Book b JOIN b.category c " +
            "GROUP BY c.name " +
            "ORDER BY COUNT(b.id) DESC")
    List<Map<String, Object>> countBooksByCategory();
}