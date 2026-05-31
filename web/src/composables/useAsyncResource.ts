import { ref, shallowRef, type Ref, type ShallowRef } from 'vue'
import { ApiError } from '@/api/client'

type AsyncStatus = 'idle' | 'loading' | 'success' | 'error'

export type UseAsyncResourceReturn<T> = {
  data: ShallowRef<T | null>
  error: Ref<ApiError | Error | null>
  status: Ref<AsyncStatus>
  isLoading: Ref<boolean>
  isError: Ref<boolean>
  load: () => Promise<void>
  reload: () => Promise<void>
}

/**
 * Generic wrapper for fetching a single async resource. Holds `data`, `error`,
 * `status`. Calling `load()` re-fetches. Errors are caught and stored, not thrown.
 */
export function useAsyncResource<T>(
  fetcher: () => Promise<T>,
  options?: { immediate?: boolean },
): UseAsyncResourceReturn<T> {
  const data = shallowRef<T | null>(null)
  const error = ref<ApiError | Error | null>(null)
  const status = ref<AsyncStatus>('idle')
  const isLoading = ref(false)
  const isError = ref(false)

  let token = 0
  const load = async (): Promise<void> => {
    const current = ++token
    isLoading.value = true
    isError.value = false
    status.value = 'loading'
    error.value = null
    try {
      const result = await fetcher()
      if (current !== token) return
      data.value = result
      status.value = 'success'
    } catch (e) {
      if (current !== token) return
      error.value = e as Error
      isError.value = true
      status.value = 'error'
    } finally {
      if (current === token) isLoading.value = false
    }
  }

  if (options?.immediate !== false) {
    void load()
  }

  return { data, error, status, isLoading, isError, load, reload: load }
}
