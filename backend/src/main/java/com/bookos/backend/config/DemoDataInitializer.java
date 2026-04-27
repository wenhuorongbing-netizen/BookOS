package com.bookos.backend.config;

import com.bookos.backend.book.dto.BookRequest;
import com.bookos.backend.book.dto.UpdateUserBookProgressRequest;
import com.bookos.backend.book.dto.UpdateUserBookRatingRequest;
import com.bookos.backend.book.dto.UpdateUserBookStatusRequest;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.book.service.UserLibraryService;
import com.bookos.backend.common.enums.RoleName;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.forum.service.ForumService;
import com.bookos.backend.user.entity.Role;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.entity.UserProfile;
import com.bookos.backend.user.repository.RoleRepository;
import com.bookos.backend.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final SeedProperties seedProperties;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BookService bookService;
    private final UserLibraryService userLibraryService;
    private final ForumService forumService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.ADMIN);
            return roleRepository.save(role);
        });
        Role userRole = roleRepository.findByName(RoleName.USER).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.USER);
            return roleRepository.save(role);
        });
        roleRepository.findByName(RoleName.MODERATOR).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.MODERATOR);
            return roleRepository.save(role);
        });

        User admin = userRepository.findByEmailIgnoreCase("admin@bookos.local").orElseGet(() -> createUser(
                "admin@bookos.local",
                "Admin123!",
                "BookOS Admin",
                adminRole));

        User designer = userRepository.findByEmailIgnoreCase("designer@bookos.local").orElseGet(() -> createUser(
                "designer@bookos.local",
                "Password123!",
                "Lead Designer",
                userRole));

        if (bookRepository.count() == 0) {
            seedBooks(admin.getEmail());
        }

        if (userBookRepository.count() == 0) {
            seedLibrary(designer.getEmail());
        }

        forumService.ensureDefaults();
    }

    private User createUser(String email, String password, String displayName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);

        UserProfile profile = new UserProfile();
        profile.setDisplayName(displayName);
        user.setProfile(profile);
        return userRepository.save(user);
    }

    private void seedBooks(String ownerEmail) {
        List<BookRequest> books = List.of(
                new BookRequest("Game Design Workshop", "A Playcentric Approach to Creating Innovative Games", "Iterative playcentric production guide.", null, "CRC Press", 2021, "https://covers.openlibrary.org/b/id/14622063-L.jpg", "Production", Visibility.PUBLIC, List.of("Tracy Fullerton"), List.of("playtesting", "iteration", "production")),
                new BookRequest("The Art of Game Design", "A Book of Lenses", "Foundational design lenses and experiential thinking.", null, "CRC Press", 2019, "https://covers.openlibrary.org/b/id/8231856-L.jpg", "Core Theory", Visibility.PUBLIC, List.of("Jesse Schell"), List.of("lenses", "player-experience", "systems")),
                new BookRequest("Game Feel", "A Game Designer's Guide to Virtual Sensation", "Tactile control, juice, and responsive interaction.", null, "Morgan Kaufmann", 2008, "https://covers.openlibrary.org/b/id/5546156-L.jpg", "Experience Design", Visibility.PUBLIC, List.of("Steve Swink"), List.of("game-feel", "feedback", "juice")),
                new BookRequest("Advanced Game Design", "A Systems Approach", "Formal system design and balancing structure.", null, "Addison-Wesley", 2017, "https://covers.openlibrary.org/b/id/8756020-L.jpg", "Systems", Visibility.PUBLIC, List.of("Michael Sellers"), List.of("systems", "balance", "formalism")),
                new BookRequest("Challenges for Game Designers", null, "Design exercises and constraint-based creativity practice.", null, "Course Technology", 2009, "https://covers.openlibrary.org/b/id/10594788-L.jpg", "Exercises", Visibility.PUBLIC, List.of("Brenda Romero", "Ian Schreiber"), List.of("exercises", "constraints", "ideation")),
                new BookRequest("Play Matters", null, "Critical perspectives on play and meaning.", null, "MIT Press", 2018, "https://covers.openlibrary.org/b/id/9351377-L.jpg", "Play Studies", Visibility.PUBLIC, List.of("Miguel Sicart"), List.of("play", "ethics", "critique")),
                new BookRequest("The Aesthetic of Play", null, "How play creates aesthetic value and experience.", null, "MIT Press", 2015, "https://covers.openlibrary.org/b/id/10940175-L.jpg", "Play Studies", Visibility.PUBLIC, List.of("Brian Upton"), List.of("aesthetics", "play", "experience")),
                new BookRequest("The Game Design Reader", null, "Collected essays and foundational scholarship.", null, "MIT Press", 2023, "https://covers.openlibrary.org/b/id/14622649-L.jpg", "Reference", Visibility.PUBLIC, List.of("Katie Salen Tekinbas", "Eric Zimmerman"), List.of("reference", "theory", "history")),
                new BookRequest("A Theory of Fun for Game Design", null, "Fun as pattern learning and cognitive engagement.", null, "Paraglyph Press", 2004, "https://covers.openlibrary.org/b/id/240726-L.jpg", "Core Theory", Visibility.PUBLIC, List.of("Raph Koster"), List.of("fun", "learning", "motivation")),
                new BookRequest("Level Up!", "The Guide to Great Video Game Design", "Genre structures, level design, and feature thinking.", null, "Wiley", 2010, "https://covers.openlibrary.org/b/id/6979863-L.jpg", "Production", Visibility.PUBLIC, List.of("Scott Rogers"), List.of("levels", "genres", "production")),
                new BookRequest("Game Mechanics", "Advanced Game Design", "Probability, economy, and system mechanics design.", null, "New Riders", 2012, "https://covers.openlibrary.org/b/id/11098515-L.jpg", "Systems", Visibility.PUBLIC, List.of("Ernest Adams", "Joris Dormans"), List.of("mechanics", "economy", "probability")),
                new BookRequest("Characteristics of Games", null, "Formal properties of games and player decisions.", null, "MIT Press", 2020, "https://covers.openlibrary.org/b/id/10438384-L.jpg", "Core Theory", Visibility.PUBLIC, List.of("George Skaff Elias", "Richard Garfield", "K. Robert Gutschera"), List.of("formalism", "decision-making", "strategy")));

        for (BookRequest request : books) {
            bookService.createBook(ownerEmail, request);
        }
    }

    private void seedLibrary(String email) {
        List<Book> books = bookRepository.findAllByOrderByTitleAsc();
        if (books.isEmpty()) {
            return;
        }

        var artOfGameDesign = books.stream().filter(book -> book.getTitle().equals("The Art of Game Design")).findFirst().orElse(null);
        var gameFeel = books.stream().filter(book -> book.getTitle().equals("Game Feel")).findFirst().orElse(null);
        var theoryOfFun = books.stream().filter(book -> book.getTitle().equals("A Theory of Fun for Game Design")).findFirst().orElse(null);

        if (artOfGameDesign != null) {
            var libraryEntry = userLibraryService.addToLibrary(email, artOfGameDesign.getId(), null);
            userLibraryService.updateStatus(email, libraryEntry.id(), new UpdateUserBookStatusRequest(ReadingStatus.CURRENTLY_READING));
            userLibraryService.updateProgress(email, libraryEntry.id(), new UpdateUserBookProgressRequest(62));
            userLibraryService.updateRating(email, libraryEntry.id(), new UpdateUserBookRatingRequest(5));
        }

        if (gameFeel != null) {
            var libraryEntry = userLibraryService.addToLibrary(email, gameFeel.getId(), null);
            userLibraryService.updateStatus(email, libraryEntry.id(), new UpdateUserBookStatusRequest(ReadingStatus.COMPLETED));
            userLibraryService.updateProgress(email, libraryEntry.id(), new UpdateUserBookProgressRequest(100));
            userLibraryService.updateRating(email, libraryEntry.id(), new UpdateUserBookRatingRequest(5));
        }

        if (theoryOfFun != null) {
            var libraryEntry = userLibraryService.addToLibrary(email, theoryOfFun.getId(), null);
            userLibraryService.updateStatus(email, libraryEntry.id(), new UpdateUserBookStatusRequest(ReadingStatus.ANTI_LIBRARY));
        }
    }
}
