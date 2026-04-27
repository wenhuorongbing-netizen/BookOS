<template>
  <figure class="book-cover">
    <img v-if="coverUrl" :src="coverUrl" :alt="`Cover of ${title}`" />
    <div v-else class="book-cover__fallback" role="img" :aria-label="`Cover placeholder for ${title}`">
      <span>{{ title }}</span>
    </div>
  </figure>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  coverUrl: string | null | undefined
}>()
</script>

<style scoped>
.book-cover {
  margin: 0;
  width: 100%;
}

.book-cover img,
.book-cover__fallback {
  width: 100%;
  aspect-ratio: 2 / 3;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 44px rgba(54, 42, 24, 0.2);
}

.book-cover img {
  display: block;
  object-fit: cover;
  background: var(--bookos-surface-muted);
}

.book-cover__fallback {
  min-height: 280px;
  padding: var(--space-5);
  display: grid;
  place-items: center;
  background:
    linear-gradient(160deg, rgba(31, 95, 87, 0.86), rgba(197, 107, 44, 0.74)),
    var(--bookos-surface-muted);
  color: #fffdf8;
  text-align: center;
}

.book-cover__fallback span {
  font-family: var(--font-book-title);
  font-size: clamp(1.15rem, 2vw, 1.55rem);
  font-weight: 800;
  line-height: 1.08;
}
</style>
