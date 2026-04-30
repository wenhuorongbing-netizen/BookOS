<template>
  <AppTooltip :content="topic.shortDefinition" :placement="placement">
    <button class="help-tooltip" type="button" :aria-label="`Help: ${topic.term}`" @click="openHelp">
      <span aria-hidden="true">?</span>
      <span class="sr-only">Help: {{ topic.term }}</span>
    </button>
  </AppTooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHelpTopic, type HelpTopicSlug } from '../../data/helpTopics'
import AppTooltip from '../ui/AppTooltip.vue'

const props = withDefaults(
  defineProps<{
    topic: HelpTopicSlug
    placement?: 'top' | 'top-start' | 'top-end' | 'bottom' | 'bottom-start' | 'bottom-end' | 'left' | 'right'
  }>(),
  {
    placement: 'top',
  },
)

const route = useRoute()
const router = useRouter()
const topic = computed(() => getHelpTopic(props.topic) ?? getHelpTopic('source-reference')!)

function openHelp() {
  void router.replace({
    query: {
      ...route.query,
      help: props.topic,
    },
  })
}
</script>

<style scoped>
.help-tooltip {
  width: var(--touch-target);
  min-width: var(--touch-target);
  height: var(--touch-target);
  display: inline-grid;
  place-items: center;
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-primary);
  cursor: pointer;
  font-weight: 900;
  line-height: 1;
  transition:
    background-color var(--motion-fast) var(--motion-ease),
    border-color var(--motion-fast) var(--motion-ease),
    box-shadow var(--motion-fast) var(--motion-ease);
}

.help-tooltip:hover {
  border-color: var(--bookos-primary);
  background: var(--bookos-primary-soft);
}

.help-tooltip:focus-visible {
  outline: 2px solid var(--bookos-focus);
  outline-offset: 3px;
  box-shadow: var(--focus-ring);
}
</style>
