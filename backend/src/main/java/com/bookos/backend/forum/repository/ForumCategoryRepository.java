package com.bookos.backend.forum.repository;

import com.bookos.backend.forum.entity.ForumCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {

    List<ForumCategory> findAllByOrderBySortOrderAscNameAsc();

    Optional<ForumCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
