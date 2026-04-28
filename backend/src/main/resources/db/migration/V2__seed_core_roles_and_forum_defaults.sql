INSERT INTO roles (created_at, updated_at, name) VALUES
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ADMIN'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MODERATOR'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'USER');

INSERT INTO forum_categories (created_at, updated_at, name, slug, description, sort_order) VALUES
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Book Discussions', 'book-discussions', 'Discuss books, chapters, reading paths, and source-backed interpretations.', 0),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Concept Discussions', 'concept-discussions', 'Connect concepts across notes, quotes, books, and design practice.', 10),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Design Lens Reviews', 'design-lens-reviews', 'Review and improve active design lenses.', 20),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Prototype Challenges', 'prototype-challenges', 'Turn reading insights into prototype tasks and constraints.', 30),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Project Critiques', 'project-critiques', 'Apply book knowledge to game projects and critique decisions.', 40),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Reading Groups', 'reading-groups', 'Coordinate reading plans, progress, and group reflection.', 50),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'General', 'general', 'General BookOS discussion that does not fit a structured category.', 60);

INSERT INTO structured_post_templates (
    created_at,
    updated_at,
    name,
    slug,
    description,
    body_markdown_template,
    default_related_entity_type,
    sort_order
) VALUES
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Book Discussion',
        'book-discussion',
        'Discuss a book, chapter, or reading claim.',
        '## Reading Context\n\n- Book:\n- Chapter/Page:\n\n## Core Claim\n\nWhat is the idea worth discussing?\n\n## Design Application\n\nHow could this change a project or prototype?',
        'BOOK',
        0
    ),
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Concept Discussion',
        'concept-discussion',
        'Connect a concept to books, notes, or examples.',
        '## Concept\n\nWhat does this concept mean in your words?\n\n## Sources\n\nWhich notes, quotes, or books support it?\n\n## Open Question\n\nWhat needs debate?',
        'CONCEPT',
        10
    ),
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Quote Discussion',
        'quote-discussion',
        'Discuss a source-backed quote without losing its origin.',
        '## Quote\n\n> Paste or reference the quote.\n\n## Interpretation\n\nWhat do you think it means?\n\n## Design Consequence\n\nWhat should change in practice?',
        'QUOTE',
        20
    ),
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Design Lens Review',
        'design-lens-review',
        'Evaluate whether a design lens is useful.',
        '## Lens\n\nWhat does this lens help you notice?\n\n## Strength\n\nWhere is it useful?\n\n## Weakness\n\nWhere can it mislead?',
        'DESIGN_LENS',
        30
    ),
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Prototype Challenge',
        'prototype-challenge',
        'Convert an idea into a playable experiment.',
        '## Challenge\n\nWhat should be prototyped?\n\n## Constraint\n\nWhat must stay small?\n\n## Success Signal\n\nWhat would prove the idea works?',
        'PROTOTYPE_TASK',
        40
    ),
    (
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'Project Critique',
        'project-critique',
        'Apply source-backed knowledge to a game project.',
        '## Project Context\n\nWhat is being designed?\n\n## Source Insight\n\nWhich book, note, or concept informs this critique?\n\n## Critique Request\n\nWhat feedback do you need?',
        'GAME_PROJECT',
        50
    );
