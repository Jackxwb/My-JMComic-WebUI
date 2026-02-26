<template>
  <div class="ListCard">

<!--    <el-segmented v-model="showMode" :options="['原生','授权']" size="small" />-->

<!--    <RecycleScroller :items="files" :item-size="100" :grid-items="2" :item-secondary-size="260" class="vGrid">&lt;!&ndash; v-slot="{ item }" &ndash;&gt;
      <template v-slot="{ item }">-->
<!--  ...  -->
<!--      </template>
</RecycleScroller>-->

<!--    <Virtualizer :data="files" class="vGrid" #default="{ item, index }">-->
<!--  ...  -->
<!--    </Virtualizer>-->

<!--    <VueMasonryWall :items="files" :options="{width: 300, padding: 12}" #default="{ item, index }">
...
    </VueMasonryWall>-->

<!--    <Waterfall :list="files" :gutter="15" :space="15" :width="240">
      <template #default="{ item, url, index }">
        <el-card :class="item.isSeen?['fileItem','isSeen']:['fileItem']"
                 :shadow="item.isSeen?'never':'always'"
        >&lt;!&ndash; v-for="(item,i) in files" :key="i" &ndash;&gt;
          <template #header>
            <el-text line-clamp="3" class="fileTitle">{{ item.name }}</el-text>
          </template>
          &lt;!&ndash;      {{ encodeURI('/api/library/img?img=' + item.image) }}&ndash;&gt;
          <el-badge :value="item.subdirectories" type="primary" :is-dot="item.subdirectories<2"
                    class="subdirectories" :max="999" :offset="calculateOffset(item.subdirectories)"
                    v-if="item.isFile===false"
          >
            <el-image class="fileImage" :src=" encodeURI('/api/library/img?token=' + getToken() + '&img=' + encodeURIComponent(item.image)) " fit="cover"
                      @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)"
                      v-if="showMode==='原生'"
            />
            <TImg class="fileImage" :authSrc=" encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) " fit="cover"
                  @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)" :is-lazy="true"
                  v-else
            />
            &lt;!&ndash; v-else &ndash;&gt;
          </el-badge>

          &lt;!&ndash;      <el-image class="fileImage" :src=" encodeURI('/api/library/img?token=' + getToken() + '&img=' + encodeURIComponent(item.image)) " fit="cover"
                          @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)"
                          v-if="showMode==='原生' && item.isFile===true"
                />&ndash;&gt;
          <TImg class="fileImage" :authSrc=" encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) " fit="cover"
                @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)" :is-lazy="true"
                v-if="item.isFile===true"
          />

          <template #footer>
            <div class="footer">
              <div class="line">
                <el-text size="small">{{ formatTime?formatTime(item.time):item.time }}</el-text>
                <span class="gap-row">
              <el-switch
                  v-model="item.isSeen"
                  :active-action-icon="View"
                  :inactive-action-icon="Hide"
                  @change="updateItem(item)"
              />
            </span>
              </div>
              <div class="line">
                <el-rate v-model="item.rate" clearable :colors="rateColor" @change="updateItem(item)" />
                <el-button v-if="isInFavoriteList(item.path)===true && !item.isFile" type="warning" link>
                  <el-icon class="favorites" @click="delFavorite(item.path)"><Management /></el-icon>
                </el-button>
                <el-button v-if="isInFavoriteList(item.path)===false && !item.isFile" link>
                  <el-icon @click="addFavorite(item.path)"><Management /></el-icon>
                </el-button>
              </div>
              <div class="line">
                <el-select
                    v-model="item.mark"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    :reserve-keyword="false"
                    placeholder="标记"
                    @change="updateItem(item)"
                    :collapse-tags="isCollapseTags"
                    :max-collapse-tags="maxShowFileTag_"
                >
                  <el-option
                      v-for="item in outTags"
                      :key="item"
                      :label="item"
                      :value="item"
                  >
                    <MySpecialTag v-if="tagList[item]" :special-tag-data="tagList[item]">{{ item }}</MySpecialTag>
                    <span v-else>{{ item }}</span>
                  </el-option>
                  <template #tag>
                    &lt;!&ndash;                <span v-for="(mark,i) in item.mark" :key="i">
                                      <el-tag closable>{{ mark }}</el-tag>
                                    </span>&ndash;&gt;
                    <MySpecialTag v-for="(mark,i) in item.mark" :key="i" closable
                                  :special-tag-data="tagList[mark]"
                                  @close="delItemTag(item, mark)"
                    >
                      {{ mark }}
                    </MySpecialTag>
                  </template>
                </el-select>
                <div class="delButton">
                  <el-popconfirm
                      class="box-item"
                      title="是否删除？"
                      confirm-button-text="删除"
                      placement="top-start"
                      @confirm="delPath(item.path)"
                  >
                    <template #reference>
                      <el-icon><DeleteFilled /></el-icon>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
              <div class="line">
                <el-input
                    class="albumId"
                    v-model="item.aid"
                    placeholder="未记录"
                    readonly
                >
                  <template #prefix>
                    <el-text class="a-text" size="small">本子ID</el-text>
                    <el-text class="a-hidden" size="small">ID</el-text>
                  </template>
                  <template #suffix>
                    <el-button-group class="idChangeButtonGroup">
                      <el-tooltip
                          effect="dark"
                          content="记录本子ID"
                          placement="top-start"
                      >
                        <el-button @click="setAId(item)" size="small"><el-icon><Edit /></el-icon></el-button>
                      </el-tooltip>
                      <el-tooltip
                          effect="dark"
                          content="更新本子"
                          placement="top-start"
                      >
                        <el-button @click="downloadAlbum(item.aid)" :disabled="!item.aid" size="small"><el-icon><VideoPlay /></el-icon></el-button>
                      </el-tooltip>
                    </el-button-group>
                  </template>
                </el-input>
              </div>
            </div>
          </template>
        </el-card>
      </template>
    </Waterfall>-->

      <VirtualWaterfall
          class="VirtualWaterfall"
          :items="files"
          :calcItemHeight="calcItemHeight"
          :itemMinWidth="240" :gap="15"
      >
          <template #default="{ item }">
            <el-card :class="item.isSeen?['fileItem','isSeen']:['fileItem']"
                     :shadow="item.isSeen?'never':'always'" :ref="item.path"
            ><!-- v-for="(item,i) in files" :key="i" -->
              <template #header>
                <el-text line-clamp="3" class="fileTitle">{{ item.name }}</el-text>
              </template>
              <!--      {{ encodeURI('/api/library/img?img=' + item.image) }}-->
              <el-badge :value="item.subdirectories" type="primary" :is-dot="item.subdirectories<2"
                        class="subdirectories" :max="999" :offset="calculateOffset(item.subdirectories)"
                        v-if="item.isFile===false"
              >
                <el-image class="fileImage" :src=" encodeURI('/api/library/img?token=' + getToken() + '&img=' + encodeURIComponent(item.image)) " fit="cover"
                          @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)"
                          v-if="showMode==='原生'"
                />
                <TImg class="fileImage" :authSrc=" encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) " fit="cover"
                      @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)" :is-lazy="true"
                      v-else
                />
                <!-- v-else -->
              </el-badge>

              <!--      <el-image class="fileImage" :src=" encodeURI('/api/library/img?token=' + getToken() + '&img=' + encodeURIComponent(item.image)) " fit="cover"
                              @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)"
                              v-if="showMode==='原生' && item.isFile===true"
                    />-->
              <TImg class="fileImage" :authSrc=" encodeURI('/api/library/img?img=' + encodeURIComponent(item.image)) " fit="cover"
                    @dblclick="openPath(item.path, item.isFile, i)" @click="openPath(item.path, item.isFile, i)" :is-lazy="true"
                    v-if="item.isFile===true"
              />

              <template #footer>
                <div class="footer">
                  <div class="line">
                    <el-text size="small">{{ formatTime?formatTime(item.time):item.time }}</el-text>
                    <span class="gap-row">
              <el-switch
                  v-model="item.isSeen"
                  :active-action-icon="View"
                  :inactive-action-icon="Hide"
                  @change="updateItem(item)"
              />
            </span>
                  </div>
                  <div class="line">
                    <el-rate v-model="item.rate" clearable :colors="rateColor" @change="updateItem(item)" />
                    <el-button v-if="isInFavoriteList(item.path)===true && !item.isFile" type="warning" link>
                      <el-icon class="favorites" @click="delFavorite(item.path)"><Management /></el-icon>
                    </el-button>
                    <el-button v-if="isInFavoriteList(item.path)===false && !item.isFile" link>
                      <el-icon @click="addFavorite(item.path)"><Management /></el-icon>
                    </el-button>
                  </div>
                  <div class="line">
                    <el-select
                        v-model="item.mark"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        :reserve-keyword="false"
                        placeholder="标记"
                        @change="updateItem(item)"
                        :collapse-tags="isCollapseTags"
                        :max-collapse-tags="maxShowFileTag_"
                    >
                      <el-option
                          v-for="item in outTags"
                          :key="item"
                          :label="item"
                          :value="item"
                      >
                        <MySpecialTag v-if="tagList[item]" :special-tag-data="tagList[item]">{{ item }}</MySpecialTag>
                        <span v-else>{{ item }}</span>
                      </el-option>
                      <template #tag>
                        <!--                <span v-for="(mark,i) in item.mark" :key="i">
                                          <el-tag closable>{{ mark }}</el-tag>
                                        </span>-->
                        <MySpecialTag v-for="(mark,i) in item.mark" :key="i" closable
                                      :special-tag-data="tagList[mark]"
                                      @close="delItemTag(item, mark)"
                        >
                          {{ mark }}
                        </MySpecialTag>
                      </template>
                    </el-select>
                    <div class="delButton">
                      <el-popconfirm
                          class="box-item"
                          title="是否删除？"
                          confirm-button-text="删除"
                          placement="top-start"
                          @confirm="delPath(item.path)"
                      >
                        <template #reference>
                          <el-icon><DeleteFilled /></el-icon>
                        </template>
                      </el-popconfirm>
                    </div>
                  </div>
                  <div class="line">
                    <el-input
                        class="albumId"
                        v-model="item.aid"
                        placeholder="未记录"
                        readonly
                    >
                      <template #prefix>
                        <el-text class="a-text" size="small">本子ID</el-text>
                        <el-text class="a-hidden" size="small">ID</el-text>
                      </template>
                      <template #suffix>
                        <el-button-group class="idChangeButtonGroup">
                          <el-tooltip
                              effect="dark"
                              content="记录本子ID"
                              placement="top-start"
                          >
                            <el-button @click="setAId(item)" size="small"><el-icon><Edit /></el-icon></el-button>
                          </el-tooltip>
                          <el-tooltip
                              effect="dark"
                              content="更新本子"
                              placement="top-start"
                          >
                            <el-button @click="downloadAlbum(item.aid)" :disabled="!item.aid" size="small"><el-icon><VideoPlay /></el-icon></el-button>
                          </el-tooltip>
                        </el-button-group>
                      </template>
                    </el-input>
                  </div>
                </div>
              </template>
            </el-card>
          </template>
      </VirtualWaterfall>






  </div>
</template>
<script>
import {computed} from "vue";
import {Hide, View} from "@element-plus/icons-vue";
import TImg from "@/components/T-Img.vue";
import {ElMessageBox, ElNotification} from "element-plus";
import API from "@/config/axios/axiosInstance.js";
import MySpecialTag from "@/components/library/MySpecialTag.vue";
// import { RecycleScroller,DynamicScroller,DynamicScrollerItem } from 'vue-virtual-scroller'
// import { Virtualizer } from "virtua/vue";
// import VueMasonryWall from "vue-masonry-wall";
import { VirtualWaterfall } from '@lhlyu/vue-virtual-waterfall'
/* 不支持 virtual
import { LazyImg, Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
*/


export default {
  name: "ListCard",
  computed: {
    Hide() {
      return Hide
    },
    View() {
      return View
    },
  },
  //引入模块
  components: {MySpecialTag, TImg
    //, RecycleScroller,DynamicScroller,DynamicScrollerItem
    // ,Virtualizer
    // , VueMasonryWall
    , VirtualWaterfall
    /*, Waterfall, LazyImg*/

  },
  //父级传入数据
  props: {
    inFiles: Object,
    //inTags: Object,
    inTags: Array,
    // faFile: Array,
    // faFileIndex: Number,
    maxShowFileTag: [String, Number, Object],
    favoritesList: Array,
    tagList: Object,
  },
  data() {

    return {
      //双向绑定处理
      files: computed({
        get: () => {
          for (let i = 0; i < this.inFiles.length; i++) {
            let file = this.inFiles[i];
            if(typeof file.mark === 'string' && file.mark.trim() !== ""){
              // file.mark = file.mark.split(",")
              // if(file.mark.indexOf(",")>0){
              //   file.mark = ""
              // }else{
              //
              // }
              try {
                file.mark = JSON.parse(file.mark);
              } catch (e) {
                file.mark = [file.mark]
              }
              this.inFiles[i] = file
            }
          }
          // console.log("this.inFiles", this.inFiles)
          return this.inFiles
        },
        set: val => {
          // if(!val){
          //   return;
          // }
          for (let i = 0; i < val.length; i++) {
            let item = val[i];
            if(Array.isArray(item.mark)){
              //item.mark = item.mark.join(",")
              item.mark = JSON.stringify(item.mark)
              val[i] = item
            }
          }
          //console.log("更新Item", val);
          this.$emit("update:inFiles", val);
        }
      }),

      rateColor: ['#99A9BF', '#F7BA2A', '#08bd1e'],

      showMode: '授权',

      maxShowFileTag_: computed({
        get: () => {
          // console.log("maxShowFileTag_ - get", this.maxShowFileTag, this.maxShowFileTag!=0 , this.maxShowFileTag==0);
          if(this.maxShowFileTag){
            return this.maxShowFileTag;
          }
          return 3;
        },
        set: val => {
          // console.log("maxShowFileTag_ - set", val);
          this.$emit("update:maxShowFileTag", val);
        }
      }),
      isCollapseTags: computed({
        get: () => {
          // console.log("isCollapseTags", this.maxShowFileTag!=0);
          return this.maxShowFileTag!=0
        },
        set: val => {
          // console.log("isCollapseTags - set", val);
        }
      }),

      outTags: computed({
        get: () => {
          if(this.inTags && this.inTags.length>0){
            return this.inTags;
          }
          if(this.tagList){
            return Object.keys(this.tagList);
          }
          return [];
        },
      })
    }
  },
  //方法
  methods: {
    updateItem: function (data) {
      if(Array.isArray(data.mark)){
        data.mark = JSON.stringify(data.mark)
      }
      this.$emit('updateData', data);
    },
    openPath: function(path, isFile, index){
      if(isFile){
        this.$emit('openView', index);
      }else{
        this.$emit('openPath', path, this.files, index);
      }
    },
    delPath: function (path) {
      this.$emit('delFile', path);
    },
    getToken: function () {
      // this
      let token = this.$store.state["user"].token;
      if(!token){
        token = localStorage.getItem("token")
      }
      if(!token){
        token = ""
      }
      return token;
    },
    setAId: function (data) {
      let that = this;
      ElMessageBox.prompt('本子ID', '记录本子ID', {
        confirmButtonText: '修改',
        cancelButtonText: '取消',
        inputValue: data.aid,
        //inputPattern: /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/,
        inputPattern: /\d+/,
        inputErrorMessage: 'ID异常',
      })
          .then(({ value }) => {
            data.aid = value;
            that.updateItem(data);
          })
          // .catch(() => {
          // })
    },
    downloadAlbum: function (id) {
      API.post("/api/download/d",{
        "id": id,
      }).then(res=>{
        let data = API.isSucess(res);
        if(data){
          ElNotification({
            title: "已开始下载",
            message: "请稍后刷新任务列表查看下载详情",
            type: 'success',
            duration: 10000,
          })
        }else {
          API.showDefErrMessage(res)
        }
      })
    },

    formatTime: function (time) {
      if(!time){return time}
      let date = new Date(time);
      if(date instanceof Date && !isNaN(date.getTime())){
        if(date.Format){
          return date.Format();
        }
        return time;
      }
      return time;
    },
    calculateOffset: function (itemNum) {
      if(itemNum<10){
        return [0,0]
      }
      if(itemNum<100){
        return [-4,0]
      }
      if(itemNum<1000){
        return [-8,0]
      }
      return [-12,0]
    },

    isInFavoriteList: function (path) {
      if(!this.favoritesList){
        return false;
      }
      for (let i = 0; i < this.favoritesList.length; i++) {
        let favorite = this.favoritesList[i];
        if(favorite.path == path){
          return true;
        }
      }

      return false;
    },
    addFavorite: function (path) {
      API.post("/api/library/favorites/add",{
        "path": path,
      }).then(res=>{
        let data = API.isSucess(res);
        if(data){
          API.TipSuccess("已添加")
          this.$emit("refreshFavoritesList");
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    delFavorite: function (path) {
      /*API.post("/api/library/favorites/del",{
        "path": path,
      }).then(res=>{
        let data = API.isSucess(res);
        if(data){
          API.TipSuccess("已添加")
          this.$emit("refreshFavoritesList");
        }else {
          API.showDefErrMessage(res)
        }
      })*/
      this.$emit("delFavorites", path);
    },

    delItemTag: function (data, tagName) {
      if(data.mark){
        let ii = data.mark.indexOf(tagName);
        data.mark.splice(ii, 1);
        this.updateItem(data);
      }
    },

    calcItemHeight: function (item, bbb) {
      //console.log("calcItemHeight", item, bbb, item.mark);

      // let element = this.$refs[aaa.path];
      // console.log("calcItemHeight", this.$refs, element);

      const box_baseHeight = 610;
      const box_gap = 6;
      const box_baseLine = 32;
      const box_baseWidth = 132;
      const item_Height = 26;
      const item_gap = 10;
      const lineStrMax = 6;

      if (Array.isArray(item.mark) && item.mark.length>0){
        let lineNum = 1;
        let lineStrNum = 0;

        item.mark.forEach(m=>{
          if( (lineStrNum + m.length + 1) > lineStrMax){
            lineNum++;
            lineStrNum = m.length + 1;
            console.log(lineNum, lineStrNum)
          }else{
            lineStrNum += m.length + 1;
            console.log(lineStrNum)
          }
        })
        if(lineStrNum === lineStrMax){
          lineNum++;
        }

        console.log("calcItemHeight", item.mark, lineNum);
        console.log(box_baseHeight + box_gap + (lineNum-1) * (item_Height + item_gap),
            item_Height + item_gap
        );

        return box_baseHeight + box_gap + (lineNum-1) * (item_Height + item_gap);

      }else{
        return box_baseHeight + box_gap;
      }


      return 750;
    }
  },
  //启动事件
  mounted() {
    /*console.log("test001", this.$store.state["user"].token)*/
  },
  //销毁
  beforeUnmount() {
  }
}
</script>
<style scoped>
@import "@/style/ListCard.css";
</style>
<style lang="scss">
.ListCard{
  .fileItem{
    .albumId{
      .el-input__inner{
        text-align: center;
      }
    }
  }

  //.vGrid{
  //  display: grid;
  //  grid-template-columns: repeat(auto-fit, 250px);
  //  &>div{
  //    width: auto !important;
  //  }
  //}
}
</style>