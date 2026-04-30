<template>
  <div class="page-shell help-page">
    <AppSectionHeader
      eyebrow="BookOS Help"
      :title="selectedTopic ? selectedTopic.term : 'Learn BookOS terms'"
      :description="selectedTopic ? selectedTopic.shortDefinition : 'Short, practical explanations for source references, graph links, project applications, AI drafts, review, mastery, and import safety.'"
      :level="1"
    >
      <template #actions>
        <RouterLink v-if="selectedTopic" :to="{ name: 'help' }" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">All topics</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <template v-if="selectedTopic">
      <section class="help-detail-grid" aria-label="Help topic detail">
        <AppCard class="help-detail-card" as="section">
          <div class="eyebrow">Why it matters</div>
          <p>{{ selectedTopic.whyItMatters }}</p>
        </AppCard>
        <AppCard class="help-detail-card" as="section">
          <div class="eyebrow">First action</div>
          <p>{{ selectedTopic.firstAction }}</p>
        </AppCard>
        <AppCard v-if="selectedTopic.example" class="help-detail-card help-detail-card--wide" as="section">
          <div class="eyebrow">Example</div>
          <code>{{ selectedTopic.example }}</code>
        </AppCard>
      </section>

      <div class="help-actions">
        <RouterLink v-if="selectedTopic.route" :to="selectedTopic.route" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Open related workspace</AppButton>
        </RouterLink>
        <RouterLink v-if="selectedTopic.useCaseSlug" :to="{ name: 'use-case-detail', params: { slug: selectedTopic.useCaseSlug } }" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open hands-on use case</AppButton>
        </RouterLink>
      </div>

      <AppCard v-if="relatedTopics.length" class="help-related" as="section">
        <AppSectionHeader title="Related topics" eyebrow="Keep learning" compact />
        <div class="help-topic-grid">
          <RouterLink v-for="topic in relatedTopics" :key="topic.slug" :to="{ name: 'help-topic', params: { topic: topic.slug } }" class="help-topic-card">
            <strong>{{ topic.term }}</strong>
            <span>{{ topic.shortDefinition }}</span>
          </RouterLink>
        </div>
      </AppCard>
    </template>

    <template v-else-if="requestedTopic">
      <AppEmptyState
        title="Help topic not found"
        description="This help topic does not exist yet. Use the glossary below or search for a related workflow."
        eyebrow="Missing topic"
      >
        <template #actions>
          <RouterLink :to="{ name: 'help' }" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">Open all help topics</AppButton>
          </RouterLink>
        </template>
      </AppEmptyState>
    </template>

    <section v-if="!selectedTopic" class="help-topic-grid" aria-label="BookOS glossary topics">
      <RouterLink v-for="topic in helpTopics" :key="topic.slug" :to="{ name: 'help-topic', params: { topic: topic.slug } }" class="help-topic-card">
        <span class="eyebrow">Help topic</span>
        <strong>{{ topic.term }}</strong>
        <span>{{ topic.shortDefinition }}</span>
      </RouterLink>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getHelpTopic, helpTopics, type HelpTopic } from '../data/helpTopics'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'

const route = useRoute()
const requestedTopic = computed(() => String(route.params.topic ?? ''))
const selectedTopic = computed(() => getHelpTopic(requestedTopic.value))
const relatedTopics = computed(() => selectedTopic.value?.relatedSlugs?.map((slug) => getHelpTopic(slug)).filter((topic): topic is HelpTopic => Boolean(topic)) ?? [])
</script>

<style scoped>
.help-page,
.help-related {
  display: grid;
  gap: var(--space-5);
}

.help-detail-grid,
.help-topic-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.help-detail-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-3);
}

.help-detail-card--wide {
  grid-column: 1 / -1;
}

.help-detail-card p,
.help-topic-card span {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.help-detail-card code {
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  white-space: pre-wrap;
}

.help-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.help-related {
  padding: var(--space-5);
}

.help-topic-card {
  min-height: 160px;
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  box-shadow: var(--shadow-card);
  color: inherit;
  text-decoration: none;
  transition:
    border-color var(--motion-fast) var(--motion-ease),
    box-shadow var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease);
}

.help-topic-card:hover {
  border-color: var(--bookos-border-strong);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

.help-topic-card:focus-visible {
  outline: 2px solid var(--bookos-focus);
  outline-offset: 3px;
}

.help-topic-card strong {
  color: var(--bookos-text-primary);
  font-size: 1.1rem;
}

@media (max-width: 860px) {
  .help-detail-grid,
  .help-topic-grid {
    grid-template-columns: 1fr;
  }
}
</style>
