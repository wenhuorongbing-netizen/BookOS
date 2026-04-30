# Quick Capture Guide

Quick Capture is the fastest way to turn a reading thought into source-backed BookOS data. It saves the raw text, parses the syntax, and keeps the selected book as the source context.

## Core Rule

Write the thought first. Add page, tag, and concept metadata only when you know it.

Unknown pages must stay unknown. Do not invent page numbers.

## Supported Markers

- `💬` Quote
- `✅` Action item
- `💡` Idea or inspiration
- `🧩` Related concept
- `❓` Question
- `📌` Important
- `🧪` Experiment
- `🔗` Link to another idea

## Page Markers

BookOS accepts common English and Chinese page markers:

- `p.42`
- `page 42`
- `页42`
- `第42页`

If a page is unknown, leave it out. The source reference will keep `pageStart = null` and `pageEnd = null`.

## Tags

Use `#tag` for lightweight grouping:

```text
#quote
#todo
#prototype
#game-feel
```

## Concepts

Use double brackets for concepts:

```text
[[Core Loop]]
[[Feedback Loop]]
[[Meaningful Choice]]
```

Parsed concepts are not automatically converted into canonical concepts. Review them before saving concepts.

## Safe Examples

These examples are original sample text and are not copyrighted book passages.

```text
💬 p.42 "Readable feedback matters in a prototype." #quote [[Game Feel]]
```

```text
✅ page 80 Playtest the feedback loop tomorrow. #todo [[Feedback Loop]]
```

```text
💡 p.12 This could become a prototype task. #prototype [[Core Loop]]
```

```text
🧩 第42页 Connect this to [[Meaningful Choice]] #concept
```

```text
❓ 页18 Why does this mechanic create tension? #question [[Risk vs Reward]]
```

## Beginner Mode

Beginner Mode lets users choose:

- Type
- Page or location
- Content
- Tags
- Concepts

BookOS then generates the equivalent raw capture text. The generated text is not saved until the user presses Capture.

## Live Parser Preview

The preview shows:

- Parsed type
- Page or page unknown
- Tags
- Concepts
- Source reference preview
- Parser warnings

Malformed page markers should be corrected before saving. If the page is unknown, remove the page marker instead of guessing.

## After Saving

After saving a capture, choose one next step:

- Convert to Note
- Convert to Quote
- Convert to Action
- Review Concept
- Open Inbox

This keeps the reading loop short: capture, parse, convert, reopen source.
