<template>
  <div class="T-Img">
    <el-image v-if="imgBlob" class="eImg" :src="imgBlob" fit="cover" :lazy="isLazy"/>
<!--    <el-image v-if="imgBlob" class="eImg" :src="getImgBlob" fit="cover" :lazy="isLazy"/>-->
    <div v-else class="error">
      {{ errorMessage }}
      <el-button type="warning" @click="getImage(authSrc)">重试</el-button>
    </div>
  </div>
</template>
<script>
import {computed} from "vue";

export default {
  name: "T-Img",
  //引入模块
  components: {},
  //父级传入数据
  props: {
    authSrc: Object,
    alt: String,
    isLazy: Boolean,
  },
  data() {
    return {
      //双向绑定处理
      /*
      src: computed({
        get: () => {
          this.getImage(this.authSrc);
          return this.authSrc;
        },
        set: val => {
          this.$emit("update:authSrc", val);
        }
      }),
      */

      imgBlob: null,
      errorMessage: null,
      urlBuff: null,

      getImgBlob: computed({
        get: async () =>  {
          let that = this;
          if(this.authSrc !== this.urlBuff){
            that.imgBlob = undefined;
            that.urlBuff = that.authSrc;
            this.ajaxBlob("GET", this.authSrc, function (res) {
              //console.log("ajaxBlob res:", url, res)
              if(res.status === 200){
                that.imgBlob = URL.createObjectURL(res.response);
              }else{
                that.errorMessage = "加载失败"
                console.log("errorMessage", that.errorMessage)
              }
            }, true);
          }
          console.log("ajaxBlob", this.ajaxBlob);
          return this.imgBlob;
        },
        set: val => {
          // this.$emit("update:authSrc", val);
        }
      }),
    }
  },
  //方法
  methods: {
    getImage: function (url) {
      let that = this;
      this.ajaxBlob("GET", url, function (res) {
        //console.log("ajaxBlob res:", url, res)
        if(res.status === 200){
          that.imgBlob = URL.createObjectURL(res.response);
        }else{
          that.errorMessage = "加载失败"
          console.log("errorMessage", that.errorMessage)
        }
      })
      /*that.imgBlob = url;*/
    },
    ajaxBlob: function (method, url, func, async=true) {
      let xhr = new XMLHttpRequest();
      xhr.open(method, url, async);
      xhr.responseType = 'blob'; // 设置 responseType 为 blob
      let token = this.getToken();
      if(token){
        xhr.setRequestHeader("Authorization", "Bearer "+token);
      }
      // xhr.onload = function() {
      //   if (xhr.status === 200) {
      //     // let blob = xhr.response;
      //     // let img = document.createElement('img');
      //     // img.src = URL.createObjectURL(blob);
      //     // document.body.appendChild(img);
      //     func(xhr);
      //   }
      // };
      xhr.onreadystatechange = function(){
        if(this.readyState === 4){
          func(xhr);
        }
      }
      xhr.send();
    },
    getToken: function () {
      let token = this.$store.state["user"].token;
      if(!token){
        token = localStorage.getItem("token")
      }
      if(!token){
        token = ""
      }
      return token;
    }
  },
  //启动事件
  mounted() {
    // window.getImage = this.getImage
    this.getImage(this.authSrc);
  },
  //销毁
  beforeUnmount() {
  }
}
</script>
<style scoped>
/*@import "@/style/T-Img.css";*/
.eImg{
  width: 100%;
  height: 100%;
}
</style>