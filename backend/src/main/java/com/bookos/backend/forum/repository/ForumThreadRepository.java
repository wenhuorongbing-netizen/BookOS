package com.bookos.backend.forum.repository;

import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.forum.entity.ForumThread;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {

    @EntityGraph(attributePaths = {"category", "author", "author.profile", "relatedBook", "relatedConcept", "relatedProject"})
    List<ForumThread> findByStatusNotOrderByUpdatedAtDesc(ForumThreadStatus status);

    @EntityGraph(attributePaths = {"category", "author", "author.profile", "relatedBook", "relatedConcept", "relatedProject"})
    List<ForumThread> findByCategorySlugAndStatusNotOrderByUpdatedAtDesc(String slug, ForumThreadStatus status);

    @EntityGraph(attributePaths = {"category", "author", "author.profile", "relatedBook", "relatedConcept", "relatedProject"})
    Optional<ForumThread> findByIdAndStatusNot(Long id, ForumThreadStatus status);

    @EntityGraph(attributePaths = {"category", "author", "author.profile", "relatedBook", "relatedConcept", "relatedProject"})
    List<ForumThread> findByAuthorIdAndStatusNotOrderByUpdatedAtDesc(Long authorId, ForumThreadStatus status);

    long countByCategoryIdAndStatusNot(Long categoryId, ForumThreadStatus status);

    long countByStatusNotAndSourceReferenceIdIsNotNull(ForumThreadStatus status);
}
