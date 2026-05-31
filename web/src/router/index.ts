import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: 'Overview' },
  },
  {
    path: '/players',
    name: 'players',
    component: () => import('@/views/PlayersListView.vue'),
    meta: { title: 'Players' },
  },
  {
    path: '/players/:uuid',
    name: 'player',
    component: () => import('@/views/PlayerProfileView.vue'),
    props: true,
    meta: { title: 'Profile' },
  },
  {
    path: '/tops/:metric?',
    name: 'tops',
    component: () => import('@/views/TopsView.vue'),
    props: true,
    meta: { title: 'Leaderboards' },
  },
  {
    path: '/:catchAll(.*)*',
    redirect: '/',
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} // COBBLEVERSE` : 'OPS // COBBLEVERSE'
})
