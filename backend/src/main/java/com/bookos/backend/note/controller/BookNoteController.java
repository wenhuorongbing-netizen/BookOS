package com.bookos.backend.note.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.note.dto.BookNoteRequest;
import com.bookos.backend.note.dto.BookNoteResponse;
import com.bookos.backend.note.dto.NoteBlockRequest;
import com.bookos.backend.note.dto.NoteBlockResponse;
import com.bookos.backend.note.service.BookNoteService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class BookNoteController {

    private final BookNoteService noteService;

    @GetMapping("/api/books/{bookId}/notes")
    public ApiResponse<List<BookNoteResponse>> listBookNotes(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Notes loaded.", noteService.listBookNotes(authentication.getName(), bookId));
    }

    @PostMapping("/api/books/{bookId}/notes")
    public ResponseEntity<ApiResponse<BookNoteResponse>> createNote(
            Authentication authentication,
            @PathVariable Long bookId,
            @Valid @RequestBody BookNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Note created.", noteService.createNote(authentication.getName(), bookId, request)));
    }

    @GetMapping("/api/notes/{id}")
    public ApiResponse<BookNoteResponse> getNote(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Note loaded.", noteService.getNote(authentication.getName(), id));
    }

    @PutMapping("/api/notes/{id}")
    public ApiResponse<BookNoteResponse> updateNote(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookNoteRequest request) {
        return ApiResponse.ok("Note updated.", noteService.updateNote(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/notes/{id}")
    public ApiResponse<Void> archiveNote(Authentication authentication, @PathVariable Long id) {
        noteService.archiveNote(authentication.getName(), id);
        return ApiResponse.ok("Note archived.");
    }

    @PostMapping("/api/notes/{id}/blocks")
    public ResponseEntity<ApiResponse<NoteBlockResponse>> addBlock(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Note block created.", noteService.addBlock(authentication.getName(), id, request)));
    }

    @PutMapping("/api/note-blocks/{id}")
    public ApiResponse<NoteBlockResponse> updateBlock(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteBlockRequest request) {
        return ApiResponse.ok("Note block updated.", noteService.updateBlock(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/note-blocks/{id}")
    public ApiResponse<Void> deleteBlock(Authentication authentication, @PathVariable Long id) {
        noteService.deleteBlock(authentication.getName(), id);
        return ApiResponse.ok("Note block deleted.");
    }
}
