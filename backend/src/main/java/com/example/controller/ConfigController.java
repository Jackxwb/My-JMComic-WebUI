package com.example.controller;

import com.example.entity.ApiResponse;
import com.example.entity.JmcomicConfig;
import com.example.service.BackendBashTaskService;
import com.example.service.ConfigService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Path("/api/config")
public class ConfigController {

    @Inject
    BackendBashTaskService backTaskService;
    @Inject
    ConfigService configService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "User", "Admin" })
    @Path("update")
    @RunOnVirtualThread
    public ApiResponse update(){
        return backTaskService.updateJmcomic();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Admin"})
    @Path("save")
    public ApiResponse saveYaml(@Valid JmcomicConfig json){
        //JSONObject obj = JSON.parseObject(json);

        if(json==null){
            return ApiResponse.returnFail("参数异常");
        }
        return configService.saveConfig(json);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Admin"})
    @Path("load")
    public ApiResponse loadYaml(){
        return configService.loadConfig();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Admin"})
    @Path("testCommand")
    public ApiResponse testCommand(@Valid Map<String, String> json){
        String test = json.get("test");
        if(test==null || test.isEmpty()){
            return ApiResponse.returnFail("没有指令");
        }
        String charsetName = json.get("charset");

        test = test
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n")
        ;
        String[] commands = test.split("\n");

        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            // 将标准错误流重定向到标准输出流
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuffer buffer = new StringBuffer();
            AtomicReference<Exception> havErr = new AtomicReference<>();
            //Executors.newSingleThreadExecutor().submit(() -> {
            Thread.startVirtualThread(()->{
                BufferedReader out = null;
                if(charsetName!=null && !charsetName.isEmpty()){
                    try {
                        out = new BufferedReader(new InputStreamReader(process.getInputStream(), charsetName));
                    } catch (UnsupportedEncodingException e) {
                        //e.printStackTrace();
                        log.error("编码异常: {}", e.getMessage());
                        //return ApiResponse.returnFail("编码异常："+e.getMessage());
                        havErr.set(e);
                    }
                }else{
                    out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                }
                String line;
                try {
                    while ((line = out.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\r\n");
                    }
                } catch (IOException e) {
                    log.error("执行报错，读取日志异常：{}", e);
                }
            });
            process.waitFor();

            if(havErr.get()!=null){
                return ApiResponse.returnFail("出现异常: "+havErr.get().getMessage()).setDataNow(buffer.toString());
            }

            return ApiResponse.returnOK().setDataNow(buffer.toString());
        } catch (IOException | InterruptedException e) {
            //throw new RuntimeException(e);
            log.error("执行报错：{}", e);
            e.printStackTrace();
            return ApiResponse.returnFail("执行报错："+e.getMessage());
        }
    }
}
