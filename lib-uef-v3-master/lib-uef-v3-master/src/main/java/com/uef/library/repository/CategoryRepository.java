// File: src/main/java/com/uef/library/repository/CategoryRepository.java

package com.uef.library.repository;

import com.uef.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <<< ĐẢM BẢO BẠN CÓ DÒNG IMPORT NÀY

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ==========================================================
    // === THÊM PHƯƠNG THỨC NÀY VÀO ĐỂ SỬA LỖI              ===
    // ==========================================================
    /**
     * Tự động tìm kiếm một Category trong cơ sở dữ liệu dựa vào tên.
     * Trả về một Optional để xử lý trường hợp không tìm thấy.
     * @param name Tên của thể loại cần tìm.
     * @return Optional chứa Category nếu tìm thấy, ngược lại trả về Optional rỗng.
     */
    Optional<Category> findByName(String name);

}