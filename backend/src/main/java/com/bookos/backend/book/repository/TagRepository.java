package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlugIgnoreCase(String slug);
}
