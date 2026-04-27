package com.bookos.backend.book.service;

import com.bookos.backend.book.dto.BookRequest;
import com.bookos.backend.book.dto.BookResponse;
import com.bookos.backend.book.entity.Author;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.BookAuthor;
import com.bookos.backend.book.entity.BookTag;
import com.bookos.backend.book.entity.Tag;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.AuthorRepository;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.TagRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final UserBookRepository userBookRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BookResponse> listBooks(String email, String query, String category, String tag) {
        User user = userService.getByEmailRequired(email);
        List<Book> books = bookRepository.findAllByOrderByTitleAsc();
        Map<Long, UserBook> userBooksByBookId = mapUserBooks(user.getId());

        Stream<Book> stream = books.stream().filter(book -> canView(user, book));
        if (StringUtils.hasText(query)) {
            String q = query.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(book -> matchesQuery(book, q));
        }
        if (StringUtils.hasText(category)) {
            String wanted = category.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(book -> book.getCategory() != null && book.getCategory().toLowerCase(Locale.ROOT).equals(wanted));
        }
        if (StringUtils.hasText(tag)) {
            String wanted = tag.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(book -> book.getBookTags().stream()
                    .map(BookTag::getTag)
                    .map(Tag::getSlug)
                    .anyMatch(slug -> slug.equalsIgnoreCase(wanted)));
        }

        return stream.map(book -> toBookResponse(book, userBooksByBookId.get(book.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        Book book = getBookEntity(id);
        assertCanView(user, book);
        UserBook userBook = userBookRepository.findByUserIdAndBookId(user.getId(), id).orElse(null);
        return toBookResponse(book, userBook);
    }

    @Transactional
    public BookResponse createBook(String email, BookRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = new Book();
        book.setOwner(user);
        applyRequest(book, request);
        Book saved = bookRepository.save(book);
        return toBookResponse(saved, null);
    }

    @Transactional
    public BookResponse updateBook(String email, Long id, BookRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = getBookEntity(id);
        assertCanManage(user, book);
        applyRequest(book, request);
        Book saved = bookRepository.save(book);
        return toBookResponse(saved, null);
    }

    @Transactional
    public void deleteBook(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found.");
        }
        Book book = getBookEntity(id);
        assertCanManage(user, book);
        userBookRepository.deleteAllByBookId(id);
        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Book getBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found."));
    }

    @Transactional(readOnly = true)
    public Book getAccessibleBookEntity(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        Book book = getBookEntity(id);
        assertCanView(user, book);
        return book;
    }

    public BookResponse toBookResponse(Book book, UserBook userBook) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getSubtitle(),
                book.getDescription(),
                book.getIsbn(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getCoverUrl(),
                book.getCategory(),
                book.getVisibility(),
                book.getBookAuthors().stream().map(link -> link.getAuthor().getName()).toList(),
                book.getBookTags().stream().map(link -> link.getTag().getName()).toList(),
                userBook != null,
                userBook != null ? userBook.getId() : null,
                userBook != null ? userBook.getReadingStatus() : null,
                userBook != null ? userBook.getReadingFormat() : null,
                userBook != null ? userBook.getOwnershipStatus() : null,
                userBook != null ? userBook.getProgressPercent() : null,
                userBook != null ? userBook.getRating() : null);
    }

    private Map<Long, UserBook> mapUserBooks(Long userId) {
        Map<Long, UserBook> result = new LinkedHashMap<>();
        for (UserBook userBook : userBookRepository.findByUserIdOrderByUpdatedAtDesc(userId)) {
            result.put(userBook.getBook().getId(), userBook);
        }
        return result;
    }

    private boolean matchesQuery(Book book, String query) {
        return Stream.of(book.getTitle(), book.getSubtitle(), book.getCategory(), book.getDescription())
                        .filter(Objects::nonNull)
                        .map(value -> value.toLowerCase(Locale.ROOT))
                        .anyMatch(value -> value.contains(query))
                || book.getBookAuthors().stream()
                        .map(link -> link.getAuthor().getName().toLowerCase(Locale.ROOT))
                        .anyMatch(name -> name.contains(query))
                || book.getBookTags().stream()
                        .map(link -> link.getTag().getName().toLowerCase(Locale.ROOT))
                        .anyMatch(name -> name.contains(query));
    }

    private void assertCanView(User user, Book book) {
        if (!canView(user, book)) {
            throw new AccessDeniedException("You are not allowed to access this book.");
        }
    }

    private boolean canView(User user, Book book) {
        return isAdmin(user)
                || isOwner(user, book)
                || book.getVisibility() == Visibility.PUBLIC
                || book.getVisibility() == Visibility.SHARED;
    }

    private void assertCanManage(User user, Book book) {
        if (!isAdmin(user) && !isOwner(user, book)) {
            throw new AccessDeniedException("You are not allowed to modify this book.");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null && user.getRole().getName() == com.bookos.backend.common.enums.RoleName.ADMIN;
    }

    private boolean isOwner(User user, Book book) {
        return book.getOwner() != null && Objects.equals(book.getOwner().getId(), user.getId());
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.title().trim());
        book.setSubtitle(trimToNull(request.subtitle()));
        book.setDescription(trimToNull(request.description()));
        book.setIsbn(trimToNull(request.isbn()));
        book.setPublisher(trimToNull(request.publisher()));
        book.setPublicationYear(request.publicationYear());
        book.setCoverUrl(trimToNull(request.coverUrl()));
        book.setCategory(trimToNull(request.category()));
        book.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());

        replaceAuthors(book, request.authors());
        replaceTags(book, request.tags());
    }

    private void replaceAuthors(Book book, List<String> authorNames) {
        List<String> cleaned = dedupe(authorNames);
        List<BookAuthor> desiredLinks = new ArrayList<>();
        for (int i = 0; i < cleaned.size(); i++) {
            String name = cleaned.get(i);
            Author author = authorRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                Author created = new Author();
                created.setName(name);
                created.setSlug(SlugUtils.slugify(name));
                return authorRepository.save(created);
            });

            BookAuthor link = book.getBookAuthors().stream()
                    .filter(existing -> existing.getAuthor().getId().equals(author.getId()))
                    .findFirst()
                    .orElseGet(BookAuthor::new);
            link.setBook(book);
            link.setAuthor(author);
            link.setDisplayOrder(i);
            desiredLinks.add(link);
        }
        book.getBookAuthors().clear();
        book.getBookAuthors().addAll(desiredLinks);
    }

    private void replaceTags(Book book, List<String> tagNames) {
        Set<BookTag> desiredLinks = new LinkedHashSet<>();
        for (String name : dedupe(tagNames)) {
            String slug = SlugUtils.slugify(name);
            Tag tag = tagRepository.findBySlugIgnoreCase(slug).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(name);
                created.setSlug(slug);
                return tagRepository.save(created);
            });

            BookTag link = book.getBookTags().stream()
                    .filter(existing -> existing.getTag().getId().equals(tag.getId()))
                    .findFirst()
                    .orElseGet(BookTag::new);
            link.setBook(book);
            link.setTag(tag);
            desiredLinks.add(link);
        }
        book.getBookTags().clear();
        book.getBookTags().addAll(desiredLinks);
    }

    private List<String> dedupe(List<String> input) {
        List<String> values = new ArrayList<>();
        if (input == null) {
            return values;
        }
        for (String value : input) {
            String cleaned = trimToNull(value);
            if (cleaned != null && values.stream().noneMatch(existing -> existing.equalsIgnoreCase(cleaned))) {
                values.add(cleaned);
            }
        }
        return values;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
