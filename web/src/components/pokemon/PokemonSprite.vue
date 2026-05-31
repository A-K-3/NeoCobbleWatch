<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { cn } from '@/utils/cn'
import { spriteUrl } from '@/utils/pokemon'
import { stripNamespace } from '@/utils/format'
import { useSpeciesStore } from '@/stores/species'
import { Sparkles } from '@lucide/vue'

type Props = {
  speciesId: string
  shiny?: boolean
  size?: 'sm' | 'md' | 'lg'
  aspects?: string[]
}

const props = withDefaults(defineProps<Props>(), { shiny: false, size: 'md', aspects: () => [] })

const speciesStore = useSpeciesStore()

const loading = ref(true)
const errored = ref(false)

watch(() => props.speciesId, () => { loading.value = true; errored.value = false })

const dex = computed(() => speciesStore.info(props.speciesId)?.national_dex)
const url = computed(() => spriteUrl(props.speciesId, { shiny: props.shiny, dex: dex.value, aspects: props.aspects }))
const fallback = computed(() => stripNamespace(props.speciesId).slice(0, 2).toUpperCase())

const sizeClass = computed(() => {
  switch (props.size) {
    case 'sm': return 'h-12 w-12'
    case 'md': return 'h-20 w-20'
    case 'lg': return 'h-28 w-28'
  }
})
</script>

<template>
  <div :class="cn('relative flex items-center justify-center', sizeClass)">
    <div
      v-if="loading && !errored"
      class="absolute inset-0 animate-pulse rounded-lg bg-white/[0.07]"
    />
    <img
      v-if="!errored"
      :src="url"
      :alt="speciesId"
      :class="cn(
        'h-full w-full object-contain transition-opacity duration-200',
        loading ? 'opacity-0' : 'opacity-100',
        shiny && 'drop-shadow-[0_0_8px_rgb(255_214_10_/_0.5)]',
      )"
      loading="lazy"
      @load="loading = false"
      @error="loading = false; errored = true"
    />
    <span v-if="errored" class="font-display text-2xl font-medium text-quiet">{{ fallback }}</span>
    <Sparkles v-if="shiny" :size="12" class="absolute right-0 top-0 text-yellow" aria-label="shiny" />
  </div>
</template>
