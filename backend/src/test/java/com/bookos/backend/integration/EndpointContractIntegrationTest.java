package com.bookos.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@ActiveProfiles("test")
class EndpointContractIntegrationTest {

    private static final Set<Endpoint> DOCUMENTED_APPLICATION_ENDPOINTS = Set.of(
            endpoint(RequestMethod.GET, "/api/health"),
            endpoint(RequestMethod.POST, "/api/auth/register"),
            endpoint(RequestMethod.POST, "/api/auth/login"),
            endpoint(RequestMethod.GET, "/api/auth/me"),
            endpoint(RequestMethod.GET, "/api/users/me/profile"),
            endpoint(RequestMethod.PUT, "/api/users/me/onboarding"),
            endpoint(RequestMethod.GET, "/api/users"),
            endpoint(RequestMethod.GET, "/api/books"),
            endpoint(RequestMethod.POST, "/api/books"),
            endpoint(RequestMethod.GET, "/api/books/{id}"),
            endpoint(RequestMethod.PUT, "/api/books/{id}"),
            endpoint(RequestMethod.DELETE, "/api/books/{id}"),
            endpoint(RequestMethod.POST, "/api/books/{id}/add-to-library"),
            endpoint(RequestMethod.GET, "/api/user-books"),
            endpoint(RequestMethod.PUT, "/api/user-books/{id}/status"),
            endpoint(RequestMethod.PUT, "/api/user-books/{id}/progress"),
            endpoint(RequestMethod.PUT, "/api/user-books/{id}/rating"),
            endpoint(RequestMethod.GET, "/api/user-books/currently-reading"),
            endpoint(RequestMethod.GET, "/api/user-books/five-star"),
            endpoint(RequestMethod.GET, "/api/user-books/anti-library"),
            endpoint(RequestMethod.GET, "/api/books/{bookId}/notes"),
            endpoint(RequestMethod.POST, "/api/books/{bookId}/notes"),
            endpoint(RequestMethod.GET, "/api/notes/{id}"),
            endpoint(RequestMethod.PUT, "/api/notes/{id}"),
            endpoint(RequestMethod.DELETE, "/api/notes/{id}"),
            endpoint(RequestMethod.POST, "/api/notes/{id}/blocks"),
            endpoint(RequestMethod.PUT, "/api/note-blocks/{id}"),
            endpoint(RequestMethod.DELETE, "/api/note-blocks/{id}"),
            endpoint(RequestMethod.POST, "/api/parser/preview"),
            endpoint(RequestMethod.POST, "/api/captures"),
            endpoint(RequestMethod.GET, "/api/captures/inbox"),
            endpoint(RequestMethod.GET, "/api/captures"),
            endpoint(RequestMethod.GET, "/api/captures/{id}"),
            endpoint(RequestMethod.PUT, "/api/captures/{id}"),
            endpoint(RequestMethod.POST, "/api/captures/{id}/convert"),
            endpoint(RequestMethod.POST, "/api/captures/{id}/review/concepts"),
            endpoint(RequestMethod.PUT, "/api/captures/{id}/archive"),
            endpoint(RequestMethod.GET, "/api/quotes"),
            endpoint(RequestMethod.POST, "/api/quotes"),
            endpoint(RequestMethod.GET, "/api/quotes/{id}"),
            endpoint(RequestMethod.PUT, "/api/quotes/{id}"),
            endpoint(RequestMethod.DELETE, "/api/quotes/{id}"),
            endpoint(RequestMethod.POST, "/api/captures/{id}/convert/quote"),
            endpoint(RequestMethod.GET, "/api/action-items"),
            endpoint(RequestMethod.POST, "/api/action-items"),
            endpoint(RequestMethod.GET, "/api/action-items/{id}"),
            endpoint(RequestMethod.PUT, "/api/action-items/{id}"),
            endpoint(RequestMethod.PUT, "/api/action-items/{id}/complete"),
            endpoint(RequestMethod.PUT, "/api/action-items/{id}/reopen"),
            endpoint(RequestMethod.DELETE, "/api/action-items/{id}"),
            endpoint(RequestMethod.POST, "/api/captures/{id}/convert/action-item"),
            endpoint(RequestMethod.GET, "/api/source-references/{id}"),
            endpoint(RequestMethod.GET, "/api/source-references"),
            endpoint(RequestMethod.GET, "/api/books/{bookId}/source-references"),
            endpoint(RequestMethod.GET, "/api/notes/{noteId}/source-references"),
            endpoint(RequestMethod.GET, "/api/captures/{captureId}/source-references"),
            endpoint(RequestMethod.GET, "/api/entity-links"),
            endpoint(RequestMethod.POST, "/api/entity-links"),
            endpoint(RequestMethod.PUT, "/api/entity-links/{id}"),
            endpoint(RequestMethod.DELETE, "/api/entity-links/{id}"),
            endpoint(RequestMethod.GET, "/api/backlinks"),
            endpoint(RequestMethod.GET, "/api/concepts"),
            endpoint(RequestMethod.POST, "/api/concepts"),
            endpoint(RequestMethod.GET, "/api/concepts/{id}"),
            endpoint(RequestMethod.PUT, "/api/concepts/{id}"),
            endpoint(RequestMethod.DELETE, "/api/concepts/{id}"),
            endpoint(RequestMethod.GET, "/api/books/{bookId}/concepts"),
            endpoint(RequestMethod.GET, "/api/knowledge-objects"),
            endpoint(RequestMethod.POST, "/api/knowledge-objects"),
            endpoint(RequestMethod.GET, "/api/knowledge-objects/{id}"),
            endpoint(RequestMethod.PUT, "/api/knowledge-objects/{id}"),
            endpoint(RequestMethod.DELETE, "/api/knowledge-objects/{id}"),
            endpoint(RequestMethod.GET, "/api/admin/ontology/default"),
            endpoint(RequestMethod.POST, "/api/admin/ontology/import/default"),
            endpoint(RequestMethod.POST, "/api/admin/ontology/import"),
            endpoint(RequestMethod.GET, "/api/daily/today"),
            endpoint(RequestMethod.POST, "/api/daily/regenerate"),
            endpoint(RequestMethod.POST, "/api/daily/skip"),
            endpoint(RequestMethod.POST, "/api/daily/reflections"),
            endpoint(RequestMethod.GET, "/api/daily/history"),
            endpoint(RequestMethod.POST, "/api/daily/create-prototype-task"),
            endpoint(RequestMethod.GET, "/api/reading-sessions"),
            endpoint(RequestMethod.POST, "/api/reading-sessions/start"),
            endpoint(RequestMethod.PUT, "/api/reading-sessions/{id}/finish"),
            endpoint(RequestMethod.GET, "/api/books/{bookId}/reading-sessions"),
            endpoint(RequestMethod.GET, "/api/review/sessions"),
            endpoint(RequestMethod.POST, "/api/review/sessions"),
            endpoint(RequestMethod.GET, "/api/review/sessions/{id}"),
            endpoint(RequestMethod.POST, "/api/review/sessions/{id}/items"),
            endpoint(RequestMethod.PUT, "/api/review/items/{id}"),
            endpoint(RequestMethod.POST, "/api/review/generate-from-book"),
            endpoint(RequestMethod.POST, "/api/review/generate-from-concept"),
            endpoint(RequestMethod.POST, "/api/review/generate-from-project"),
            endpoint(RequestMethod.GET, "/api/mastery"),
            endpoint(RequestMethod.GET, "/api/mastery/target"),
            endpoint(RequestMethod.PUT, "/api/mastery/target"),
            endpoint(RequestMethod.GET, "/api/analytics/reading"),
            endpoint(RequestMethod.GET, "/api/analytics/knowledge"),
            endpoint(RequestMethod.GET, "/api/analytics/books/{bookId}"),
            endpoint(RequestMethod.GET, "/api/demo/status"),
            endpoint(RequestMethod.POST, "/api/demo/start"),
            endpoint(RequestMethod.POST, "/api/demo/reset"),
            endpoint(RequestMethod.DELETE, "/api/demo"),
            endpoint(RequestMethod.GET, "/api/export/json"),
            endpoint(RequestMethod.GET, "/api/export/book/{bookId}/json"),
            endpoint(RequestMethod.GET, "/api/export/book/{bookId}/markdown"),
            endpoint(RequestMethod.GET, "/api/export/quotes/csv"),
            endpoint(RequestMethod.GET, "/api/export/action-items/csv"),
            endpoint(RequestMethod.GET, "/api/export/concepts/csv"),
            endpoint(RequestMethod.POST, "/api/import/preview"),
            endpoint(RequestMethod.POST, "/api/import/commit"),
            endpoint(RequestMethod.GET, "/api/forum/categories"),
            endpoint(RequestMethod.POST, "/api/forum/categories"),
            endpoint(RequestMethod.GET, "/api/forum/templates"),
            endpoint(RequestMethod.GET, "/api/forum/threads"),
            endpoint(RequestMethod.POST, "/api/forum/threads"),
            endpoint(RequestMethod.GET, "/api/forum/threads/{id}"),
            endpoint(RequestMethod.PUT, "/api/forum/threads/{id}"),
            endpoint(RequestMethod.DELETE, "/api/forum/threads/{id}"),
            endpoint(RequestMethod.PUT, "/api/forum/threads/{id}/moderation"),
            endpoint(RequestMethod.GET, "/api/forum/threads/{id}/comments"),
            endpoint(RequestMethod.POST, "/api/forum/threads/{id}/comments"),
            endpoint(RequestMethod.PUT, "/api/forum/comments/{id}"),
            endpoint(RequestMethod.DELETE, "/api/forum/comments/{id}"),
            endpoint(RequestMethod.POST, "/api/forum/threads/{id}/bookmark"),
            endpoint(RequestMethod.DELETE, "/api/forum/threads/{id}/bookmark"),
            endpoint(RequestMethod.POST, "/api/forum/threads/{id}/like"),
            endpoint(RequestMethod.DELETE, "/api/forum/threads/{id}/like"),
            endpoint(RequestMethod.POST, "/api/forum/threads/{id}/report"),
            endpoint(RequestMethod.GET, "/api/forum/reports"),
            endpoint(RequestMethod.PUT, "/api/forum/reports/{id}/resolve"),
            endpoint(RequestMethod.GET, "/api/search"),
            endpoint(RequestMethod.GET, "/api/graph"),
            endpoint(RequestMethod.GET, "/api/graph/book/{bookId}"),
            endpoint(RequestMethod.GET, "/api/graph/concept/{conceptId}"),
            endpoint(RequestMethod.GET, "/api/graph/project/{projectId}"),
            endpoint(RequestMethod.GET, "/api/ai/status"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/note-summary"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/extract-actions"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/extract-concepts"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/design-lenses"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/project-applications"),
            endpoint(RequestMethod.POST, "/api/ai/suggestions/forum-thread"),
            endpoint(RequestMethod.GET, "/api/ai/suggestions"),
            endpoint(RequestMethod.PUT, "/api/ai/suggestions/{id}/accept"),
            endpoint(RequestMethod.PUT, "/api/ai/suggestions/{id}/reject"),
            endpoint(RequestMethod.PUT, "/api/ai/suggestions/{id}/edit"),
            endpoint(RequestMethod.GET, "/api/projects"),
            endpoint(RequestMethod.POST, "/api/projects"),
            endpoint(RequestMethod.GET, "/api/projects/{id}"),
            endpoint(RequestMethod.PUT, "/api/projects/{id}"),
            endpoint(RequestMethod.DELETE, "/api/projects/{id}"),
            endpoint(RequestMethod.PUT, "/api/projects/{id}/archive"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/problems"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/problems"),
            endpoint(RequestMethod.PUT, "/api/project-problems/{id}"),
            endpoint(RequestMethod.DELETE, "/api/project-problems/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/applications"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/applications"),
            endpoint(RequestMethod.PUT, "/api/project-applications/{id}"),
            endpoint(RequestMethod.DELETE, "/api/project-applications/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/decisions"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/decisions"),
            endpoint(RequestMethod.PUT, "/api/design-decisions/{id}"),
            endpoint(RequestMethod.DELETE, "/api/design-decisions/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/playtest-plans"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/playtest-plans"),
            endpoint(RequestMethod.PUT, "/api/playtest-plans/{id}"),
            endpoint(RequestMethod.DELETE, "/api/playtest-plans/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/playtest-findings"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/playtest-findings"),
            endpoint(RequestMethod.PUT, "/api/playtest-findings/{id}"),
            endpoint(RequestMethod.DELETE, "/api/playtest-findings/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/knowledge-links"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/knowledge-links"),
            endpoint(RequestMethod.DELETE, "/api/project-knowledge-links/{id}"),
            endpoint(RequestMethod.GET, "/api/projects/{projectId}/lens-reviews"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/lens-reviews"),
            endpoint(RequestMethod.PUT, "/api/project-lens-reviews/{id}"),
            endpoint(RequestMethod.DELETE, "/api/project-lens-reviews/{id}"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/apply/source-reference"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/apply/quote"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/apply/concept"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/apply/knowledge-object"),
            endpoint(RequestMethod.POST, "/api/projects/{projectId}/create-prototype-task-from-daily")
    );

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void documentedApplicationEndpointsHaveControllerMappings() {
        assertThat(actualApplicationEndpoints()).containsAll(DOCUMENTED_APPLICATION_ENDPOINTS);
    }

    @Test
    void controllerMappingsAreDocumented() {
        assertThat(DOCUMENTED_APPLICATION_ENDPOINTS).containsAll(actualApplicationEndpoints());
    }

    private Set<Endpoint> actualApplicationEndpoints() {
        return handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(this::toEndpoints)
                .filter(endpoint -> endpoint.path().startsWith("/api/"))
                .collect(Collectors.toSet());
    }

    private Stream<Endpoint> toEndpoints(RequestMappingInfo info) {
        var methods = info.getMethodsCondition().getMethods();
        var patternsCondition = info.getPathPatternsCondition();
        var patterns = patternsCondition == null
                ? info.getPatternsCondition().getPatterns()
                : patternsCondition.getPatternValues();

        return patterns.stream()
                .flatMap(pattern -> methods.stream().map(method -> endpoint(method, pattern)));
    }

    private static Endpoint endpoint(RequestMethod method, String path) {
        return new Endpoint(method, path);
    }

    private record Endpoint(RequestMethod method, String path) {
    }
}
