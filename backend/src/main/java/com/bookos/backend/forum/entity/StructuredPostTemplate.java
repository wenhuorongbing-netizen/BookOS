package com.bookos.backend.forum.entity;

import com.bookos.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "structured_post_templates")
public class StructuredPostTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(nullable = false)
    private String bodyMarkdownTemplate;

    @Column(length = 64)
    private String defaultRelatedEntityType;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
