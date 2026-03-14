// createWebHashHistory
import { createRouter, createWebHashHistory  } from 'vue-router'
import store from '@/config/store'

const routes = [
  {
    path: '/',
    // redirect:'/index',
    // component: () => import('@/page/Home.vue'),
    // children: [
    //   {
    //     path: "home",
    //     component: import('@/page/Home.vue'),
    //     children: [
    //       // { path: "", component: () => import('@/components/home/SwitchMenu.vue'), },
    //       { path: "download", component: () => import('@/components/home/SwitchMenu.vue'), },
    //     ]
    //   },
    // ],
    component: () => import('@/page/Home.vue'),
    children: [
      { path: "home/download", component: () => import('@/page/home/download.vue'), },
      { path: "home/search", component: () => import('@/page/home/search.vue'), },
      { path: "home/config", component: () => import('@/page/home/config.vue'), },
      { path: "home/library", component: () => import('@/page/home/library.vue'), },
      { path: "home/special_tag", component: () => import('@/page/home/specialTag.vue'), },
    ],
    // 只有经过身份验证的用户才能创建帖子
    meta: { requiresAuth: true },
  },
  {
    path: '/secure/login',
    component: () => import('@/page/login.vue'),
    /*children: [
      { path: "home/download", component: () => import('@/page/home/download.vue'), },
      { path: "home/search", component: () => import('@/page/home/search.vue'), },
      { path: "home/config", component: () => import('@/page/home/config.vue'), },
      { path: "home/library", component: () => import('@/page/home/library.vue'), },
    ]*/

  },
  // {
  //   path: '/',
  //   name: 'indexPage',
  //   component: () => import('@/page/Home.vue')
  // },
  // {
  //   path: '/t',
  //   component: () => import('@/page/phoneTest.vue'),
  // },
]

const router = createRouter({
  history: createWebHashHistory(),
  // mode: 'hash',
  // history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from) => {
  //console.log("router.beforeEach", this, store)
  // 而不是去检查每条路由记录
  // to.matched.some(record => record.meta.requiresAuth)

  if(process.env.NODE_ENV === "development" && false) {
    // 调试环境
    return;
  }

  if (to.meta.requiresAuth ) {// && !auth.isLoggedIn()
    let isLogin = await store.dispatch("user/checkToken");
    console.log("isLogin", isLogin)
    if(!isLogin){
      store.commit("user/clearToken")
      return {
        path: '/secure/login',
        // 保存我们所在的位置，以便以后再来
        query: { redirect: to.fullPath },
      }
    }
    /*
    // 此路由需要授权，请检查是否已登录
    // 如果没有，则重定向到登录页面
    return {
      path: '/login',
      // 保存我们所在的位置，以便以后再来
      query: { redirect: to.fullPath },
    }
    */
  }
})

export default router
