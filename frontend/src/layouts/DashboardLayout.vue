<template>
  <div class="dashboard-layout surface-card">
    <div class="dashboard-layout__hero">
      <div>
        <div class="eyebrow">Milestone 1</div>
        <h1>Personal book library foundation</h1>
        <p>Authentication, books, and library operations are live. The rest of BookOS can grow on top of this base.</p>
      </div>
    </div>
    <RouterView />
  </div>
</template>

<style scoped>
.dashboard-layout {
  padding: 1.5rem;
  display: grid;
  gap: 1.5rem;
}

.dashboard-layout__hero h1 {
  margin: 0.4rem 0;
  font-size: clamp(1.8rem, 4vw, 3rem);
  letter-spacing: -0.04em;
}

.dashboard-layout__hero p {
  margin: 0;
  color: var(--text-muted);
  max-width: 42rem;
}
</style>
