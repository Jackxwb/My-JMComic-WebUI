//from: https://blog.csdn.net/qq_56989560/article/details/124706021
//from: https://blog.csdn.net/qq_45934504/article/details/123462736
import { createStore } from 'vuex'
import user from "./user.js"

export default createStore({
    //提供唯一的公共数据源，所有共享的数据统一放到store的state进行储存，相似与data
    state: {
        javaUrl: "/api/",
        // loginUser: null,
        showMode: "pc",
    },
    //里面定义方法，操作state方法
    //更改 Vuex 的 store 中的状态的唯一方法是提交 mutation。Vuex 中的 mutation 非常类似于事件：每个 mutation 都有一个字符串的事件类型 (type)和一个回调函数 (handler)。这个回调函数就是我们实际进行状态更改的地方，并且它会接受 state 作为第一个参数：
    mutations: {
        reSizeWindow: function (state) {
            let width = window.innerWidth;
            let w1= 720;
            let w2= 450;
            if(width>w1){
                state.showMode = "pc"
            }else if(width<=w1 && width>w2){
                state.showMode = "phone"
            }else{
                state.showMode = "smailPhone"
            }
        }
    },
    // 操作异步操作mutation
    //Action和Mutation相似，一般不用Mutation 异步操作，若要进行异步操作，使用Action
    // 原因：为了方便devtools打个快照存下来，方便管理维护。所以说这个只是规范，而不是逻辑的不允许，只是为了让这个工具能够追踪数据变化而已
    actions: {
    },
    //类似于vue中的computed，进行缓存，对于Store中的数据进行加工处理形成新的数据
    Getter:{

    },
    //当遇见大型项目时，数据量大，store就会显得很臃肿
    // 为了解决以上问题，Vuex 允许我们将 store 分割成模块（module）。每个模块拥有自己的 state、mutation、action、getter、甚至是嵌套子模块——从上至下进行同样方式的分割：
    modules: {
        user,
    }
})