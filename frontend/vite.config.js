import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

import viewportHeightCorrection from 'postcss-viewport-height-correction';

let server = "localhost"
//server = "omv"
const defProxy = {
  target: `http://${server}:9011`, // 你的真实后端地址
  changeOrigin: true,              // 允许跨域
  // 如果后端接口没有 /api 前缀，可以进行路径重写
  // rewrite: (path) => path.replace(/^\/api/, '')
  // 调试输出
  configure: (proxy, options) => {
    proxy.on('proxyReq', (proxyReq, req, res) => {
      console.log('正在转发请求：', req.method, req.url);
    });
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 9010,
    host: "0.0.0.0",
    proxy: {
      /*
      // 匹配所有以 /api 开头的请求
      '/api': {
        target: 'http://localhost:9011', // 你的真实后端地址
        changeOrigin: true,              // 允许跨域
        // 如果后端接口没有 /api 前缀，可以进行路径重写
        // rewrite: (path) => path.replace(/^\/api/, '')
        // 调试输出
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('正在转发请求：', req.method, req.url);
          });
        }
      },*/
      '/api': defProxy,
      '/secure': defProxy
    }
  },
  css: {
    postcss: {
      plugins: [
        viewportHeightCorrection({ variable: 'vh' }),
      ]
    }
  }
})
