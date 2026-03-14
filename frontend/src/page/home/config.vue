<template>
  <div class="config">
    <el-button-group>
      <el-button plain @click="readJMConfig">读取</el-button>
      <el-button plain @click="saveJMConfig">保存</el-button>
      <el-button plain @click="updateJM">更新JMComic下载库</el-button>
      <el-drawer
          v-model="openJM"
          title="更新 Jmcomic Python库"
          direction="rtl"
          :size="dSize"
      >
        <div class="line gap-row">
          <el-tag type="primary" v-if="updateJMTask?.running">更新中</el-tag>
          <el-tag type="primary" v-if="!updateJMTask?.running">更新结束</el-tag>
          <el-tag type="success" effect="dark" >{{ updateJMTask.pid }}</el-tag>
        </div>
        <pre class="out">{{ updateJMTask?.log_full }}</pre>
      </el-drawer>

      <el-button plain @click="openTestWindow_()">测试</el-button>
      <el-drawer v-model="openTestWindow" title="运行测试指令" :size="dSize" >
        <el-alert type="warning" :closable="false">
          <p>此功能仅面对开发者</p>
          <p>此功能可运行主机上任意命令，小白请不要使用，以免造成数据损失！</p>
        </el-alert>
        <el-divider />
        <el-input
            v-model="testCommand"
            :rows="10"
            type="textarea"
            :placeholder="'运行命令\r\n一行一个参数\r\n如：jmcomic 1153488 --option=./option.yml\r\n转换后请输入：\r\njmcomic\r\n1153488\r\n--option=./option.yml'"
            :disabled="testIsRun"
        />
        <el-divider />
        <div class="gap-row">
          <el-button type="primary" :disabled="testIsRun" @click="runTest">运行</el-button>
          <el-select filterable allow-create clearable v-model="testResultCharset" placeholder="指定控制台编码" >
            <el-option value="UTF-8"/>
            <el-option value="GBK"/>
          </el-select>
        </div>
        <el-divider />
        <pre>{{ testResult }}</pre>
        <el-divider />
      </el-drawer>

    </el-button-group>
    <el-text class="flexLeft" type="warning">不支持插件!目前只支持更新！</el-text>
    <el-form class="configForm" :model="configEntity" label-width="auto">
      <el-divider content-position="left"><div class="gap-row">
          <el-text>客户端设置</el-text>
          <el-switch v-model="configEnable.enableClient" />
      </div></el-divider>
      <el-form-item label="客户端">
        <el-radio-group v-model="configEntity.client.impl" :disabled="!configEnable.enableClient">
          <el-radio value="html">网页端</el-radio>
          <el-radio value="api">APP端</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-divider content-position="left"><div class="gap-row">
        <el-text>域名设置</el-text>
        <el-switch v-model="configEnable.enableDomain" />
      </div></el-divider>
      <el-form-item label="网页端域名配置">
        <el-select
            v-model="configEntity.client.domain.html"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入域名"
            :disabled="!configEnable.enableDomain"
        />
      </el-form-item>
      <el-form-item label="APP端域名配置">
        <el-select
            v-model="configEntity.client.domain.api"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入域名"
            :disabled="!configEnable.enableDomain"
        />
      </el-form-item>
      <el-divider content-position="left"><div class="gap-row">
        <el-text>配置请求</el-text>
        <el-switch v-model="configEnable.enablePostman" />
      </div></el-divider>
      <el-form-item label="代理设置">
        <el-select allow-create filterable default-first-option clearable v-model="configEntity.client.postman.meta_data.proxies" :disabled="!configEnable.enablePostman" >
          <el-option key="null" label="null" value="null" />
          <el-option key="clash" label="clash" value="clash" />
          <el-option key="v2ray" label="v2ray" value="v2ray" />
        </el-select>
      </el-form-item>
      <el-form-item label="Cookies">
        <el-input v-model="configEntity.client.postman.meta_data.cookies.AVS" clearable :disabled="!configEnable.enablePostman" />
      </el-form-item>
      <el-divider content-position="left"><div class="gap-row">
        <el-text>下载配置</el-text>
        <el-switch v-model="configEnable.enableDownload" />
      </div></el-divider>
      <el-form-item label="跳过已下载">
        <el-switch v-model="configEntity.download.cache" :disabled="!configEnable.enableDownload" />
      </el-form-item>
      <el-form-item label="图片解密">
        <el-switch v-model="configEntity.download.image.decode" :disabled="!configEnable.enableDownload" />
      </el-form-item>
      <el-form-item label="自动转换格式">
        <el-select allow-create filterable v-model="configEntity.download.image.suffix" :disabled="!configEnable.enableDownload"
                   :options="['null','.jpg','.png'].map(item=>{return{id:item, label:item, value:item }})"
        >
        </el-select>
      </el-form-item>
      <el-form-item label="同时下载的图片数">
        <el-input-number
            v-model="configEntity.download.threading.image"
            :min="1"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="!configEnable.enableDownload"
        />
      </el-form-item>
      <el-form-item label="同时下载的章节数">
        <el-input-number
            v-model="configEntity.download.threading.photo"
            :min="1"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="!configEnable.enableDownload"
        />
      </el-form-item>
      <el-divider content-position="left"><div class="gap-row">
        <el-text>文件夹规则配置</el-text>
        <el-switch v-model="configEnable.enableDirRule" disabled />
      </div></el-divider>
      <el-form-item label="保存位置">
        <el-input v-model="configEntity.dir_rule.base_dir" :disabled="!configEnable.enableDirRule" />
      </el-form-item>
      <el-form-item label="文件规则">
        <el-input v-model="configEntity.dir_rule.rule" :disabled="!configEnable.enableDirRule" />
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
// import {computed} from "vue";

import API from "@/config/axios/axiosInstance.js";
import {ElNotification} from "element-plus";

export default {
  name: "config",
  //引入模块
  components: {},
  //父级传入数据
  props: {
    // inData: Object,
  },
  computed: {
    dSize(){
      switch (this.$store.state.showMode) {
        case "pc":
          return "50%"
        case "phone":
        case "smailPhone":
          return "100%"
      }
    },
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

      configEnable:{
        enableClient: false,
        enableDomain: false,
        enablePostman: false,
        enableDownload: false,
        enableDirRule: false,
      },

      configEntity:{
        client:{
          impl: "html",
          postman:{
            meta_data:{
              proxies: "",
              cookies:{
                AVS: ""
              }
            },
          },
          domain:{
            html:[],
            api:[],
          },
        },
        retry_times: 5,
        download:{
          cache: true,
          image: {
            decode: true,
            suffix: ".jpg"
          },
          threading:{
            image: 30,
            photo: 16,
          }
        },
        dir_rule:{
          base_dir: "",
          rule: "",
        },
      },

      openJM: false,
      updateJMTask: {},
      // jMTaskId: "",

      openTestWindow: false,
      testCommand: "",
      testIsRun: false,
      testResult: "",
      testResultCharset: "",
    }
  },
  //方法
  methods: {
    readJMConfig: function () {
      API.post("/api/config/load").then(res=>{
        let data = API.isSucess(res);
        if(data){
          this.configEntity = Object.assign(this.configEntity, data);
          console.log("configEntity", data);
          this.checkNullConfig();
          API.defaultSuccessFuc();
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    saveJMConfig: function () {
      let data = {};
      // 客户端
      if(this.configEnable.enableClient){
        data.client = this.configEntity.client
      }else{
        data.client = {}
        data.client.postman = {}
        data.client.postman.meta_data = {}
      }
      // 域名
      if(this.configEnable.enableDomain){
        data.client.domain = this.configEntity.client.domain
      }
      // 请求
      if(this.configEnable.enablePostman){
        data.client.postman = this.configEntity.client.postman
      }
      // 下载
      if(this.configEnable.enableDownload){
        data.download = this.configEntity.download;
      }

      API.post("/api/config/save", data).then(res=>{
        let data = API.isSucess(res);
        if(data){
          //this.configEntity = data;
          API.defaultSuccessFuc()
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    checkNullConfig:function () {
      if(!this.configEntity.client){
        this.configEntity.client = {}
      }
      if(!this.configEntity.client.domain){
        this.configEntity.client.domain={}
      }
      if(!this.configEntity.client.postman){
        this.configEntity.client.postman={}
      }
      if(!this.configEntity.client.postman.meta_data){
        this.configEntity.client.postman.meta_data={}
      }
      if(!this.configEntity.client.postman.meta_data.cookies){
        this.configEntity.client.postman.meta_data.cookies={}
      }
      if(!this.configEntity.download){
        this.configEntity.download={}
      }
      if(!this.configEntity.download.image){
        this.configEntity.download.image = {}
      }
      if(!this.configEntity.download.threading){
        this.configEntity.download.threading={}
      }
      if(!this.configEntity.dir_rule){
        this.configEntity.dir_rule={}
      }
    },

    updateJM: function () {
      API.post("/api/config/update").then(res=>{
        let data = API.isSucess(res);
        if(data){
          //this.taskList = data;
          /*ElNotification({
            title: "开始更新",
            //message: "请稍后刷新任务列表查看下载详情",
            type: 'success',
            duration: 10000,
          })*/
          this.openJM = true;
          this.updateJMTask = data;
          this.updateJMLog();
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    updateJMLog: function () {
      let that = this;
      console.log("that.updateJMTask",that, that.updateJMTask)
      API.post("/api/download/g",{
        "id": that.updateJMTask.id
      }).then(res=>{
        let data = API.isSucess(res);
        if(data){
          console.log("that.updateJMTask", data, data["data"])
          that.updateJMTask = data;
          // that.updateJMTask = data[0];
          if(that.updateJMTask.running === true){
            window.setTimeout(()=>{
              that.updateJMLog();
            },1000)
          }
        }else {
          API.showDefErrMessage(res)
        }
      })
    },

    openTestWindow_: function () {
      this.testResult = ""
      this.openTestWindow = true
    },
    runTest: function () {
      let that = this;
      API.post("/api/config/testCommand",{
        "test": that.testCommand,
        "charset": that.testResultCharset
      }).then(res=>{
        let data = API.isSucess(res);
        if(data){
          that.testResult = data
        }else {
          API.showDefErrMessage(res)
        }
      })
    }
  },
  //启动事件
  mounted() {
  },
  //销毁
  beforeUnmount() {
  }
}
</script>
<style scoped>
@import "@/style/config.css";
</style>
<style lang="scss">
.phone .config, .smallPhone .config{
  .el-button-group{
    .el-drawer{
      width: 100% !important;
    }
  }
}
</style>