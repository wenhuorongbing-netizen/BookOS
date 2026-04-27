<template>
  <div class="public-layout">
    <section class="public-layout__hero">
      <div class="eyebrow">BookOS</div>
      <h1>Game Design Knowledge Operating System</h1>
      <p>
        Build a personal game design library, track reading progress, and turn books into usable design references.
      </p>
      <div class="public-layout__chips">
        <span class="tag-chip">Books</span>
        <span class="tag-chip">Library</span>
        <span class="tag-chip">Reading Workflow</span>
      </div>
    </section>
    <main class="public-layout__panel surface-card">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 2rem;
  padding: 2rem;
}

.public-layout__hero,
.public-layout__panel {
  padding: 2rem;
}

.public-layout__hero {
  display: grid;
  align-content: center;
  gap: 1rem;
}

.public-layout__hero h1 {
  margin: 0;
  font-size: clamp(2.4rem, 6vw, 4.8rem);
  line-height: 0.94;
  letter-spacing: -0.05em;
}

.public-layout__hero p {
  max-width: 36rem;
  color: var(--text-muted);
  font-size: 1.05rem;
}

.public-layout__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.public-layout__panel {
  align-self: center;
  background: var(--surface-strong);
}

@media (max-width: 980px) {
  .public-layout {
    grid-template-columns: 1fr;
    padding: 1rem;
  }
}
</style>
