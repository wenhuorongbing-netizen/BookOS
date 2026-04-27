package com.bookos.backend.book.service;

import com.bookos.backend.book.dto.AddToLibraryRequest;
import com.bookos.backend.book.dto.UpdateUserBookProgressRequest;
import com.bookos.backend.book.dto.UpdateUserBookRatingRequest;
import com.bookos.backend.book.dto.UpdateUserBookStatusRequest;
import com.bookos.backend.book.dto.UserBookResponse;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLibraryService {

    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional
    public UserBookResponse addToLibrary(String email, Long bookId, AddToLibraryRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getBookEntity(bookId);
        UserBook existing = userBookRepository.findByUserIdAndBookId(user.getId(), bookId).orElse(null);
        if (existing != null) {
            return toUserBookResponse(existing);
        }

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setReadingStatus(request != null && request.readingStatus() != null
                ? request.readingStatus()
                : ReadingStatus.BACKLOG);
        userBook.setReadingFormat(request != null && request.readingFormat() != null
                ? request.readingFormat()
                : ReadingFormat.PHYSICAL);
        userBook.setOwnershipStatus(request != null && request.ownershipStatus() != null
                ? request.ownershipStatus()
                : OwnershipStatus.OWNED);

        return toUserBookResponse(userBookRepository.save(userBook));
    }

    @Transactional(readOnly = true)
    public List<UserBookResponse> listLibrary(String email) {
        User user = userService.getByEmailRequired(email);
        return userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(this::toUserBookResponse)
                .toList();
    }

    @Transactional
    public UserBookResponse updateStatus(String email, Long userBookId, UpdateUserBookStatusRequest request) {
        UserBook userBook = getOwnedUserBook(email, userBookId);
        userBook.setReadingStatus(request.status());
        return toUserBookResponse(userBookRepository.save(userBook));
    }

    @Transactional
    public UserBookResponse updateProgress(String email, Long userBookId, UpdateUserBookProgressRequest request) {
        UserBook userBook = getOwnedUserBook(email, userBookId);
        userBook.setProgressPercent(request.progressPercent());
        return toUserBookResponse(userBookRepository.save(userBook));
    }

    @Transactional
    public UserBookResponse updateRating(String email, Long userBookId, UpdateUserBookRatingRequest request) {
        UserBook userBook = getOwnedUserBook(email, userBookId);
        userBook.setRating(request.rating());
        return toUserBookResponse(userBookRepository.save(userBook));
    }

    @Transactional(readOnly = true)
    public List<UserBookResponse> currentlyReading(String email) {
        return filterByStatuses(email, List.of(ReadingStatus.CURRENTLY_READING));
    }

    @Transactional(readOnly = true)
    public List<UserBookResponse> fiveStar(String email) {
        User user = userService.getByEmailRequired(email);
        return userBookRepository.findByUserIdAndRatingOrderByUpdatedAtDesc(user.getId(), 5).stream()
                .map(this::toUserBookResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserBookResponse> antiLibrary(String email) {
        return filterByStatuses(email, List.of(ReadingStatus.ANTI_LIBRARY));
    }

    public UserBookResponse toUserBookResponse(UserBook userBook) {
        return new UserBookResponse(
                userBook.getId(),
                userBook.getBook().getId(),
                userBook.getBook().getTitle(),
                userBook.getBook().getSubtitle(),
                userBook.getBook().getCoverUrl(),
                userBook.getBook().getCategory(),
                userBook.getBook().getBookAuthors().stream().map(link -> link.getAuthor().getName()).toList(),
                userBook.getBook().getBookTags().stream().map(link -> link.getTag().getName()).toList(),
                userBook.getReadingStatus(),
                userBook.getReadingFormat(),
                userBook.getOwnershipStatus(),
                userBook.getProgressPercent(),
                userBook.getRating());
    }

    private List<UserBookResponse> filterByStatuses(String email, List<ReadingStatus> statuses) {
        User user = userService.getByEmailRequired(email);
        return userBookRepository.findByUserIdAndReadingStatusInOrderByUpdatedAtDesc(user.getId(), statuses).stream()
                .map(this::toUserBookResponse)
                .toList();
    }

    private UserBook getOwnedUserBook(String email, Long userBookId) {
        User user = userService.getByEmailRequired(email);
        return userBookRepository.findByIdAndUserId(userBookId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Library entry not found."));
    }
}
