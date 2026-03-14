//导入axios
import axios from 'axios'
import store from "@/config/store";
import {ElNotification} from "element-plus";
import {h} from "vue";

// console.log("axios - store", store)
//使用axios下面的create([config])方法创建axios实例，其中config参数为axios最基本的配置信息。
const API = axios.create({
    // baseUrl:"http://localhost:8080", //请求后端数据的基本地址，自定义
    // baseUrl: store.state.Setting.javaUrl,
    // timeout: 60 * 1000,                   //请求超时设置，单位ms
    timeout: 0,
})

// 给每个请求拦截一下,添加请求Token信息
API.interceptors.request.use(function (config) {
    let token = localStorage.getItem("token");
    // if(token){
        config.headers.Authorization = 'Bearer '+ token;
    // }
    return config;
})

API.isSucess = function (res={}){
    // console.log("isSucess", res)
    if(res["status"]==200 && res["data"]!=null){
        let response = res["data"];
        if(response["success"]) {
            if (response["data"]) {
                let resData = response["data"];
                return resData
            }
            return true
        }
    }
    return null;
}

API.readErrMessage = function (res) {
    // console.log("read Err", res)
    if(res.response){
        res = res.response
    }
    let errMessage
    if(res["data"]){
        errMessage = res["data"]
        if(errMessage["message"]){
            errMessage = errMessage["message"]
        }
    }else{
        if(res["status"]){
            errMessage = res["status"]
        }else if(res["message"]){
            errMessage = res["message"]
        }
    }
    return errMessage
}

API.showDefErrMessage = function (res, title="Error", showUrl=true, showTime=0) {
    // console.log("showDefErrMessage", res)
    let message = h('p', null, [
        // h('span', null, '数据删除失败！'),
        h('p', { style: 'color: red' }, API.readErrMessage(res)),
    ])

    if(showUrl){
        message = h('p', null, [
            // h('span', null, '数据删除失败！'),
            h('p', { style: 'color: red' }, API.readErrMessage(res)),
            h("p",[
                h("span", "URL:"),
                h("i", res.request.responseURL),
            ]),
        ])
    }

    ElNotification({
        title: title,
        message: message,
            // API.readErrMessage(res),
        type: 'error',
        duration: showTime,
    })
}

API.defaultSuccessFuc = function (res, title="Success", message="操作成功", showUrl=true, showTime=10000) {
    if(!res){
        ElNotification({
            title: title,
            message: message,
            type: 'success',
            duration: showTime,
        })
        return;
    }
    let data = API.isSucess(res);
    if(data){
        ElNotification({
            title: title,
            message: data,
            type: 'success',
            duration: showTime,
        })
    }else{
        ElNotification({
            title: title,
            message: message,
            type: 'success',
            duration: showTime,
        })
    }
}

//响应拦截器
API.interceptors.response.use(
    response => {
        // 对响应数据做一些处理
        // console.log("API全局响应处理", response)
        return response;
    },
    error => {
        // // 对响应错误做一些处理
        // if (error.response) {
        //     // 获取错误响应状态码
        //     const status = error.response.status;
        //     // 检查是否需要跳转到登录页面或其他特定页面
        //     if (status === 401) {
        //         // ...
        //     } else if (status === 404) {
        //         // ...
        //     } else {
        //         // ...
        //     }
        // } else {
        //     // 处理请求超时等其他错误
        //     // ...
        // }
        console.log("API全局错误", error)
        if (error.status === 200){
            return error;
        }
        if (error.status === 403){
            localStorage.removeItem("token")
        }
        API.showDefErrMessage(error)
        // 向全局catch传递错误
        return Promise.reject(error);
    }
);

API.TipSuccess = function (message="请求成功",title="API", showTime=10000) {
    ElNotification({
        title: title,
        message: message,
        type: 'success',
        duration: showTime,
    })
}
API.TipError = function (message="请求失败",title="API", showTime=10000) {
    ElNotification({
        title: title,
        message: message,
        type: 'error',
        duration: showTime,
    })
}

//导出我们建立的axios实例模块，ES6 export用法
export default API
