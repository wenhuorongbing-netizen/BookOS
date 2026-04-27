package com.bookos.backend.note.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.note.dto.BookNoteRequest;
import com.bookos.backend.note.dto.BookNoteResponse;
import com.bookos.backend.note.dto.NoteBlockRequest;
import com.bookos.backend.note.dto.NoteBlockResponse;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.note.repository.NoteBlockRepository;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import com.bookos.backend.parser.service.NoteParserService;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookNoteService {

    private final BookNoteRepository bookNoteRepository;
    private final NoteBlockRepository noteBlockRepository;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;
    private final NoteParserService parserService;
    private final SourceReferenceService sourceReferenceService;

    @Transactional(readOnly = true)
    public List<BookNoteResponse> listBookNotes(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        assertBookInLibrary(user.getId(), bookId);
        return bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookNoteResponse getNote(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getNoteForUser(id, user.getId()));
    }

    @Transactional
    public BookNoteResponse createNote(String email, Long bookId, BookNoteRequest request) {
        User user = userService.getByEmailRequired(email);
        assertBookInLibrary(user.getId(), bookId);
        Book book = bookService.getBookEntity(bookId);

        BookNote note = new BookNote();
        note.setUser(user);
        note.setBook(book);
        applyRequest(note, request);
        note.setThreeSentenceSummary(buildSummary(request.markdown()));

        NoteBlock block = buildBlock(user, book, note, new NoteBlockRequest(request.markdown(), 0));
        note.getBlocks().add(block);

        BookNote saved = bookNoteRepository.save(note);
        for (NoteBlock savedBlock : saved.getBlocks()) {
            ParsedNoteResponse parsed = parserService.parse(savedBlock.getRawText());
            sourceReferenceService.createForNoteBlock(user, book, saved, savedBlock, parsed);
        }

        return toResponse(saved);
    }

    @Transactional
    public BookNoteResponse updateNote(String email, Long id, BookNoteRequest request) {
        User user = userService.getByEmailRequired(email);
        BookNote note = getNoteForUser(id, user.getId());
        applyRequest(note, request);
        note.setThreeSentenceSummary(buildSummary(request.markdown()));
        return toResponse(bookNoteRepository.save(note));
    }

    @Transactional
    public void archiveNote(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        BookNote note = getNoteForUser(id, user.getId());
        note.setArchived(true);
        bookNoteRepository.save(note);
    }

    @Transactional
    public NoteBlockResponse addBlock(String email, Long noteId, NoteBlockRequest request) {
        User user = userService.getByEmailRequired(email);
        BookNote note = getNoteForUser(noteId, user.getId());
        NoteBlock block = buildBlock(user, note.getBook(), note, request);
        note.getBlocks().add(block);
        BookNote saved = bookNoteRepository.save(note);
        NoteBlock savedBlock = saved.getBlocks().get(saved.getBlocks().size() - 1);
        ParsedNoteResponse parsed = parserService.parse(savedBlock.getRawText());
        sourceReferenceService.createForNoteBlock(user, saved.getBook(), saved, savedBlock, parsed);
        return toBlockResponse(savedBlock);
    }

    @Transactional
    public NoteBlockResponse updateBlock(String email, Long blockId, NoteBlockRequest request) {
        User user = userService.getByEmailRequired(email);
        NoteBlock block = noteBlockRepository.findByIdAndUserId(blockId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Note block not found."));
        applyParsedBlock(block, request.rawText());
        if (request.sortOrder() != null) {
            block.setSortOrder(Math.max(0, request.sortOrder()));
        }
        NoteBlock saved = noteBlockRepository.save(block);
        ParsedNoteResponse parsed = parserService.parse(saved.getRawText());
        sourceReferenceService.replaceForNoteBlock(user, saved.getBook(), saved.getNote(), saved, parsed);
        return toBlockResponse(saved);
    }

    @Transactional
    public void deleteBlock(String email, Long blockId) {
        User user = userService.getByEmailRequired(email);
        NoteBlock block = noteBlockRepository.findByIdAndUserId(blockId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Note block not found."));
        noteBlockRepository.delete(block);
    }

    private BookNote getNoteForUser(Long id, Long userId) {
        return bookNoteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Note not found."));
    }

    private void assertBookInLibrary(Long userId, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(userId, bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before creating notes.");
        }
    }

    private void applyRequest(BookNote note, BookNoteRequest request) {
        note.setTitle(request.title().trim());
        note.setMarkdown(request.markdown().trim());
        note.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());
    }

    private NoteBlock buildBlock(User user, Book book, BookNote note, NoteBlockRequest request) {
        NoteBlock block = new NoteBlock();
        block.setUser(user);
        block.setBook(book);
        block.setNote(note);
        block.setSortOrder(request.sortOrder() == null ? note.getBlocks().size() : Math.max(0, request.sortOrder()));
        applyParsedBlock(block, request.rawText());
        return block;
    }

    private void applyParsedBlock(NoteBlock block, String rawText) {
        ParsedNoteResponse parsed = parserService.parse(rawText);
        block.setRawText(parsed.rawText());
        block.setMarkdown(parsed.rawText());
        block.setPlainText(parsed.cleanText());
        block.setBlockType(parsed.type());
        block.setPageStart(parsed.pageStart());
        block.setPageEnd(parsed.pageEnd());
        block.setParserWarnings(String.join("\n", parsed.warnings()));
    }

    private BookNoteResponse toResponse(BookNote note) {
        return new BookNoteResponse(
                note.getId(),
                note.getBook().getId(),
                note.getBook().getTitle(),
                note.getTitle(),
                note.getMarkdown(),
                note.getThreeSentenceSummary(),
                note.getVisibility(),
                note.isArchived(),
                note.getBlocks().stream().map(this::toBlockResponse).toList(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }

    private NoteBlockResponse toBlockResponse(NoteBlock block) {
        return new NoteBlockResponse(
                block.getId(),
                block.getNote().getId(),
                block.getBook().getId(),
                block.getBlockType(),
                block.getRawText(),
                block.getMarkdown(),
                block.getPlainText(),
                block.getSortOrder(),
                block.getPageStart(),
                block.getPageEnd(),
                splitWarnings(block.getParserWarnings()),
                block.getSourceReferences().stream().map(sourceReferenceService::toResponse).toList(),
                block.getCreatedAt(),
                block.getUpdatedAt());
    }

    private List<String> splitWarnings(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("\\n")).filter(StringUtils::hasText).toList();
    }

    private String buildSummary(String markdown) {
        String plain = parserService.parse(markdown).cleanText();
        if (!StringUtils.hasText(plain)) {
            return null;
        }
        String[] sentences = plain.split("(?<=[.!?])\\s+");
        return Arrays.stream(sentences)
                .filter(StringUtils::hasText)
                .limit(3)
                .reduce((left, right) -> left + " " + right)
                .orElse(plain);
    }
}
