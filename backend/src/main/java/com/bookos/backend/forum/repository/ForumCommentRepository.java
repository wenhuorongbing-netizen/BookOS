package com.bookos.backend.forum.repository;

import com.bookos.backend.forum.entity.ForumComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    long countByAuthorIdAndArchivedFalse(Long authorId);

    @EntityGraph(attributePaths = {"author", "author.profile"})
    List<ForumComment> findByThreadIdAndArchivedFalseOrderByCreatedAtAsc(Long threadId);

    @EntityGraph(attributePaths = {"thread", "thread.author", "author", "author.profile"})
    Optional<ForumComment> findByIdAndArchivedFalse(Long id);

    long countByThreadIdAndArchivedFalse(Long threadId);
}
