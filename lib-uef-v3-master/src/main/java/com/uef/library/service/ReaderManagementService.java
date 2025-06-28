package com.uef.library.service;

import com.uef.library.dto.ReaderDetailDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface ReaderManagementService {

    Page<ReaderDetailDTO> getPaginatedReaders(String keyword, Pageable pageable);

    Optional<ReaderDetailDTO> findReaderById(String userId);

    /**
     * SỬA LỖI: Thay đổi kiểu trả về từ void -> ReaderDetailDTO
     */
    ReaderDetailDTO createReader(ReaderDetailDTO readerDto) throws Exception;

    /**
     * SỬA LỖI: Thay đổi kiểu trả về từ void -> ReaderDetailDTO
     */
    ReaderDetailDTO updateReader(String userId, ReaderDetailDTO readerDto) throws Exception;

    void deleteReader(String userId);

    void importReadersFromExcel(MultipartFile file) throws IOException;

    void exportReadersToExcel(HttpServletResponse response) throws IOException;
}