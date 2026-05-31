<script setup lang="ts">
import { ref, watch } from 'vue'
import { Search, X } from '@lucide/vue'

type Props = {
  modelValue: string
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), { placeholder: 'Buscar...' })
const emit = defineEmits<{ 'update:modelValue': [v: string] }>()

const local = ref(props.modelValue)
watch(() => props.modelValue, (v) => { local.value = v })

let timer: ReturnType<typeof setTimeout> | null = null
function onInput() {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => emit('update:modelValue', local.value), 150)
}

function clear() {
  local.value = ''
  if (timer) clearTimeout(timer)
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="relative inline-flex">
    <Search :size="14" class="pointer-events-none absolute left-2.5 top-1/2 -translate-y-1/2 text-faint" />
    <input
      v-model="local"
      type="search"
      :placeholder="placeholder"
      class="w-48 rounded-md border border-edge-soft bg-white/[0.04] py-1 pl-8 pr-7 font-sans text-xs text-ink placeholder:text-faint focus:border-edge focus:outline-none"
      @input="onInput"
    />
    <button
      v-if="local"
      type="button"
      class="absolute right-1.5 top-1/2 -translate-y-1/2 text-faint transition-colors hover:text-ink"
      @click="clear"
    >
      <X :size="12" />
    </button>
  </div>
</template>
