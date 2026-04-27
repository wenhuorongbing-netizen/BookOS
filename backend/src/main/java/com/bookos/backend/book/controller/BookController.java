package com.bookos.backend.book.controller;

import com.bookos.backend.book.dto.AddToLibraryRequest;
import com.bookos.backend.book.dto.BookRequest;
import com.bookos.backend.book.dto.BookResponse;
import com.bookos.backend.book.dto.UserBookResponse;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.book.service.UserLibraryService;
import com.bookos.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class BookController {

    private final BookService bookService;
    private final UserLibraryService userLibraryService;

    @GetMapping
    public ApiResponse<List<BookResponse>> listBooks(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return ApiResponse.ok("Books loaded.", bookService.listBooks(authentication.getName(), q, category, tag));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            Authentication authentication,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book created.", bookService.createBook(authentication.getName(), request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBook(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Book loaded.", bookService.getBook(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return ApiResponse.ok("Book updated.", bookService.updateBook(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(Authentication authentication, @PathVariable Long id) {
        bookService.deleteBook(authentication.getName(), id);
        return ApiResponse.ok("Book deleted.");
    }

    @PostMapping("/{id}/add-to-library")
    public ResponseEntity<ApiResponse<UserBookResponse>> addToLibrary(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) AddToLibraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book added to personal library.", userLibraryService.addToLibrary(authentication.getName(), id, request)));
    }
}
