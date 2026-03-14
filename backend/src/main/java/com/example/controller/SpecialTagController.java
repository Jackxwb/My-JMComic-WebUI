package com.example.controller;

import com.example.entity.ApiResponse;
import com.example.entity.FileItem;
import com.example.entity.SpecialTag;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/tag")
@RolesAllowed({ "User", "Admin" })
public class SpecialTagController {

    @Inject
    EntityManager entityManager;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    //@Transactional
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse listSpecialTag(){
        List<SpecialTag> result = SpecialTag.listAll();
        return ApiResponse.returnOK().setDataNow(result);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("add")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse addSpecialTag(@Valid SpecialTag tag){
        if(tag==null){
            return ApiResponse.returnFail("参数异常");
        }

        if(tag.id == null){
            entityManager.persist(tag); // 新实体，持久化
            return ApiResponse.returnOK().setMessageNow("创建成功");
        }else{
            entityManager.merge(tag); // 脱管状态，合并
            return ApiResponse.returnOK().setMessageNow("保存成功");
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("del")
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse delSpecialTag(@Valid Map<String, String> json){
        String id = json.get("id");
        if(id==null || id.isEmpty()){
            return ApiResponse.returnFail("路径为空");
        }

        PanacheQuery<SpecialTag> query = SpecialTag.find("id", id);
        if(query.count()==0){
            return ApiResponse.returnFail("Tag id不存在");
        }

        long delResult = SpecialTag.delete("id", id);
        if(delResult>0){
            return ApiResponse.returnOK();
        }
        return ApiResponse.returnFail("删除失败");
    }
}
