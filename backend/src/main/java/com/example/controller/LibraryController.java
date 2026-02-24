package com.example.controller;

import com.example.entity.ApiResponse;
import com.example.entity.Favorite;
import com.example.entity.FileItem;
import com.example.entity.JSONResult.ParentResult;
import com.example.entity.PathView;
import com.example.service.BackendBashTask.DelFileFormRM;
import com.example.service.LibraryService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/library")
@RolesAllowed({ "User", "Admin" })
@RunOnVirtualThread
public class LibraryController {

//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public String hello() {
//        return "Hello from Quarkus REST";
//    }

    //@Inject
    //@Resource
    //LibraryService libraryService = new LibraryService();

    @Inject
    LibraryService libraryService;

    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "setting.dataPath", defaultValue = "./Archive")
    private String defPath;
    private static String mainPath = null;

    private String getDefPath(){
        // 优先读取环境变量
        String dataPath = System.getenv("data_path");
        if(dataPath!=null && !dataPath.isEmpty()){
            return dataPath;
        }
        // 没有的话返回设置中的路径
        return defPath;
    }
    private String getPath(){
        if(mainPath!=null){
            return mainPath;
        }

        String path = getDefPath();
        try {
            java.nio.file.Path path_ = Paths.get(path);
            path_ = path_.normalize();
            path_ = path_.toRealPath(LinkOption.NOFOLLOW_LINKS);
            //return path_.normalize().toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
            //return path_.toString();
            mainPath = path_.toString();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            log.error("路径转换失败!\r\n{}", e.getMessage());
            //return path;
            mainPath = path;
        }
        return mainPath;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    //@JwtSecured(roles = {"User", "Admin"})
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse listFile(@Valid Map<String, String> json){
//        JSONObject obj = JSON.parseObject(json);
//        String path = null;
//        if(obj!=null){
//            path = obj.getString("path");
//        }

        String path = json.get("path");

        // 空路径
        if (path==null || path.isEmpty()){
            path = getPath();
        }

        log.info("path: {}, getPath: {}", path, getPath());

        //判断路径是否溢出范围
        if(!path.startsWith(getPath())){
            path = getPath();
        }

        File file = new File(path);
        if(!file.exists()){
            return ApiResponse.returnFail("路径不存在");
        }
        if(file.isFile()){
            return ApiResponse.returnFail("非法的路径");
        }

        if(!file.canRead() || !file.canWrite() || !file.canExecute()){
            log.warn("权限异常: canRead: {}, canWrite: {}, canExecute: {}, <- {}"
                    ,file.canRead()
                    ,file.canWrite()
                    ,file.canExecute()
                    ,path
            );
        }

        List<FileItem> fileItems = libraryService.listFile(path);

        String parent = file.getParent();
        parent = FileItem.AutomaticProcessingPath(parent);
        ParentResult data = new ParentResult();
        data.nowPathFileItems = fileItems;
        if(parent.startsWith(getPath())){
            Integer index = null;
            List<FileItem> parentFileItems = libraryService.listFile(parent);
            for (int i = 0; i < parentFileItems.size(); i++) {
                if(path.equals(parentFileItems.get(i).path)){
                    index = i;
                    break;
                }
            }
            data.parent = parentFileItems;
            data.index = index;
            data.path = parent;
        }else{
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            //return ApiResponse.returnFail("不允许访问", 403);
            log.warn("不允许访问: {}", parent);
        }

        //读取视图
        PathView viewEntity = null;
        PanacheQuery<PanacheEntityBase> viewQuery = null;
        if(path == null || path.isEmpty()){
            viewQuery = PathView.find("path", getPath());
        }else{
            viewQuery = PathView.find("path", path);
        }

        if(viewQuery!=null){
            viewEntity = viewQuery.firstResult();
        }
        if(viewEntity!=null){
            data.view = viewEntity.getViewData();
        }

        //return ApiResponse.returnOK().setDataNow(fileItems);
        return ApiResponse.returnOK().setDataNow(data);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("savePathView")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse savePathView(@Valid Map<String, String> json){
        String path = json.get("path");
        String view = json.get("view");

        if(path==null || path.isEmpty()){
            //return ApiResponse.returnFail("路径为空");
            path = getPath();
        }

        // 防止路径溢出
        if(!path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }
        PathView viewEntity = null;
        PanacheQuery<PanacheEntityBase> viewQuery = PathView.find("path", path);
        if(viewQuery!=null){
            viewEntity = viewQuery.firstResult();
        }
        if(viewEntity==null){
            viewEntity = new PathView();
        }
        viewEntity.setPath(path);
        viewEntity.setViewData(view);

        if(viewEntity.id == null){
            entityManager.persist(viewEntity); // 新实体，持久化
        }else{
            entityManager.merge(viewEntity); // 脱管状态，合并
        }

        return ApiResponse.returnOK();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("parent")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse getParentInfo(@Valid Map<String, String> json){
        String path = json.get("path");

        if(path==null || path.isEmpty()){
            return ApiResponse.returnFail("路径为空");
        }

        // 防止路径溢出
        if(!path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }
        String parent = new File(path).getParent();
        parent = FileItem.AutomaticProcessingPath(parent);
        if(!parent.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }
        Integer index = null;
        List<FileItem> fileItems = libraryService.listFile(parent);
        for (int i = 0; i < fileItems.size(); i++) {
            if(path.equals(fileItems.get(i).path)){
                index = i;
                break;
            }
        }
        ParentResult data = new ParentResult();
        data.parent = fileItems;
        data.index = index;
        data.path = parent;


        return ApiResponse.returnOK().setDataNow(data);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("update")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse update(@Valid FileItem data){
        //JSONObject obj = JSON.parseObject(data);
        //FileItem item = JSON.parseObject(data, FileItem.class);
        //FileItem.getFormPath(item.path).updateData(item);

        if(data==null){
            return ApiResponse.returnFail("参数异常");
        }

        FileItem.getFormPath(data.path).updateData(data);

        return ApiResponse.returnOK();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("refresh")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse refresh(@Valid Map<String, String> json){
//        JSONObject obj = JSON.parseObject(data);
////        FileItem item = JSON.parseObject(data, FileItem.class);
//        String path = obj.getString("path");

        String path = json.get("path");
        if(path==null || path.isEmpty()){
            return ApiResponse.returnFail("参数异常");
        }

        FileItem fileItem = FileItem.getFormPath(path);

        return ApiResponse.returnOK().setDataNow(fileItem);
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    @Path("img")
    //@RolesAllowed({ "User", "Admin" })
    @PermitAll
    public Response openImage(@QueryParam("img") String path){// , @QueryParam("token") String token
        path = URLDecoder.decode(path, Charset.forName("UTF-8"));
        if(path==null){
            log.warn("路径不存在：{}", path);
            return Response.status(Response.Status.NOT_FOUND).entity("no path").build();
        }

        log.debug("访问路径: {}, ", path);

        //TODO 防止路径溢出
        if(!path.startsWith(getPath())){
            log.warn("路径溢出，试图访问：{}", path);
            return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
        }

        // 构建图片文件的完整路径
        java.nio.file.Path imagePath = Paths.get(path);
        File imageFile = imagePath.toFile();

        log.debug("file: exists={}, isFile={}, canRead={}, canWrite={}",
                imageFile.exists(), imageFile.isFile(),
                imageFile.canRead(), imageFile.canWrite()
        );

        if(!imageFile.canRead()){
            log.warn("权限异常: [canRead: {}] {}", imageFile.canRead(), path);
            //return Response.status(Response.Status.FORBIDDEN).entity("权限不足").build();
        }
        if (!imageFile.exists() || !imageFile.isFile()) {
            log.warn("路径异常：{}, exists: {}, isFile: {}", path, imageFile.exists(), imageFile.isFile());
            return Response.status(Response.Status.NOT_FOUND).entity("Image not found: " + path).build();
        }

        try {
            // 根据文件扩展名确定 Content-Type
            String mimeType = Files.probeContentType(imagePath);
            if (mimeType == null) {
                // 如果无法确定 MIME 类型，默认使用 application/octet-stream
                mimeType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // 读取文件内容并作为 Response 返回
            return Response.ok(imageFile, mimeType)
                    .header("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"") // 可选：添加下载头
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            log.error("未知异常: {}, {}", path, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading image: " + e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("del")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse delPath(@Valid Map<String, String> json){
//        JSONObject obj = JSON.parseObject(data);
////        FileItem item = JSON.parseObject(data, FileItem.class);
//        String path = obj.getString("path");

        String path = json.get("path");

        // 防止路径溢出
        if(!path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }

        java.nio.file.Path p = Paths.get(path);
        File pFile = p.toFile();
        if(!pFile.exists()){
            return ApiResponse.returnFail("文件不存在");
        }
        boolean isDelete = pFile.delete();
        if (isDelete){
            return ApiResponse.returnOK();
        }else{
            try {
                Files.delete(p);
                return ApiResponse.returnOK();
            } catch (IOException e) {
                //throw new RuntimeException(e);
//                e.printStackTrace();
                log.warn("java.nio.file.Files 删除失败: {}", e.getMessage());
            }

            try {
                FileUtils.delete(pFile);
                return ApiResponse.returnOK();
            } catch (IOException e) {
//                e.printStackTrace();
                log.warn("org.apache.commons.io.FileUtils.delete 删除失败: {}", e.getMessage());
            }

            try {
                FileUtils.deleteDirectory(pFile);
                return ApiResponse.returnOK();
            } catch (IOException e) {
//                e.printStackTrace();
                log.warn("org.apache.commons.io.FileUtils.deleteDirectory 删除失败: {}", e.getMessage());
            }

            try {
                FileUtils.forceDelete(pFile);
                return ApiResponse.returnOK();
            } catch (IOException e) {
//                e.printStackTrace();
                log.warn("org.apache.commons.io.FileUtils.forceDelete 删除失败: {}", e.getMessage());
            }

            log.error("路径删除失败! {}, canRead={}, canWrite={}, canExecute={}",
                    path, pFile.canRead(), pFile.canWrite(), pFile.canExecute()
            );
            pFile.deleteOnExit();
            String osName = System.getProperty("os.name").toLowerCase();
            if(!osName.contains("win")){
                DelFileFormRM rm = new DelFileFormRM(path);
                rm.startRun();
                log.info("调用rm强制删除");
            }
            return ApiResponse.returnFail("删除失败!");
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("favorites/list")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse listFavorites(){
        List<PanacheEntityBase> result = Favorite.listAll();
        return ApiResponse.returnOK().setDataNow(result);
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("favorites/add")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse addFavorites(@Valid Map<String, String> json){
        String path = json.get("path");

        // 防止路径溢出
        if(!path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }

        FileItem fileItem = FileItem.getFormPath(path);
        if(!fileItem.path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }

        PanacheQuery<Favorite> result = Favorite.find("path", fileItem.path);
        if(result.count()>0){
            return ApiResponse.returnFail("路径已存在");
        }
        Favorite favorite = new Favorite(fileItem.path, fileItem.name);
        try {
            favorite.persistAndFlush();
            return ApiResponse.returnOK();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.returnFail("创建失败");
        }
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("favorites/del")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse delFavorites(@Valid Map<String, String> json){
        String path = json.get("path");

        // 防止路径溢出
        if(!path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }

        FileItem fileItem = FileItem.getFormPath(path);
        if(!fileItem.path.startsWith(getPath())){
            //return Response.status(Response.Status.FORBIDDEN).entity("不允许访问").build();
            return ApiResponse.returnFail("不允许访问", 403);
        }

        PanacheQuery<Favorite> result = Favorite.find("path", fileItem.path);
        if(result.count()==0){
            return ApiResponse.returnFail("路径不存在");
        }
        long delResult = Favorite.delete("path", fileItem.path);
        if(delResult==0){
            return ApiResponse.returnFail("删除失败");
        }
        return ApiResponse.returnOK();
    }
}
