<template>
  <div class="Vertical imageView">
    <el-page-header @back="close" :class="showSwitchToolbar?'title':'title hide'">
      <template #content>
        <div class="headTitle">
          <el-tooltip
              class="box-item"
              effect="dark"
              content="默认开启虚拟化，提高性能。如果出现渲染错误，请关闭试试"
              placement="bottom-start"
          >
            <el-check-tag size="small" :checked="disableVirtual" type="danger" @change="switchVirtualBox">
              <span v-if="showMode==='pc'">禁用虚拟化</span>
              <el-icon v-else><Ship /></el-icon>
            </el-check-tag>
          </el-tooltip>

          <el-button type="primary" @click="openBeforePath"
                     v-if="faFile && faFile.length>1 && faFileIndex>0"
          ><el-icon><Back /></el-icon></el-button>

          <el-popover
              popper-class="myScrollBar"
              class="faFileName"
              title="上级文件列表"
              placement="bottom"
              width="50vw"
              v-if="faFile && faFile.length>0"
              :teleported="false"
          >
            <template #reference>
              <el-text truncated>
                {{ faFile[faFileIndex]?.name }}
              </el-text>
            </template>
<!--            <div class="fileItem" v-for="(item,i) in faFile" :key="i" @click="switchPath(item.path, faFile, i)">
              <el-link underline="always"
                       :type=" faFileIndex===i?'primary':'' "
              >
                <el-icon v-if="faFileIndex===i" ><CaretRight /></el-icon>
                {{ item.name }}
              </el-link>
            </div>-->
            <div class="fileItem" v-for="(item,i) in faFile" :key="i" >
              <el-link underline="always" :type=" faFileIndex===i?'primary':'' ">
                <div class="left" @click="switchPath(item.path, faFile, i)">
                  <el-icon v-if="now_slider===i" ><CaretRight /></el-icon>
                  {{ item.name }}
                </div>
                <div class="right">
                  <el-switch
                      v-model="item.isSeen"
                      :active-action-icon="View"
                      :inactive-action-icon="Hide"
                      @change="updateFileItem(item)"
                  />
                </div>
              </el-link>
            </div>
          </el-popover>

          <el-button type="primary" @click="openNextPath"
                     v-if="faFile && faFile.length>1 && faFileIndex< faFile.length-1"
          ><el-icon><Right /></el-icon></el-button>
        </div>
      </template>
    </el-page-header>

    <div :class="['VerticalImageBox noScrollBar', direction, disableVirtual?'disableVirtual':'virtual' ]" @click="switchToolbar" @scroll="handleScroll" ref="imageBox"
    >

<!--      <el-image v-for="(item,i) in files" :key="i" :src=" encodeURI('/api/library/img?img=' + item.image) "
                :ref="item.name"
      >
        <template #error>
          <div class="image-slot">
            <el-icon><icon-picture /></el-icon>
            <div>{{ encodeURI('/api/library/img?img=' + item.image) }}</div>
          </div>
        </template>
      </el-image>-->

<!--      <VList :data="files" #default="{ item, index }" class="vList" :bufferSize="5000" :itemSize="imageHeight">
        <TImg :authSrc="encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) "
              :ref="item.name"
        />
      </VList>-->

      <TImg v-for="(item,i) in displayFiles" :key="i" :authSrc=" encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) "
            :ref="item.name" v-if="disableVirtual"
      />

<!-- 移动端渲染异常！
      <RecycleScroller
          class="virtual-scroller noScrollBar"
          ref="virtualScroller"
          :items="displayFiles"
          :item-size="imageHeight"
          :direction="direction==='ver'?'vertical':'horizontal'"
          key-field="path"
          v-slot="{ item }"
          :buffer="imageHeight*3"
          @scroll.native="handleScroll"
          :emit-update="false"
          v-else
      >
        <TImg :authSrc="encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) "
              :ref="item.name"
        />
      </RecycleScroller>-->

<!--      <div
          ref="virtualScroller"
          class="virtual-scroller noScrollBar"
          :class="direction === 'ver' ? 'dir-vertical' : 'dir-horizontal'"
          @scroll="handleScroll"
          :style="virtualStyle"
      >
        <el-image v-for="(item,i) in virtualRows" :key="i" :src=" encodeURI('/api/library/img?img=' + item.image) "
                  :ref="item.name"/>
      </div>-->

      <div :style="virtualStyle">
        <el-image v-for="(virtualRow,i) in virtualRows" :key="virtualRow.index"
                  :src=" encodeURI('/api/library/img?img=' + displayFiles[virtualRow.index].image) "
                  :ref="displayFiles[virtualRow.index].name"
                  :style="virtualItemStyle(virtualRow)"
        />
      </div>

    </div>
    <div :class="showSwitchToolbar?'bottom':'bottom hide'">
      <el-popover
          popper-class="myScrollBar fileFastMark"
          title="文件快速标记"
          placement="top-end"
          width="70vw"
          :teleported="false"
      >
        <template #reference>
          <el-tag type="primary"><el-icon><Position /></el-icon></el-tag>
        </template>
        <div class="fileItem" v-for="(item,i) in files" :key="i" >
          <el-link class="fileItemELink" underline="always" line-clamp="3" :type=" now_slider===i?'primary':'' " >
            <div class="left" @click="changeScroll(i)">
              <el-icon v-if="now_slider===i" ><CaretRight /></el-icon>
              {{ item.name }}
            </div>
            <div class="right">
              <el-switch
                  v-model="item.isSeen"
                  :active-action-icon="View"
                  :inactive-action-icon="Hide"
                  @change="updateFileItem(item)"
              />
            </div>
          </el-link>
        </div>
      </el-popover>
      <el-slider v-model="now_slider" :max="files.length" @input="changeScroll" />
      <el-text size="large" @click="switchImage" @touchend="switchImage" >{{ now_slider }}/{{ files.length }}</el-text>
    </div>
  </div>
</template>
<script>
import {computed} from "vue";
import TImg from "@/components/T-Img.vue";
import {ElMessageBox} from "element-plus";
import {Hide, View} from "@element-plus/icons-vue";
import { VList, WindowVirtualizer } from "virtua/vue";
import { useVirtualizer } from '@tanstack/vue-virtual';

export default {
  name: "Vertical",
  computed: {
    Hide() {
      return Hide
    },
    View() {
      return View
    },
    direction(){
      switch (this.setDirection) {
        case "竖向":
        case "↓↓":
        case "↓":
          return "ver"
        case "从左到右":
        case "|→":
        case "→":
          return "lTor horizontal"
        case "从右到左":
        case "←|":
        case "←":
          return "rTol horizontal"
      }
    },
    imageHeight(){
      if(Array.isArray(this.inData)){
        for (let i = 0; i < this.inData.length; i++) {
          // let ratio = this.inData[i].ratio;
          // if(ratio && ratio!="null"){
          //   try {
          //     let ratio_number = parseFloat(ratio);
          //     let rect = document.body.getBoundingClientRect();
          //     /*let imageE = document.querySelector(".VerticalImageBox .T-Img");
          //     let rect;
          //     if(imageE){
          //       rect  = imageE.getBoundingClientRect();
          //     }else{
          //       rect = document.body.getBoundingClientRect();
          //     }*/
          //     const width = rect.width - 40;// 移除margin宽度
          //     const height = rect.height - 60;// 移除margin宽度
          //     switch (this.direction) {
          //       case "ver":
          //         console.log("ver", ratio_number, width / ratio_number);
          //         return width * ratio_number;
          //       default:
          //         console.log("other", ratio_number, height * ratio_number);
          //         return height / ratio_number;
          //     }
          //   }catch (e) {
          //     console.log("比例转换报错", e);
          //   }
          // }
          const result = this.sumImageHeightOrWidth(this.inData[i]);
          if(result)return result;
        }
      }
      return 1000;
    },

    virtualRows(){
      if(!this.rowVirtualizer){
        return [];
      }
      console.log("rowVirtualizer", this.rowVirtualizer);
      return this.rowVirtualizer.getVirtualItems()
    },
    virtualStyle(){//totalSize
      if(!this.rowVirtualizer){
        return [];
      }
      let style={
        position: 'relative'
      };
      if(this.direction === "ver"){
        style["width"] = "100%";
        style["height"] = this.rowVirtualizer.getTotalSize() + "px";
      }else{
        style["height"] = "100%";
        style["width"] = this.rowVirtualizer.getTotalSize() + "px";
      }
      console.log("rowVirtualizer", this.rowVirtualizer);
      return style;
    },
    virtualTotalSize(){
      if(!this.rowVirtualizer){
        this.initVirtualizer();
      }
      return this.rowVirtualizer.getTotalSize()
    },
  },
  watch: {
    // 监听输入数据的变化，一旦变化就重新开始分批加载
    inData: {
      immediate: true,
      handler(newVal) {
        this.startBatchLoad(newVal);
      }
    }
  },
  //引入模块
  components: {TImg, VList, WindowVirtualizer},
  //父级传入数据
  props: {
    inData: Object,
    inSwitchView: Boolean,

    showToolbar: true,
    setDirection: String,

    fileIndex: Number,

    faFile: Array,
    faFileIndex: Number,

    showMode: String,
  },
  data() {
    return {
      //双向绑定处理
      files: computed({
        get: () => this.inData,
        set: val => {
          this.$emit("update:inData", val);
        }
      }),
      displayFiles: [],
      animationFrameId: null,

      switchView: computed({
        get: () => this.inSwitchView,
        set: val => {
          this.$emit("update:inSwitchView", val);
        }
      }),

      showSwitchToolbar: true,

      now_slider: 0,
      inMove: false,

      disableVirtual: false,

      rowVirtualizer: null,
    }
  },
  //方法
  methods: {
    close: function () {
      this.$emit("update:inSwitchView", false);
    },
    switchToolbar: function () {
      this.showSwitchToolbar = !this.showSwitchToolbar;
    },
    handleScroll: function (event) {
      if(!this.inMove){
        console.log("handleScroll", event)
        let scrollHeight = event.target.scrollHeight;
        let scrollWidth = event.target.scrollWidth;
        if(this.disableVirtual===false){
          // 虚拟化
          scrollHeight = this.rowVirtualizer.getTotalSize();
          scrollWidth = this.rowVirtualizer.getTotalSize();
        }
        switch (this.setDirection) {
          case "竖向":case "↓↓":case "↓":{
            let all = scrollHeight - event.target.clientHeight;
            let now = event.target.scrollTop / all;
            console.log("now", now, all)
            this.now_slider = Math.round(now * this.files.length)
          }
          break;
          case "从左到右":case "|→":case "→":{
            let all = scrollWidth;
            let now = event.target.scrollLeft / all;
            console.log("now", now, all)
            this.now_slider = Math.round(now * this.files.length)
          }
          break;
          case "从右到左":case "←|":case "←":{
            // let nowIndex = event.target.scrollWidth + event.target.scrollLeft;
            // let now = nowIndex / event.target.scrollWidth;
            // // console.log("now", now)
            // this.now_slider = Math.round(now * this.files.length)

            let all = scrollWidth;
            let now = event.target.scrollLeft / all;
            console.log("now", now, all)
            this.now_slider = Math.round(now * this.files.length)
          }
          break;
        }
      }
    },
    changeScroll: function (index) {
      this.inMove = true;
      this.now_slider = index;

      if(this.disableVirtual){
        // 默认
        let arr = document.querySelector(".VerticalImageBox").querySelectorAll(".el-image");
        // console.log("changeScroll", index)
        switch (this.setDirection) {
          case "从右到左":{
            index = arr.length - index - 1;
          }
            break
        }
        if(index >= 0 && index < arr.length){
          let element = arr[index];
          element.scrollIntoView({ behavior: 'smooth' });
        }
      }else{
        // 虚拟化
        let scrollToItem = this.$refs["virtualScroller"]?.scrollToItem;
        if(scrollToItem){
          scrollToItem(index)
        }else{
          console.log("移动失败！虚拟化组件或方法未找到")
        }
      }


      let that = this;
      window.setTimeout(() => {
        that.inMove = false;
      },1000)
    },
    wheelFunc: function (event) {
      let that = this;
      let imageBox = that.$refs["imageBox"];
      // console.log('滚动了', event, event.deltaY, event.deltaX);
      let def = function () {
        //window.scrollBy(event.deltaY, 0)
        imageBox.scrollBy(event.deltaY, 0)
        // console.log("<>", event.deltaY, that, imageBox)
      }
      switch (this.setDirection) {
        case "从左到右":case "|→":case "→":
        case "从右到左":case "←|":case "←":
          def()
          break
      }
    },

    openBeforePath: function () {
      this.$emit('openBeforePath');
    },
    openNextPath: function () {
      this.$emit('openNextPath');
    },

    switchPath: function (path, faFile, faFileIndex) {
      this.$emit('switchPath', path, faFile, faFileIndex);
    },

    switchImage: function () {
      ElMessageBox.prompt('请输入要跳转到的图片(1~' + (this.files.length) + ')', '跳转图片', {
        confirmButtonText: '跳转',
        cancelButtonText: '取消',
        // inputPattern: /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/,
        // inputErrorMessage: 'Invalid Email',
        inputValidator: (value) => {
          const index = Number(value)
          if (isNaN(index)) {
            return '请输入数字'
          }
          if (index <= 1) {
            return '必须是正数';
          }
          if(index > this.files.length){
            return '超出范围';
          }
          return true
        }
      })
          .then(({ value }) => {
            /*ElMessage({
              type: 'success',
              message: `Your email is:${value}`,
            })*/
            this.changeScroll(value-1)
          })
          .catch(() => {
            /*ElMessage({
              type: 'info',
              message: 'Input canceled',
            })*/
          })
    },

    updateFileItem: function (data) {
      if(Array.isArray(data.mark)){
        data.mark = JSON.stringify(data.mark)
      }
      this.$emit('updateData', data);
    },

    switchVirtualBox: function () {
      this.disableVirtual = !this.disableVirtual;

      let tmp = this.now_slider;
      const that =this;
      setTimeout(()=>{
        that.changeScroll(tmp);
      }, 500);
    },

    sumImageHeightOrWidth: function (item) {
      let ratio = item.ratio;
      if(ratio && ratio!="null"){
        try {
          let ratio_number = parseFloat(ratio);
          let rect = document.body.getBoundingClientRect();
          /*let imageE = document.querySelector(".VerticalImageBox .T-Img");
          let rect;
          if(imageE){
            rect  = imageE.getBoundingClientRect();
          }else{
            rect = document.body.getBoundingClientRect();
          }*/
          const width = rect.width - 40;// 移除margin宽度
          const height = rect.height - 60;// 移除margin宽度
          switch (this.direction) {
            case "ver":
              console.log("ver", ratio_number, width / ratio_number);
              return width * ratio_number;
            default:
              console.log("other", ratio_number, height * ratio_number);
              return height / ratio_number;
          }
        }catch (e) {
          console.log("比例转换报错", e);
        }
      }
      return null;
    },

    initVirtualizer: function () {
      const that = this;
      let parentRef = this.$refs["imageBox"];
      this.rowVirtualizer = useVirtualizer({
        // 动态响应 count
        get count() { return that.displayFiles.length; },
        getScrollElement: () => parentRef,
        // 这里的 size 对应原项的尺寸
        estimateSize: (i) => {
          //return that.imageHeight;
          let result = this.sumImageHeightOrWidth(that.displayFiles[i]);
          return result?result:1000;
        },
        // 对应原 buffer，这里按个数算，建议设置 3-5 个
        overscan: 10,
        horizontal: that.direction !== 'ver'
      });
    },
    virtualItemStyle: function (virtualRow) {
      let style = {
        position: 'absolute',
        willChange: 'transform',
        top: 0,
        left: 0,
        // width: this.direction === 'ver' ? '100%' : 'auto',
        // height: this.direction === 'ver' ? 'auto' : '100%',
        transform: this.direction === 'ver'
            ? `translateY(${virtualRow.start}px) translateZ(0)`
            : `translateX(${virtualRow.start}px) translateZ(0)`,
      }
      console.log("virtualData", virtualRow)
      return style;
    },

    // 分批加载数据
    startBatchLoad(rawList) {
      // 1. 清理之前的任务，防止多次触发叠加
      cancelAnimationFrame(this.animationFrameId);
      this.displayFiles = [];

      // 2. 开始分批
      const batchSize = 20; // 建议初始值设小一点，保证首屏瞬间出来
      let current = 0;

      const next = () => {
        if (current < rawList.length) {
          // 取出一批数据
          const nextBatch = rawList.slice(current, current + batchSize);
          // 推入显示列表
          this.displayFiles.push(...nextBatch);
          current += batchSize;

          //setTimeout(()=>this.animationFrameId = requestAnimationFrame(next), 10)
          // 请求下一帧更新
          this.animationFrameId = requestAnimationFrame(next);
        }
      };

      next();
    },
  },
  //启动事件
  mounted() {
    this.$nextTick(() => {
      // console.log("this.$refs=",
      //     this.$refs, this, this.$refs["00181.jpg"]
      // )
      console.log("fileIndex", this.fileIndex)
      let that = this
      if(this.fileIndex){
        window.setTimeout(() => {
          that.changeScroll(that.fileIndex)
        },1000)
      }
    });

    document.addEventListener('wheel', this.wheelFunc);

    this.initVirtualizer();
  },
  //销毁
  beforeUnmount() {
    document.removeEventListener('wheel', this.wheelFunc);
  }
}
</script>
<style scoped>
@import "@/style/Vertical.css";
@import "@/style/ImageView.css";
</style>
<style lang="scss">
/*@import "@/style/Vertical.css";*/
.el-slider__runway{
  background: aquamarine;
}
.imageView{
  .el-page-header__left{
    width: 100%;
    &>div{
      flex-shrink: 0;
    }
    .el-page-header__content{
      flex-shrink: 1;
      flex-grow: 1;
      min-width: 0;
    }
  }
}
.library .Vertical .el-popper.el-tooltip.el-popover{
  max-height: calc(100vh - 128px);
  max-height: calc(100svh - 128px);
}
.fileFastMark{
  .fileItem{
    .fileItemELink{
      width: 100%;
      .el-link__inner{
        width: 100%;
        justify-content: space-between;
      }
    }
  }
}
</style>