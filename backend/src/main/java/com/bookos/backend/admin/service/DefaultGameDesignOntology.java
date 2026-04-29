package com.bookos.backend.admin.service;

import com.bookos.backend.admin.dto.OntologyImportRequest;
import com.bookos.backend.admin.dto.OntologySeedBookRequest;
import com.bookos.backend.admin.dto.OntologySeedConceptRequest;
import com.bookos.backend.admin.dto.OntologySeedKnowledgeObjectRequest;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import java.util.List;

final class DefaultGameDesignOntology {

    private DefaultGameDesignOntology() {}

    static OntologyImportRequest request() {
        return new OntologyImportRequest(books(), concepts(), knowledgeObjects());
    }

    private static List<OntologySeedBookRequest> books() {
        return List.of(
                book("The Art of Game Design", "A Book of Lenses", "A practical lens-based approach for reasoning about player experience, goals, constraints, and design decisions.", "CRC Press", 2019, "Design Lenses", List.of("Jesse Schell"), List.of("lenses", "experience", "design-thinking")),
                book("Game Design Workshop", "A Playcentric Approach to Creating Innovative Games", "A production-oriented guide to iterative design, prototyping, playtesting, and reflective improvement.", "CRC Press", 2021, "Practice & Exercises", List.of("Tracy Fullerton"), List.of("playtest", "iteration", "prototype")),
                book("Game Feel", "A Game Designer's Guide to Virtual Sensation", "A design vocabulary for responsive controls, real-time interaction, audiovisual feedback, and the sensation of play.", "Morgan Kaufmann", 2008, "Game Feel", List.of("Steve Swink"), List.of("game-feel", "control", "feedback")),
                book("Advanced Game Design", "A Systems Approach", "A systems-centered framing for mechanics, economies, feedback structures, emergence, and tuning.", "Addison-Wesley", 2017, "Systems & Loops", List.of("Michael Sellers"), List.of("systems", "balance", "economy")),
                book("Challenges for Game Designers", null, "A practice collection for building design skill through constraints, exercises, and prototype challenges.", "Course Technology", 2009, "Practice & Exercises", List.of("Brenda Romero", "Ian Schreiber"), List.of("exercises", "constraints", "practice")),
                book("Play Matters", null, "A critical perspective on play as a human activity shaped by context, meaning, ethics, and expression.", "MIT Press", 2018, "Play & Fun", List.of("Miguel Sicart"), List.of("play", "meaning", "critique")),
                book("The Aesthetic of Play", null, "A perspective on play as aesthetic experience: anticipation, interpretation, agency, and felt meaning.", "MIT Press", 2015, "Player Experience", List.of("Brian Upton"), List.of("aesthetics", "play", "experience")),
                book("The Game Design Reader", null, "A broad reference anthology useful for connecting design practice to historical and theoretical contexts.", "MIT Press", 2023, "Topics & Reader", List.of("Katie Salen Tekinbas", "Eric Zimmerman"), List.of("reference", "theory", "history")),
                book("A Theory of Fun for Game Design", null, "A compact theory connecting fun, learning, pattern recognition, mastery, and player cognition.", "Paraglyph Press", 2004, "Play & Fun", List.of("Raph Koster"), List.of("fun", "learning", "patterns")),
                book("Characteristics of Games", null, "A formal vocabulary for comparing games through goals, decisions, uncertainty, incentives, and player interaction.", "MIT Press", 2020, "Systems & Loops", List.of("George Skaff Elias", "Richard Garfield", "K. Robert Gutschera"), List.of("formalism", "decisions", "systems")),
                book("Level Up", "The Guide to Great Video Game Design", "A production-friendly overview of game concepts, genre conventions, features, levels, and practical design communication.", "Wiley", 2010, "Projects & Application", List.of("Scott Rogers"), List.of("production", "levels", "features")),
                book("Game Mechanics: Advanced Game Design", null, "A mechanics-focused treatment of economies, probabilities, feedback systems, and systemic game structures.", "New Riders", 2012, "Systems & Loops", List.of("Ernest Adams", "Joris Dormans"), List.of("mechanics", "economy", "systems")));
    }

    private static List<OntologySeedConceptRequest> concepts() {
        return List.of(
                concept("Play", "Voluntary, situated activity where players explore possibility, constraints, meaning, and agency.", "Play & Fun", "Play Matters", "play", "agency"),
                concept("Fun", "A positive form of engagement often produced by learning, surprise, mastery, social energy, or expressive freedom.", "Play & Fun", "A Theory of Fun for Game Design", "fun", "learning"),
                concept("Game Feel", "The perceived quality of controlling a game moment through input response, feedback, timing, and audiovisual reinforcement.", "Game Feel", "Game Feel", "feel", "controls"),
                concept("Real-Time Control", "The loop where player input, simulation response, feedback, and correction happen continuously.", "Game Feel", "Game Feel", "control", "input"),
                concept("Simulated Space", "A coherent playable environment whose rules, feedback, and affordances make movement and interaction legible.", "Game Feel", "Game Feel", "space", "simulation"),
                concept("Prototype", "A small playable artifact built to answer a design question with less risk than full production.", "Practice & Exercises", "Game Design Workshop", "prototype", "experiment"),
                concept("Playtest", "A structured observation of players using a build so design assumptions can be tested against actual behavior.", "Practice & Exercises", "Game Design Workshop", "playtest", "feedback"),
                concept("Iteration", "A repeated cycle of making, testing, learning, and revising toward stronger player experience.", "Practice & Exercises", "Game Design Workshop", "iteration", "process"),
                concept("Essential Experience", "The core emotional or experiential target a design should preserve across mechanics, presentation, and pacing.", "Player Experience", "The Art of Game Design", "experience", "intent"),
                concept("Design Lens", "A focused question or perspective that helps evaluate a game from a specific design angle.", "Design Lenses", "The Art of Game Design", "lens", "question"),
                concept("Core Loop", "The repeatable cycle of player action, system response, reward, and renewed motivation.", "Systems & Loops", "Advanced Game Design", "loop", "systems"),
                concept("Feedback Loop", "A structure where outcomes feed back into future choices, changing momentum, difficulty, or strategy.", "Systems & Loops", "Game Mechanics: Advanced Game Design", "feedback", "systems"),
                concept("Meaningful Choice", "A decision where options are understandable, consequences matter, and player intent can shape the outcome.", "Player Experience", "The Art of Game Design", "choice", "agency"),
                concept("Risk vs Reward", "A design relationship where larger possible gains are balanced by uncertainty, cost, exposure, or loss.", "Systems & Loops", "Characteristics of Games", "risk", "reward"),
                concept("Mastery", "The player's growing ability to understand patterns, execute skills, and make better decisions over time.", "Player Experience", "A Theory of Fun for Game Design", "mastery", "learning"),
                concept("Pattern Recognition", "The cognitive process of noticing structure in problems, systems, feedback, and player situations.", "Play & Fun", "A Theory of Fun for Game Design", "patterns", "learning"),
                concept("Player Motivation", "The reasons players continue: curiosity, mastery, expression, competition, social connection, fantasy, or progress.", "Player Experience", "The Art of Game Design", "motivation", "engagement"),
                concept("Emergence", "Complex or surprising play that arises from interactions among simpler rules, systems, and player choices.", "Systems & Loops", "Advanced Game Design", "emergence", "systems"),
                concept("Balance", "The tuning of options, costs, rewards, difficulty, and dominant strategies toward intended experience.", "Systems & Loops", "Game Mechanics: Advanced Game Design", "balance", "tuning"),
                concept("Juiciness", "Extra feedback polish that makes actions feel satisfying, clear, and emotionally reinforced.", "Game Feel", "Game Feel", "juice", "feedback"),
                concept("Systems Thinking", "Understanding a game as interacting parts with dependencies, feedback, constraints, and dynamic behavior.", "Systems & Loops", "Advanced Game Design", "systems", "modeling"),
                concept("Internal Economy", "A resource system where sources, sinks, converters, and feedback shape player decisions.", "Systems & Loops", "Game Mechanics: Advanced Game Design", "economy", "resources"),
                concept("Progression Curve", "The planned change in challenge, complexity, reward, and mastery across play time.", "Projects & Application", "Level Up", "progression", "pacing"),
                concept("Mental Model", "The player's working understanding of how the game behaves and what actions are likely to matter.", "Player Experience", "The Art of Game Design", "model", "learning"));
    }

    private static List<OntologySeedKnowledgeObjectRequest> knowledgeObjects() {
        return List.of(
                ko(KnowledgeObjectType.DESIGN_LENS, "Lens of Meaningful Choice", "Ask whether the player can compare options, predict enough consequence to act intentionally, and care about the result.", "Design Lenses", "The Art of Game Design", "Meaningful Choice", "lens", "choice"),
                ko(KnowledgeObjectType.DESIGN_LENS, "Lens of Game Feel", "Ask whether input, timing, response, feedback, camera, sound, and animation produce the intended sensation.", "Game Feel", "Game Feel", "Game Feel", "lens", "feel"),
                ko(KnowledgeObjectType.DESIGN_LENS, "Lens of Core Loop", "Ask whether the central repeatable action cycle creates motivation to continue rather than only mechanical repetition.", "Systems & Loops", "Advanced Game Design", "Core Loop", "lens", "loop"),
                ko(KnowledgeObjectType.DIAGNOSTIC_QUESTION, "What does the player learn every minute?", "Use this question to test whether the design creates visible patterns, useful feedback, and a path toward mastery.", "Play & Fun", "A Theory of Fun for Game Design", "Mastery", "diagnostic", "learning"),
                ko(KnowledgeObjectType.DIAGNOSTIC_QUESTION, "Where can feedback become misleading?", "Identify moments where audiovisual polish, UI, scoring, or rewards might teach the wrong mental model.", "Player Experience", "The Art of Game Design", "Mental Model", "diagnostic", "feedback"),
                ko(KnowledgeObjectType.DIAGNOSTIC_QUESTION, "Which choice is currently dominant?", "Find the option that rational players will overuse, then inspect cost, risk, timing, counters, and information.", "Systems & Loops", "Characteristics of Games", "Balance", "diagnostic", "balance"),
                ko(KnowledgeObjectType.EXERCISE, "One-Mechanic Prototype", "Build a playable test around one verb, one goal, and one feedback rule. Stop before adding content.", "Practice & Exercises", "Challenges for Game Designers", "Prototype", "exercise", "prototype"),
                ko(KnowledgeObjectType.EXERCISE, "Playtest Observation Pass", "Run a short playtest and record only observable behavior before writing interpretations or solutions.", "Practice & Exercises", "Game Design Workshop", "Playtest", "exercise", "playtest"),
                ko(KnowledgeObjectType.EXERCISE, "Economy Source-Sink Map", "List every source, sink, converter, and storage point in a resource loop, then mark where runaway growth can occur.", "Systems & Loops", "Game Mechanics: Advanced Game Design", "Internal Economy", "exercise", "economy"),
                ko(KnowledgeObjectType.PROTOTYPE_TASK, "Prototype a Feedback Loop", "Create a small build where one player action changes a value that affects the next decision within thirty seconds.", "Systems & Loops", "Game Mechanics: Advanced Game Design", "Feedback Loop", "prototype", "feedback"),
                ko(KnowledgeObjectType.PROTOTYPE_TASK, "Prototype Juicy Movement", "Take a basic movement verb and add three feedback layers without changing the mechanic's rules.", "Game Feel", "Game Feel", "Juiciness", "prototype", "feel"),
                ko(KnowledgeObjectType.PROTOTYPE_TASK, "Prototype Risk Choice", "Create two options with the same goal but different uncertainty, exposure, reward, and recovery profile.", "Systems & Loops", "Characteristics of Games", "Risk vs Reward", "prototype", "risk"),
                ko(KnowledgeObjectType.PRINCIPLE, "Preserve the essential experience", "When cutting scope, preserve the intended player feeling before preserving secondary systems or presentation.", "Player Experience", "The Art of Game Design", "Essential Experience", "principle", "scope"),
                ko(KnowledgeObjectType.PATTERN, "Observe before explaining", "In playtests, separate what players did from why you think they did it; design changes should respond to evidence.", "Practice & Exercises", "Game Design Workshop", "Playtest", "pattern", "research"),
                ko(KnowledgeObjectType.METHOD, "Core Loop Sketch", "Write the action, system response, reward, new state, and next motivation as a five-step loop before adding features.", "Systems & Loops", "Advanced Game Design", "Core Loop", "method", "loop"));
    }

    private static OntologySeedBookRequest book(
            String title,
            String subtitle,
            String summary,
            String publisher,
            Integer year,
            String category,
            List<String> authors,
            List<String> tags) {
        return new OntologySeedBookRequest(title, subtitle, summary, publisher, year, category, Visibility.PUBLIC, authors, tags);
    }

    private static OntologySeedConceptRequest concept(
            String title,
            String description,
            String layer,
            String sourceBookTitle,
            String... tags) {
        return new OntologySeedConceptRequest(title, description, layer, List.of(tags), sourceBookTitle, SourceConfidence.MEDIUM);
    }

    private static OntologySeedKnowledgeObjectRequest ko(
            KnowledgeObjectType type,
            String title,
            String description,
            String layer,
            String sourceBookTitle,
            String conceptTitle,
            String... tags) {
        return new OntologySeedKnowledgeObjectRequest(
                type,
                title,
                description,
                layer,
                List.of(tags),
                sourceBookTitle,
                conceptTitle,
                null,
                null,
                SourceConfidence.MEDIUM);
    }
}
