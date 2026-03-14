<template>
  <div class="library" ref="library">
    <div class="controller gap-row" ref="controller">
      <el-button-group>
        <el-button @click="listPath()" :size="showMode==='pc'?'':'small'" >
          <el-icon><HomeFilled /></el-icon>
          <span class="a-text">主页</span>
        </el-button>
        <el-button @click="listPath(nowPath)" :size="showMode==='pc'?'':'small'" >
          <el-icon><Refresh /></el-icon>
          <span class="a-text">刷新</span>
        </el-button>
        <el-button @click="listPath(faPath)" v-if="faPath" :size="showMode==='pc'?'':'small'" >
          <el-icon><Top /></el-icon>
        </el-button>
        <el-popover title="收藏夹" placement="bottom" width="90%">
          <template #reference>
            <el-button :size="showMode==='pc'?'':'small'" >
              <el-icon class="favorites"><Management /></el-icon>
            </el-button>
          </template>
          <div class="favoriteItem" v-for="(item,i) in favorites" :key="i">
            <el-tooltip
                class="box-item"
                effect="dark"
                :content="item.path"
                placement="right"
            >
              <el-icon><Warning /></el-icon>
            </el-tooltip>
            <el-link underline="always" truncated @click="listPath(item.path)">
              <el-icon><Management /></el-icon>
              <span class="name">
                {{ item.showName }}
              </span>
            </el-link>
            <el-button text @click="delFavorites(item.path)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </el-popover>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-text>
        <span class="a-hidden"><el-icon><Monitor /></el-icon></span>
        <span class="a-text">阅读方向</span>
      </el-text>
      <el-segmented v-model="setting.direction" :options="direction" @change="saveSetting" :size="showMode==='pc'?'':'small'" class="viewDirection" />
      <el-divider direction="vertical" />
      <div class="gap-row" v-if="isPathView">
        <el-icon @click="openViewSettingWindow=true;" style="color: var(--el-color-success);"><View /></el-icon>
        <el-text size="small" type="success" class="a-text">路径专属视图</el-text>
      </div>
      <el-icon @click="openViewSettingWindow=true;" v-if="!isPathView"><View /></el-icon>
      <el-drawer v-model="openViewSettingWindow" title="修改视图" class="viewSettingWindow"><!-- size="50%" -->
        <div class="box myScrollBar">
          <el-form label-width="auto" class="viewSetting">
            <el-form-item label="文件排序">
              <div>
                <el-select
                    v-model="setting.fileSortItem"
                    placeholder="Select"
                    size="small"
                    style="width: 6rem;"
                    @change="analyzePath(true)"
                >
                  <el-option
                      v-for="item in fileSortOptions"
                      :key="item.key"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
                <el-switch v-model="setting.fileSort" active-text="↑" inactive-text="↓" @change="analyzePath(true)" />
              </div>
            </el-form-item>
            <el-form-item label="文件分组">
              <el-segmented v-model="setting.fileGroup" :options="groupOptions" size="default" @change="analyzePath(true)" />
            </el-form-item>
            <el-form-item label="最多显示Tag">
              <el-segmented v-model="setting.maxShowFileTag" :options="showMaxTagOptions" size="default" @change="changeMaxShowFileTag" />
            </el-form-item>
            <el-space fill>
              <el-alert type="info" show-icon :closable="false">
                <p>如果您的手机浏览器出现<el-text tag="ins">底部统计栏</el-text>超出屏幕，可尝试使用该选项修复</p>
                <p>如果页面无法滚动，则可能浏览器版不支持svh高度，可尝试使用其他模式或更新您的浏览器</p>
              </el-alert>
              <el-form-item label="页面高度修正">
                <el-input-number v-model="setting.phoneHeightFix" :min="0"  @change="changePhoneHeightFix" >
                  <template #suffix>
                    <span>px</span>
                  </template>
                </el-input-number>
                <el-divider direction="vertical" />
                <el-segmented v-model="setting.phoneHeightFixCompatibility" :options="phoneHeightFixCompatibilityOptions" size="default" @change="changePhoneHeightFix" />
              </el-form-item>
            </el-space>
          </el-form>
          <div class="line">
            <el-tooltip
                class="box-item"
                effect="dark"
                content="其实修改后会自动保存的"
                placement="top"
            >
              <el-button type="primary" plain @click="saveSetting">保存到浏览器默认视图</el-button>
            </el-tooltip>
            <el-button type="success" @click="saveViewToPath">保存路径视图</el-button>
          </div>
        </div>
      </el-drawer>
    </div>
    <el-divider class="smailDivider" />
    <el-scrollbar>
      <div class="fileView" :style="styleAuto" >
      <el-segmented v-model="fastFileFilter" :options="fastFileFilterOptions" @change="analyzePath()" :size="showMode==='pc'?'':'small'" class="fastFileFilter" />
      <list-card v-model:in-files="filesFilterResult"
                 v-loading.fullscreen.lock="isLoad"
                 @updateData="updateFileItem" @openPath="listPath"
                 @openView="openImage" @delFile="delFilePath"
                 :inTags="tagOptions.out"
                 :maxShowFileTag="maxShowFileTagVal"
                 v-if="setting.fileGroup==='关闭'"
                 :favoritesList="favorites" @refreshFavoritesList="listPath(nowPath)" @delFavorites="delFavorites"
                 :tagList="tagList" :useVirtual="true"
      />
      <div class="groupView" :style="styleAuto">
<!--        <VList class="groupFileView" :data="groupFiles" #default="{ item, index }" v-if="setting.fileGroup!=='关闭'">
          <div class="title">
            <el-divider content-position="left">{{ item.name }}</el-divider>
          </div>
          <list-card v-model:in-files="item.files"
                     v-loading.fullscreen.lock="isLoad"
                     @updateData="updateFileItem" @openPath="listPath"
                     @openView="openImage" @delFile="delFilePath"
                     :inTags="tagOptions.out"
                     :maxShowFileTag="maxShowFileTagVal"
                     :favoritesList="favorites" @refreshFavoritesList="listPath(nowPath)" @delFavorites="delFavorites"
                     :tagList="tagList"
          />
        </VList>-->
        <div class="groupFileView" v-for="(item,i) in groupFiles" :key="i" v-if="setting.fileGroup!=='关闭'">
          <div class="title">
            <el-divider content-position="left">{{ item.name }}</el-divider>
          </div>
          <list-card v-model:in-files="item.files"
                     v-loading.fullscreen.lock="isLoad"
                     @updateData="updateFileItem" @openPath="listPath"
                     @openView="openImage" @delFile="delFilePath"
                     :inTags="tagOptions.out"
                     :maxShowFileTag="maxShowFileTagVal"
                     :favoritesList="favorites" @refreshFavoritesList="listPath(nowPath)" @delFavorites="delFavorites"
                     :tagList="tagList"
          />
        </div>
      </div>
    </div>
    </el-scrollbar>
    <div class="imageView" v-if="viewImage">
      <vertical v-model:inData="filterFiles" v-model:inSwitchView="viewImage"
                :setDirection="setting.direction" :fileIndex="fileIndex"
                :faFile="faFile" :faFileIndex="faFileIndex"
                @openBeforePath="openBeforePath" @openNextPath="openNextPath"
                @switchPath="listPath" @updateData="updateFileItem" :showMode="showMode"
      />
    </div>
    <el-empty v-if="filesFilterResult==null || filesFilterResult.length===0" :description="loadEmptyText" />
<!--    <div class="grow1"></div>-->

    <!-- el-divider content-position="left" -->
    <div class="statusBottomBar">
      <el-button type="primary" @click="openBeforePath" size="small"
                 v-if="faFile && faFile.length>1 && faFileIndex>0"
      ><el-icon><Back /></el-icon></el-button>

      <el-popover
          popper-class="myScrollBar seenList"
          title="已阅快速跳转"
          placement="top-start"
          :disabled="!(fileQuickJump && fileQuickJump.length>0)"
          width="50vw"
          :teleported="false"
          ref="seenList"
      >
        <template #reference>
          <el-tag size="large" effect="light" >
            <el-icon ><Document /></el-icon>
            <span class="a-text">文件数量</span>
            <el-tag size="small" effect="dark">{{ status.num_files }}</el-tag>
            <el-icon><Folder /></el-icon>
            <span class="a-text">文件夹数量</span>
            <el-tag size="small" effect="dark">{{ status.num_dir }}</el-tag>
          </el-tag>
        </template>
        <div class="fileItem" v-for="(item,i) in fileQuickJump" :key="i">
          <el-link underline="always" line-clamp="3" v-if="item.isFile" @click="openImage(item.openIndex, 'seenList')">
            <el-icon color="#409EFF"><View /></el-icon>
            <el-icon><Document /></el-icon>
            {{ item.name }}
          </el-link>
          <el-link underline="always" line-clamp="3" v-else @click="listPath(item.path, files, i)">
            <el-icon color="#409EFF"><View /></el-icon>
            <el-icon><Folder /></el-icon>
            {{ item.name }}
          </el-link>
        </div>
      </el-popover>

      <el-button type="primary" @click="openNextPath" size="small"
                 v-if="faFile && faFile.length>1 && faFileIndex< faFile.length-1"
      ><el-icon><Right /></el-icon></el-button>

      <el-tooltip
          class="faFileName"
          v-if="faFile"
          effect="dark"
          :content="faFile[faFileIndex]?.name"
          placement="top-start"
      >
        <el-text truncated>
          {{ faFile[faFileIndex]?.name }}
        </el-text>
      </el-tooltip>

      <el-popover
          popper-class="myScrollBar ParentFileList"
          title="上级文件列表"
          placement="top-end"
          width="50vw"
          v-if="faFile && faFile.length>0"
          :teleported="false"
      >
        <template #reference>
          <el-button><el-icon><Memo /></el-icon></el-button>
        </template>
        <div class="fileItem" v-for="(item,i) in faFile" :key="i" >
          <el-link class="fileItemELink" underline="always"  line-clamp="3" :type=" faFileIndex===i?'primary':'' " >
            <div class="left" @click="listPath(item.path, faFile, i)">
              <el-icon v-if="faFileIndex===i" ><CaretRight /></el-icon>
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
    </div>
  </div>
</template>
<script>
import {computed, reactive, unref} from "vue";
import API from "@/config/axios/axiosInstance";
import ListCard from "@/components/library/ListCard.vue";
import Vertical from "@/components/image/Vertical.vue";
import ArraySortUtil from "@/utils/ArraySortUtil.js";
import {Hide, View, CaretBottom, CaretRight, CaretLeft} from "@element-plus/icons-vue";
// import { RecycleScroller } from 'vue-virtual-scroller'

export default {
  name: "library",
  computed: {
    Hide() {
      return Hide
    },
    View() {
      return View
    },
  },
  //引入模块
  components: {CaretLeft, CaretRight, CaretBottom, Vertical, ListCard
    // , RecycleScroller
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

      setting:{
        direction: "竖向",
        fileSortItem: "fileName",
        fileSort: true,
        fileGroup: "关闭",
        /*groupDate: "月",*/
        maxShowFileTag: "不限制",
        phoneHeightFix: 0,
        phoneHeightFixCompatibility: "vsh",
      },
      direction: ["竖向","从左到右","从右到左"],
      // direction: [
      //   {value: "竖向", icon: CaretBottom},
      //   {value: "从左到右", icon: CaretRight},
      //   {value: "从右到左", icon: CaretLeft},
      // ],
      fileSortOptions: [
        {key:"fileName", label:"文件名", value:"fileName"}
        ,{key:"changeTime", label:"修改日期", value:"changeTime"}
      ],
      groupOptions: ["关闭","时间","标记","评分"],
      openViewSettingWindow: false,
      /*dateOptions: ["年","月","日"],*/
      showMaxTagOptions: ["不限制","3个","6个"],
      maxShowFileTagVal: computed({
        get: () => {
          // console.log("maxShowFileTagVal - get", this.setting.maxShowFileTag)
          switch (this.setting.maxShowFileTag) {
            case "不限制":return 0;
            case "3个":return 3;
            case "6个":return 6;
          }
          return 0;
        },
        set: val => {
          // console.log("maxShowFileTagVal - set1", val, this.maxShowFileTag)
          switch (val) {
            case 0:this.maxShowFileTag = "不限制";break;
            case 3:this.maxShowFileTag = "3个";break;
            case 6:this.maxShowFileTag = "6个";break;
          }
          // console.log("maxShowFileTagVal - set2", val, this.maxShowFileTag)
        }
      }),
      phoneHeightFixCompatibilityOptions: ["svh","vh","js"],
      fastFileFilterOptions: ["显示全部","未阅读","已阅读"],
      fastFileFilter: "显示全部",

      // 显示模式
      showMode: "pc",

      files:[],
      filesFilterResult: [],
      groupFiles:[],
      filterFileText: "",
      // 过滤显示快速跳转已读列表
      fileQuickJump: computed({
        get: () => {
          let tmp = [];
          let fileIndex = -1;
          for (const fileItem of this.files) {
            if(fileItem.isFile){
              fileIndex++;
            }
            if(fileItem.isSeen){
              if(fileItem.isFile){
                fileItem.openIndex=fileIndex;
              }
              tmp.push(fileItem)
            }
          }
          console.log("fileQuickJump", tmp)
          return tmp;
        },
        set: val => {
        }
      }),

      nowPath: "",
      isLoad: false,
      isLoadTag: false,
      loadEmptyText: "目录为空",
      viewImage: false,
      filterFiles: [],

      fileIndex: 0,

      //statusText: "",
      status:{
        num_files: 0,
        num_dir: 0,
      },

      tagOptions: {
        buff: null,
        mode: "",
        out: [],
      },

      styleVal:{
        controllerHeight: 32,
      },
      styleAuto: {
      },

      faPath: "",
      faFile: [],
      faFileIndex: 0,

      groupViewShowTag: null,

      // 收藏夹
      favorites: [],

      // 特殊Tag列表
      tagList: {},
      tagListOut: [],

      //libraryHeight: 0,
      isPathView: false,
    }
  },
  //方法
  methods: {
    listPath: async function (path, faFile, faFileIndex) {
      this.files = [];
      this.filesFilterResult=[];
      this.isLoad = true;
      this.analyzePath();
      this.nowPath = path;
      let that = this
      that.loadEmptyText = "数据加载中...";
      console.log("传入参数:\n切换路径", path, "\n父级文件：", faFile, "\n父级文件Index", faFileIndex,
          "\n<><><>\n父级路径：", that.faPath,
          "\n父级文件", that.faFile,
          "\n父级文件Index", that.faFileIndex,
      "\n<><><>")

      that.listTag();

      API.post("/api/library/",{"path":path}).then(async function (res){
        let data = API.isSucess(res);
        if(data){
          that.processFileResult(data)
          console.log("响应参数:\n切换路径", path, "\n父级文件：", faFile, "\n父级文件Index", faFileIndex,
              "\n<><><>\n响应 - 父级路径：", data.path,
              "\n响应 - 父级文件", data.parent,
              "\n响应 - 父级文件Index", data.index,
              "\n<><><>\n父级路径：", that.faPath,
              "\n父级文件", that.faFile,
              "\n父级文件Index", that.faFileIndex,
              "\n<><><>")
          /*that.$nextTick(() => {
            that.processFileResult(data)
            console.log("响应参数:\n切换路径", path, "\n父级文件：", faFile, "\n父级文件Index", faFileIndex,
                "\n<><><>\n响应 - 父级路径：", data.path,
                "\n响应 - 父级文件", data.parent,
                "\n响应 - 父级文件Index", data.index,
                "\n<><><>\n父级路径：", that.faPath,
                "\n父级文件", that.faFile,
                "\n父级文件Index", that.faFileIndex,
                "\n<><><>")
          });*/
          /*setTimeout(()=>{
            that.processFileResult(data)
            console.log("响应参数:\n切换路径", path, "\n父级文件：", faFile, "\n父级文件Index", faFileIndex,
                "\n<><><>\n响应 - 父级路径：", data.path,
                "\n响应 - 父级文件", data.parent,
                "\n响应 - 父级文件Index", data.index,
                "\n<><><>\n父级路径：", that.faPath,
                "\n父级文件", that.faFile,
                "\n父级文件Index", that.faFileIndex,
                "\n<><><>")
          },2000)*/
        }else {
          API.showDefErrMessage(res)
        }
      }).finally(res=>{
        that.$nextTick(() => {
          that.isLoad = false;
          that.reSizeWindow();
          that.loadEmptyText = "目录为空";
        });
      })
    },
    /**
     * 处理文件返回数据
     * @param data
     */
    processFileResult: function (data) {
      let that = this
      //读取路径视图
      if(data.view){
        that.setting = JSON.parse(data.view);
        console.log("路径视图")
        that.isPathView = true;
      }else{
        that.loadBrowserSetting();
        console.log("浏览器默认视图")
        that.isPathView = false;
      }

      that.files = data.nowPathFileItems;
      that.analyzePath();
      that.listFavorites();

      that.faPath = "";
      that.faFile = [];
      that.faFileIndex = 0;
      if(data.path){
        that.faPath = data.path;
      }
      if(data.parent){
        that.faFile = data.parent;
      }
      if(data.index){
        that.faFileIndex = data.index;
      }
      //await that.getParentPath(path);

      //判定是否在图片预览状态下切换目录
      if(that.viewImage===true){
        //that.openImage(that.fileIndex)
        that.openImage(0)
        // console.log("更新当前图片预览")
        that.viewImage=false;
        that.$nextTick(() => {
          that.viewImage=true;
        });
      }

    },
    updateFileItem: function (item) {
      API.post("/api/library/update", item).then(res=>API.defaultSuccessFuc)
      this.analyzePath();
    },
    openImage: function (index, refTarget) {
      this.filterFiles = [];

      for (let i = 0; i < this.files.length; i++) {
        let fileItem = this.files[i];
        if(fileItem.isFile){
          this.filterFiles.push(fileItem);
        }
      }

      this.viewImage = true;

      console.log("打开图片", index)
      this.fileIndex = index;
      /*this.$nextTick(() => {
        console.log("this.$refs=", this.$refs, this)
      });*/
      if(refTarget){
        //console.log("unref", unref, this)
        this.$refs[refTarget]?.hide?.()
      }
    },
    delFilePath: function (path) {
      API.post("/api/library/del", {"path": path}).then(res=>{
        let data = API.isSucess(res);
        if(data){
          API.TipSuccess("路径已删除")
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    saveSetting: function () {
      localStorage.setItem("library.setting", JSON.stringify(this.setting));
    },
    /**
     * 前端解析目录数据
     */
    analyzePath: function (willSaveSetting=false) {
      this.status.num_files=0;
      this.status.num_dir=0;
      let that = this;
      this.tag_init();
      this.filesFilterResult = [];
      for (const file of this.files) {
        if( file.isFile ){
          this.status.num_files++;
        }else{
          this.status.num_dir++;
        }

        if(typeof file.mark === 'string' && file.mark.trim() !== ""){
          try {
            file.mark = JSON.parse(file.mark);
          } catch (e) {
            file.mark = [file.mark]
          }
        }

        if(file.mark){
          for (let tag of file.mark) {
            this.tag_add(tag);
          }
        }

        file.show = true;
        //过滤
        if(this.fastFileFilter!=="显示全部"){
          if( (file.isSeen===true) !== (this.fastFileFilter==="已阅读") ){
            file.show = false;
          }
        }
        // TODO 文件名、标签 过滤
        if(file.show===true){
          this.filesFilterResult.push(file);
        }
      }
      this.tag_get();

      // 排序
      /*this.files.sort(function (a, b) {
        //console.log("this.files.sort", that.setting.fileSortItem)
        switch (that.setting.fileSortItem) {
          case "fileName":{
            if(that.setting.fileSort){
              return ArraySortUtil.compare(a.name, b.name)
            }else{
              return ArraySortUtil.compare(b.name, a.name)
            }
          }
          case "changeTime":{
            if(that.setting.fileSort){
              return new Date(b.time).getTime() - new Date(a.time).getTime()
            }else{
              return new Date(a.time).getTime() - new Date(b.time).getTime()
            }
          }
          default:
            console.log("this.files.sort", "default",that.setting.fileSortItem)
        }
      })*/

      switch (that.setting.fileSortItem) {
        case "fileName":{
          let sortFun = function (a, b) {
            if(that.setting.fileSort){
              return ArraySortUtil.compare(a.name, b.name)
            }else{
              return ArraySortUtil.compare(b.name, a.name)
            }
          }
          that.files.sort(sortFun)
          that.filesFilterResult.sort(sortFun)
        }
        break;
        case "changeTime":{
          let timeSortFun = function (a, b) {
            if(that.setting.fileSort){
              return new Date(a.time).getTime() - new Date(b.time).getTime()
            }else{
              return new Date(b.time).getTime() - new Date(a.time).getTime()
            }
          }
          that.files.sort(timeSortFun)
          that.filesFilterResult.sort(timeSortFun)
        }
        break;
        default:
          console.log("this.files.sort", "default",that.setting.fileSortItem)
      }

      // 分组
      this.fileGroup();

      if(willSaveSetting===true){
        this.saveSetting();
      }

      this.reSizeWindow();
    },
    fileGroup: function () {
      this.groupFiles = [];

      // 分组
      if(this.setting.fileGroup === "时间"){
        this.groupFiles = [
          {name: "今天", files: []}
          ,{name: "昨天", files: []}
          ,{name: "这周", files: []}
          ,{name: "这月", files: []}
          ,{name: "半年内", files: []}
          ,{name: "很久之前", files: []}
        ];
        let now_d = new Date().clearhhmmss();
        let today = now_d.Format("yyyy-MM-dd");
        let yesterday = now_d.addDayReturnNew(-1).Format("yyyy-MM-dd");
        let day = Math.floor(now_d.getTime() / 1000 / 3600 / 24);
        let month = now_d.Format("yyyy-MM");

        for (const file of this.filesFilterResult) {
          let time = new Date(file.time).clearhhmmss();
          let fileDay = time.Format("yyyy-MM-dd");
          let day_file = Math.floor(time.getTime() / 1000 / 3600 / 24);
          let month_file = time.Format("yyyy-MM");

          if(today === fileDay){// 今天
            this.groupFiles[0].files.push(file)
          }else if(yesterday === fileDay){// 昨天
            this.groupFiles[1].files.push(file)
          }else if( (day - day_file)<=7 ){// 这周
            this.groupFiles[2].files.push(file)
          }else if(month === month_file){// 这月
            this.groupFiles[3].files.push(file)
          }else if( (day - day_file)<=183 ){//半年内
            this.groupFiles[4].files.push(file)
          }else{
            this.groupFiles[5].files.push(file)
          }
        }
      }
      if(this.setting.fileGroup === "评分") {
        this.groupFiles = [
          {name: "五星", files: []}
          , {name: "四星", files: []}
          , {name: "三星", files: []}
          , {name: "二星", files: []}
          , {name: "一星", files: []}
          , {name: "未评分", files: []}
        ];
        for (const file of this.filesFilterResult) {
          switch (file.rate) {
            case 5: this.groupFiles[0].files.push(file);break;
            case 4: this.groupFiles[1].files.push(file);break;
            case 3: this.groupFiles[2].files.push(file);break;
            case 2: this.groupFiles[3].files.push(file);break;
            case 1: this.groupFiles[4].files.push(file);break;
            default:
              this.groupFiles[5].files.push(file);
          }
        }
      }
      if(this.setting.fileGroup === "标记") {
        for (const tag of this.tagOptions.out) {
          this.groupFiles.push({name: tag, files: []})
        }
        this.groupFiles.push({name: "未标记", files: []})
        for (const file of this.filesFilterResult) {
          if(file.mark === ''){
            this.groupFiles[this.tagOptions.out.length].files.push(file);
            continue;
          }
          //let tags = JSON.parse(file.mark);
          let tags = file.mark;
          for (const tagItem of this.groupFiles) {
            // console.log("tagItem.name", tags, tagItem.name)
            if(tags.indexOf(tagItem.name)>-1){
              tagItem.files.push(file)
            }
          }
        }
      }

      // 外部分组排序
      if(this.setting.fileSort===true){
        this.groupFiles.reverse();
      }

      let buff = [];
      // 清空空组
      for (let i = 0; i < this.groupFiles.length; i++) {
        let tmp = this.groupFiles[i];
        if(tmp.files.length>0){
          buff.push(tmp);
        }
      }
      this.groupFiles = buff;
    },
    tag_init: function () {
      if( Set ){
        this.tagOptions.buff = new Set();
        this.tagOptions.mode = "set";
      }else{
        this.tagOptions.buff = {};
        this.tagOptions.mode = "object";
      }
    },
    tag_add: function (item) {
      switch (this.tagOptions.mode) {
        case "set":{
          this.tagOptions.buff.add(item)
        }break;
        default:{
          this.tagOptions.buff[item] = {}
        }
      }
    },
    tag_get: function () {
      switch (this.tagOptions.mode) {
        case "set":{
          this.tagOptions.out = Array.from(this.tagOptions.buff);
        }break;
        default:{
          this.tagOptions.out = Object.keys(this.tagOptions.buff);
        }
      }
    },
    reSizeWindow: function () {
      this.styleVal.controllerHeight = this.$refs["controller"].clientHeight
      this.styleAuto = {
        //"max-height": "calc(100vh - 168px - " + this.styleVal.controllerHeight + "px + 1.5rem)"
        // "max-height": `calc(100vh - 168px - ${this.styleVal.controllerHeight}px - ${this.setting.phoneHeightFix}px + 1.5rem)`,
        //"max-height": `calc(100svh - 168px - ${this.styleVal.controllerHeight}px - ${this.setting.phoneHeightFix}px + 1.5rem)`
      };
      switch (this.setting.phoneHeightFixCompatibility) {
        case "vh":
          this.styleAuto["max-height"] = `calc(100vh - 168px - ${this.styleVal.controllerHeight}px - ${this.setting.phoneHeightFix}px + 1.5rem)`;
          break;
        case "js":
          this.styleAuto["max-height"] = `calc(var(--pHeight) - 168px - ${this.styleVal.controllerHeight}px - ${this.setting.phoneHeightFix}px + 1.5rem)`;
          break;
        case "svh":
        default:
          this.styleAuto["max-height"] = `calc(100svh - 168px - ${this.styleVal.controllerHeight}px - ${this.setting.phoneHeightFix}px + 1.5rem)`;
      }
      console.log("reSizeWindow", this, this.styleVal.controllerHeight, this.styleAuto)

      let width = window.innerWidth;
      let w1= 720;
      let w2= 450;
      if(width>w1){
        this.direction = ["竖向","从左到右","从右到左"];
        switch (this.setting.direction) {
          case "竖向":case "↓↓":case "↓":this.setting.direction="竖向";break;
          case "从左到右":case "|→":case "→":this.setting.direction="从左到右";break;
          case "从右到左":case "←|":case "←":this.setting.direction="从右到左";break;
        }
        this.showMode = "pc"
      }else if(width<=w1 && width>w2){
        this.direction = ["↓↓","|→","←|"];
        switch (this.setting.direction) {
          case "竖向":case "↓↓":case "↓":this.setting.direction="↓↓";break;
          case "从左到右":case "|→":case "→":this.setting.direction="|→";break;
          case "从右到左":case "←|":case "←":this.setting.direction="←|";break;
        }
        this.showMode = "phone"
      }else{
        this.direction = ["↓","→","←"];
        switch (this.setting.direction) {
          case "竖向":case "↓↓":case "↓":this.setting.direction="↓";break;
          case "从左到右":case "|→":case "→":this.setting.direction="→";break;
          case "从右到左":case "←|":case "←":this.setting.direction="←";break;
        }
        this.showMode = "smailPhone"
      }
    },

    openBeforePath: function () {
      if(this.faFile && this.faFile.length>1 && this.faFileIndex>0){
        let nextIndex = this.faFileIndex-1;
        let tmp = this.faFile[nextIndex];
        this.listPath(tmp.path, this.faFile, nextIndex)
      }else{
        API.TipError("文件夹不存在")
      }
    },
    openNextPath: function () {
      if(this.faFile && this.faFile.length>1 && this.faFileIndex< this.faFile.length-1){
        let nextIndex = this.faFileIndex+1;
        let tmp = this.faFile[nextIndex];
        this.listPath(tmp.path, this.faFile, nextIndex)
      }else{
        API.TipError("文件夹不存在")
      }
    },

    changeMaxShowFileTag: function (item) {
      //console.log("changeMaxShowFileTag", aaa, bbb);
      this.setting.maxShowFileTag = item
      switch (this.setting.maxShowFileTag) {
        case "不限制":this.maxShowFileTagVal=0;break;
        case "3个":this.maxShowFileTagVal=3;break;
        case "6个":this.maxShowFileTagVal=6;break;
      }
    },
    /**
     * 修改高度单位
     *
     * （部分浏览器可能对vh兼容异常，特别是手机浏览器，可能会出现高度失效、状态栏遮挡等）
     *
     * *只针对本页面生效
     */
    changePhoneHeightFix: function () {
      this.reSizeWindow();
      this.saveSetting();
    },
    /**
     * 获取收藏夹数据
     */
    listFavorites: function () {
      API.post("/api/library/favorites/list").then(res=>{
        let data = API.isSucess(res);
        if(data){
          this.favorites = data;
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    delFavorites: function (path) {
      API.post("/api/library/favorites/del",{"path": path}).then(res=>{
        let data = API.isSucess(res);
        if(data){
          API.TipSuccess("删除成功")
          this.listFavorites();
          // this.listPath(this.nowPath);
        }else {
          API.showDefErrMessage(res)
        }
      })
    },
    /**
     * 获取父级目录数据
     *
     * （即将废除）
     * @param path 当前路径
     * @returns {Promise<void>}
     */
    getParentPath: async function (path) {
      let that = this;
      await API.post("/api/library/parent",{"path":path}).then(res=> {
        let data = API.isSucess(res);
        if (data) {
          that.faPath = data.path;
          that.faFile = data.parent;
          that.faFileIndex = data.index;
        }else{
          that.faPath = "";
          that.faFile = [];
          that.faFileIndex = 0;
        }
      });
    },
    /**
     * 获取特殊标签
     */
    listTag: function () {
      let that = this;
      that.isLoadTag = true;
      that.tagList = {};
      that.tagListOut = [];

      API.post("/api/tag/").then(function (res){
        let data = API.isSucess(res);
        if(data){
          //that.tagList = data;
          // console.log("tagList", data)
          for (const tagItem of data) {
            that.tagList[tagItem.name] = tagItem;
            that.tagListOut.push(tagItem.name)
          }
        }else {
          API.showDefErrMessage(res)
        }
      }).finally(res=>{
        that.$nextTick(() => {
          that.isLoadTag = false;
        });
      })
    },
    /**
     * 读取浏览器保存的视图设置
     */
    loadBrowserSetting: function () {
      let setting = localStorage.getItem("library.setting");
      if(setting!=null && typeof setting === "string"){
        this.setting = JSON.parse(setting);
      }
    },
    /**
     * 保存视图设置到路径
     */
    saveViewToPath: function () {
      //savePathView
      let that = this;
      let nowPath = this.nowPath;
      let view = JSON.stringify(that.setting);
      API.post("/api/library/savePathView",{
        "path": nowPath,
        "view": view
      }).then(function (res){
        let data = API.isSucess(res);
        if(data){
          API.TipSuccess("路径专属视图已保存");
        }else {
          API.showDefErrMessage(res);
        }
      })
    }
  },
  //启动事件
  mounted() {
    this.listPath();
    this.loadBrowserSetting();

    //计算空白窗体的高度
    // this.libraryHeight = this.$refs["library"].clientHeight;
    // document.documentElement.style.setProperty('--defHeight', `${this.libraryHeight}px`);
    // let libraryHeight = this.$refs["library"].clientHeight;
    // document.documentElement.style.setProperty('--defHeight', `${libraryHeight}px`);

    window.addEventListener('resize', this.reSizeWindow);
    this.$nextTick(() => {
      this.reSizeWindow();
    });
  },
  //销毁
  beforeUnmount() {
    window.removeEventListener('resize', this.reSizeWindow);
  }
}
</script>
<style scoped>
@import "@/style/library.css";
</style>
<style lang="scss">
.library{
  .statusBottomBar .el-tag__content{
    display: flex;
    grid-gap: 0.5rem;
    gap: 0.5rem;
    justify-content: center;
    align-items: center;
  }
  .el-popper.el-tooltip.el-popover{
    max-height: calc(100svh - 32px - 1rem - var(--el-menu-horizontal-height));
    overflow-y: scroll;
  }
  /*
  .viewSettingWindow{
    width: 50% !important;
  }
  */
  .el-overlay>.el-splitter>.el-splitter-panel{
    flex-basis: 50% !important;
  }

  .viewDirection{
    .el-segmented__item-label{
      display: flex;
      justify-content: center;
      align-items: flex-start;
    }
  }
  .statusBottomBar{
    .ParentFileList{
      .fileItem {
        .el-link, .el-link__inner {
          width: 100%;
          justify-content: space-between;
        }
      }
    }
    .seenList{
      .fileItem {
        .el-link, .el-link__inner {
          width: 100%;
          justify-content: flex-start;
        }
        .el-link__inner{
          display: flex;
          gap: 0.5rem;
          align-items: center;
        }
      }
    }
  }
}
.phone .library, .smallPhone .library{
  .viewSettingWindow{
    width: 100% !important;
  }
}
</style>