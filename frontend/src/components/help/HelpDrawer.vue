<template>
  <el-drawer v-model="drawerOpen" size="420px" append-to-body :title="topic?.term ?? 'BookOS help'">
    <article v-if="topic" class="help-drawer" aria-live="polite">
      <div>
        <div class="eyebrow">BookOS term</div>
        <h2>{{ topic.term }}</h2>
        <p>{{ topic.shortDefinition }}</p>
      </div>

      <AppCard class="help-drawer__card" variant="muted" as="section">
        <h3>Why it matters</h3>
        <p>{{ topic.whyItMatters }}</p>
      </AppCard>

      <AppCard class="help-drawer__card" variant="muted" as="section">
        <h3>First action</h3>
        <p>{{ topic.firstAction }}</p>
      </AppCard>

      <AppCard v-if="topic.example" class="help-drawer__card" variant="muted" as="section">
        <h3>Example</h3>
        <code>{{ topic.example }}</code>
      </AppCard>

      <div class="help-drawer__actions">
        <RouterLink v-if="topic.route" :to="topic.route" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigateAndClose(navigate)">Open related workspace</AppButton>
        </RouterLink>
        <RouterLink :to="{ name: 'help-topic', params: { topic: topic.slug } }" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigateAndClose(navigate)">Open full help</AppButton>
        </RouterLink>
        <RouterLink v-if="topic.useCaseSlug" :to="{ name: 'use-case-detail', params: { slug: topic.useCaseSlug } }" custom v-slot="{ navigate }">
          <AppButton variant="ghost" @click="navigateAndClose(navigate)">See use case</AppButton>
        </RouterLink>
      </div>

      <div v-if="relatedTopics.length" class="help-drawer__related" aria-label="Related help topics">
        <strong>Related</strong>
        <button v-for="related in relatedTopics" :key="related.slug" type="button" @click="openRelated(related.slug)">
          {{ related.term }}
        </button>
      </div>
    </article>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getHelpTopic, type HelpTopic } from '../../data/helpTopics'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'

const route = useRoute()
const router = useRouter()

const topic = computed(() => getHelpTopic(Array.isArray(route.query.help) ? route.query.help[0] : route.query.help))
const drawerOpen = computed({
  get: () => Boolean(topic.value),
  set: (value) => {
    if (!value) closeHelp()
  },
})
const relatedTopics = computed(() => topic.value?.relatedSlugs?.map((slug) => getHelpTopic(slug)).filter((item): item is HelpTopic => Boolean(item)) ?? [])

function closeHelp() {
  const nextQuery = { ...route.query }
  delete nextQuery.help
  void router.replace({ query: nextQuery })
}

function openRelated(slug: string) {
  void router.replace({
    query: {
      ...route.query,
      help: slug,
    },
  })
}

function navigateAndClose(navigate: () => void) {
  closeHelp()
  navigate()
}
</script>

<style scoped>
.help-drawer,
.help-drawer__card {
  display: grid;
  gap: var(--space-4);
}

.help-drawer h2,
.help-drawer h3,
.help-drawer p {
  margin: 0;
}

.help-drawer h2 {
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.45rem;
}

.help-drawer p {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.help-drawer__card {
  padding: var(--space-4);
}

.help-drawer code {
  padding: var(--space-3);
  display: block;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  white-space: pre-wrap;
}

.help-drawer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.help-drawer__related {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.help-drawer__related strong {
  color: var(--bookos-text-primary);
}

.help-drawer__related button {
  min-height: 2rem;
  padding: 0 var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface-muted);
  color: var(--bookos-primary);
  cursor: pointer;
  font-weight: 800;
}

.help-drawer__related button:focus-visible {
  outline: 2px solid var(--bookos-focus);
  outline-offset: 2px;
}
</style>
