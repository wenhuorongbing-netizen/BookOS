package com.bookos.backend.forum.repository;

import com.bookos.backend.forum.entity.StructuredPostTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StructuredPostTemplateRepository extends JpaRepository<StructuredPostTemplate, Long> {

    List<StructuredPostTemplate> findAllByOrderBySortOrderAscNameAsc();

    boolean existsBySlug(String slug);
}
