package com.bookos.backend.capture.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "raw_captures")
public class RawCapture extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Book book;

    @Lob
    @Column(nullable = false)
    private String rawText;

    @Lob
    private String cleanText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NoteBlockType parsedType = NoteBlockType.NOTE;

    private Integer pageStart;

    private Integer pageEnd;

    @Lob
    private String tagsJson;

    @Lob
    private String conceptsJson;

    @Lob
    private String parserWarningsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CaptureStatus status = CaptureStatus.INBOX;

    @Column(length = 80)
    private String convertedEntityType;

    private Long convertedEntityId;
}
