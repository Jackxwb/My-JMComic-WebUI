package com.example.controller;

import com.example.entity.ApiResponse;
import com.example.service.BackendBashTaskService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Path("/api/download")
@RunOnVirtualThread
public class DownloadController {

    @Inject
    BackendBashTaskService backTaskService;

    @POST
    @RolesAllowed({ "User", "Admin" })
    @Produces(MediaType.APPLICATION_JSON)
    //@Transactional
    public ApiResponse listDownload(){
        return ApiResponse.returnOK().setDataNow(backTaskService.getBackTask());
    }

    @POST
    @RolesAllowed({ "User", "Admin" })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("d")
    public ApiResponse downloadAlbum(@Valid Map<String, String> json){
        if(json==null){
            return ApiResponse.returnFail("参数异常");
        }
        String id = json.get("id");
        if(id==null || id.isEmpty()){
            return ApiResponse.returnFail("参数异常");
        }
        backTaskService.createDownloadTask(id);
        return ApiResponse.returnOK();
    }

    @POST
    @RolesAllowed({ "User", "Admin" })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("g")
    public ApiResponse getTask(@Valid Map<String, Long> json){
        if(json==null){
            return ApiResponse.returnFail("参数异常");
        }
        Long id = json.get("id");
        if(id==null){
            return ApiResponse.returnFail("参数异常");
        }
        //return ApiResponse.returnOK().setDataNow(backTaskService.getTask(id));
        return backTaskService.getTask(id);
    }

    @POST
    @RolesAllowed({ "User", "Admin" })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("del")
    public ApiResponse delDownloadAlbumTask(@Valid Map<String, Long> json){
        if(json==null){
            return ApiResponse.returnFail("参数异常");
        }
        Long id = json.get("id");
        if(id==null){
            return ApiResponse.returnFail("参数异常");
        }
        return ApiResponse.returnOK().setDataNow(backTaskService.delTask(id));
    }

    @POST
    @RolesAllowed({ "User", "Admin" })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("stop")
    public ApiResponse stopDownloadAlbumTask(@Valid Map<String, Long> json){
        if(json==null){
            return ApiResponse.returnFail("参数异常");
        }
        Long id = json.get("id");
        if(id==null){
            return ApiResponse.returnFail("参数异常");
        }
        return ApiResponse.returnOK().setDataNow(backTaskService.stopTask(id));
    }
}
