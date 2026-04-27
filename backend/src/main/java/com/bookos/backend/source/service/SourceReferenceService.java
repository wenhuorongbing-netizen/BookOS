package com.bookos.backend.source.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SourceReferenceService {

    private final SourceReferenceRepository sourceReferenceRepository;

    @Transactional
    public SourceReference createForNoteBlock(User user, Book book, BookNote note, NoteBlock block, ParsedNoteResponse parsed) {
        SourceReference sourceReference = new SourceReference();
        sourceReference.setUser(user);
        sourceReference.setBook(book);
        sourceReference.setNote(note);
        sourceReference.setNoteBlock(block);
        sourceReference.setSourceType("NOTE_BLOCK");
        sourceReference.setPageStart(parsed.pageStart());
        sourceReference.setPageEnd(parsed.pageEnd());
        sourceReference.setLocationLabel(buildLocationLabel(parsed));
        sourceReference.setSourceText(parsed.rawText());
        sourceReference.setSourceConfidence(parsed.pageStart() == null ? SourceConfidence.LOW : SourceConfidence.HIGH);
        return sourceReferenceRepository.save(sourceReference);
    }

    @Transactional
    public void replaceForNoteBlock(User user, Book book, BookNote note, NoteBlock block, ParsedNoteResponse parsed) {
        sourceReferenceRepository.deleteByNoteBlockId(block.getId());
        createForNoteBlock(user, book, note, block, parsed);
    }

    public SourceReferenceResponse toResponse(SourceReference sourceReference) {
        return new SourceReferenceResponse(
                sourceReference.getId(),
                sourceReference.getSourceType(),
                sourceReference.getBook().getId(),
                sourceReference.getNote() != null ? sourceReference.getNote().getId() : null,
                sourceReference.getNoteBlock() != null ? sourceReference.getNoteBlock().getId() : null,
                sourceReference.getRawCaptureId(),
                sourceReference.getPageStart(),
                sourceReference.getPageEnd(),
                sourceReference.getLocationLabel(),
                sourceReference.getSourceText(),
                sourceReference.getSourceConfidence());
    }

    private String buildLocationLabel(ParsedNoteResponse parsed) {
        if (parsed.pageStart() == null) {
            return "Note block";
        }
        if (parsed.pageEnd() != null) {
            return "p." + parsed.pageStart() + "-" + parsed.pageEnd();
        }
        return "p." + parsed.pageStart();
    }
}
