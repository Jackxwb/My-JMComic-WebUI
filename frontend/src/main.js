import { createApp } from 'vue'
// import './style.css'
import App from './App.vue'

// vuex
import store from './config/store'
// router
import router from './config/router'
// axios
import API from '@/config/axios/axiosInstance'

import ElementPlus from 'element-plus';
import 'element-plus/theme-chalk/index.css';
import 'element-plus/dist/index.css';

// vue-virtual-scroller
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import VueVirtualScroller from 'vue-virtual-scroller'

import "@/utils/DateUtil.js"

//前端MOCK
import "@/config/mockjs"

//国际化
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 防抖，用来解决 element-plus el-tabs 改变大小触发 ResizeObserver loop limit exceeded 异常的问题
// from: https://github.com/element-plus/element-plus/issues/10630
// 另外解决 el-table 不能缩小，需要在 el-table 外面的容器加上 { flex: 1; width: 0 } 样式
// form: https://github.com/element-plus/element-plus/issues/6202#issuecomment-1047527166

const debounce = (fn, delay) => {
    let timer = null;
    return function () {
        let context = this;
        let args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    }
}
const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver {
    constructor(callback) {
        callback = debounce(callback, 16);
        super(callback);
    }
}
// 防抖

// createApp(App).mount('#app')
const app = createApp(App);


app.use(store)
    .use(router)
    .use(ElementPlus, {
        locale: zhCn,
    })
    .use(VueVirtualScroller)
app.mount('#app')

//element-plus/icons-vue 注册所有图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

//全局Axios
app.config.globalProperties.$API = API;