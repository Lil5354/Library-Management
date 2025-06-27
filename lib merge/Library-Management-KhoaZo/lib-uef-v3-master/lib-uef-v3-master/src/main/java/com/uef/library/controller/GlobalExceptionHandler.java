// PATH: src/main/java/com/uef/library/controller/GlobalExceptionHandler.java
package com.uef.library.controller;

import com.uef.library.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Bạn có thể thêm các @ExceptionHandler khác cho các loại exception khác ở đây
    // Ví dụ:
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
    //     ErrorResponse errorResponse = new ErrorResponse(
    //             HttpStatus.INTERNAL_SERVER_ERROR.value(),
    //             "Đã có lỗi không mong muốn xảy ra ở server."
    //     );
    //     return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    // }
}