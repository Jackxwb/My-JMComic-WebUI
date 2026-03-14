import Mock from 'mockjs'  //导入mockjs

import apiSuccess from "./json/apiSuccess.json"
import apiFall from "./json/apiFail.json"

import library_list from "./json/library_list.json"
import library_list_file from "./json/library_list_file.json"
// import image0 from "./image/0.jpg"
import loginSuccess from "./json/login_success.json"
import getTask from "./json/getTask.json"
import downloadTaskList from "./json/download_task_list.json"
import downloadList from "./json/download_list.json"
import tagList from "./json/tag_list.json"
import favorites_list from "./json/favorites_list.json"

if(process.env.NODE_ENV === "development" && false) {

    // Mock.mock("/api/library/", library_list);
    Mock.mock("/api/library/", library_list_file);

    // Mock.mock("/api/library/img", (options) => {
    //     console.log("Mock.mock", "/api/library/img", options);
    // });
    /*Mock.mock("/api/library/img", (req, res) => {
            const imgPath = decodeURIComponent(req.query.img)
            console.log('模拟请求的图片路径:', imgPath, req, res)

            // 返回模拟响应
            res.json({
                code: 200,
                message: 'success',
                data: {
                    url: 'https://via.placeholder.com/300',
                    path: imgPath,
                    name: '模拟图片.jpg'
                }
            })
        }
    );*/

    Mock.mock("/api/config/update", apiSuccess);
    Mock.mock("/api/download/g", getTask);
    Mock.mock("/secure/login", loginSuccess);
    Mock.mock("/secure/check", apiSuccess);
    //Mock.mock("/api/download", downloadTaskList);
    Mock.mock("/api/download", downloadList);
    Mock.mock("/api/tag/", tagList);
    Mock.mock("/api/library/favorites/list", favorites_list);


}