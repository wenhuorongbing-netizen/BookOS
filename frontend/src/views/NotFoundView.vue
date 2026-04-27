<template>
  <div class="page-shell">
    <div class="eyebrow">404</div>
    <section class="surface-card not-found-card">
      <div class="not-found-card__code">Page Not Found</div>
      <h1>The route you requested does not exist in BookOS.</h1>
      <p>
        The link may be outdated, or the page may have moved while Milestone 1 was being reorganized. Use one of the
        entry points below to get back into the app.
      </p>
      <div class="not-found-card__actions">
        <RouterLink to="/dashboard"><el-button type="primary">Go to dashboard</el-button></RouterLink>
        <RouterLink to="/my-library"><el-button>Open my library</el-button></RouterLink>
        <RouterLink to="/login"><el-button text>Login page</el-button></RouterLink>
      </div>
    </section>
  </div>
</template>

<style scoped>
.not-found-card {
  padding: 2rem;
  display: grid;
  gap: 1rem;
  background: var(--surface-strong);
}

.not-found-card__code {
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #7f6542;
}

.not-found-card h1 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 0.96;
}

.not-found-card p {
  margin: 0;
  color: var(--text-muted);
  max-width: 42rem;
}

.not-found-card__actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}
</style>
