<template>
  <div class="page-shell">
    <RouterLink to="/use-cases" custom v-slot="{ navigate }">
      <AppButton class="use-case-detail-view__back" variant="text" @click="navigate">Back to use cases</AppButton>
    </RouterLink>

    <UseCaseDetail v-if="useCase" :use-case="useCase" />

    <AppErrorState
      v-else
      title="Use case not found"
      description="This scenario does not exist. Open the use-case library to choose a supported workflow."
    >
      <template #actions>
        <RouterLink to="/use-cases" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Open use-case library</AppButton>
        </RouterLink>
      </template>
    </AppErrorState>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppButton from '../components/ui/AppButton.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import UseCaseDetail from '../components/use-case/UseCaseDetail.vue'
import { findUseCase } from '../data/useCases'

const route = useRoute()

const useCase = computed(() => findUseCase(String(route.params.slug ?? '')))
</script>

<style scoped>
.use-case-detail-view__back {
  justify-self: start;
}
</style>
