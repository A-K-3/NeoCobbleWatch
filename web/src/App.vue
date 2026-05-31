<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
</script>

<template>
  <div class="flex min-h-full flex-col">
    <AppHeader />
    <main class="flex-1">
      <div class="mx-auto max-w-6xl px-5 py-10 sm:px-8 sm:py-14">
        <RouterView v-slot="{ Component, route }">
          <Transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </Transition>
        </RouterView>
      </div>
    </main>
    <footer class="border-t border-edge-soft">
      <div class="mx-auto flex max-w-6xl flex-col items-center gap-2 px-5 py-6 text-center sm:flex-row sm:justify-between sm:px-8 sm:text-left">
        <span class="font-sans text-sm text-muted">
          Cobbleverse · Panel de la liga
        </span>
        <span class="font-mono text-xs text-faint">v1 · solo lectura</span>
      </div>
    </footer>
  </div>
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease, filter 0.28s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
  filter: blur(4px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  filter: blur(4px);
}
</style>
