<template>
  <div class="Home">
    <switch-menu/>
    <div :class="clazz">
      <router-view />
    </div>
  </div>
</template>
<script>
import {computed} from "vue";
import SwitchMenu from "@/components/home/SwitchMenu.vue";

export default {
  name: "Home",
  //引入模块
  components: {
    SwitchMenu
  },
  //父级传入数据
  props: {
    // inData: Object,
  },
  data() {
    return {
      //双向绑定处理
      // tabPosition: computed({
      //   get: () => this.inData,
      //   set: val => {
      //     this.$emit("update:inData", val);
      //   }
      // }),

      clazz: ["box"]
    }
  },
  //方法
  methods: {
    upPageSize: function (env) {
      //console.log("upPageSize", env)
      /*
      let width = window.innerWidth;
      let w1= 720;
      let w2= 450;
      if(width>w1){
        this.clazz = ["box"]
      }else if(width<=w1 && width>w2){
        this.clazz = ["box", "phone"]
      }else{
        this.clazz = ["box", "smallPhone"]
      }
      */
      this.$store.commit("reSizeWindow")
      // console.log("this.$store", this.$store, this.$store.state)
      switch (this.$store.state.showMode) {
        case "pc":
          this.clazz = ["box"]
          break;
        case "phone":
          this.clazz = ["box", "phone"]
          break;
        case "smailPhone":
          this.clazz = ["box", "smallPhone"]
          break;
      }
    }
  },
  //启动事件
  mounted() {
    document.title = "JMComic辅助工具"
    this.upPageSize();
    window.addEventListener('resize', this.upPageSize);
  },
  //销毁
  beforeUnmount() {
    window.removeEventListener('resize', this.upPageSize);
  }
}
</script>
<style scoped>
@import "@/style/Home.css";
</style>