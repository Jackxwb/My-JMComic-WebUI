package com.example.security;

import com.example.entity.ApiResponse;
import com.example.entity.User;
import com.example.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Slf4j
@Path("/secure/")
@PermitAll
@RunOnVirtualThread
public class SecureController {

    @Inject
    RoutingContext context;
    @Inject
    SecureService secureService;
    @Inject
    TokenManage tokenManage;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "setting.jwt.expiration", defaultValue = "86400")
    private long ExpirationDate;
    @ConfigProperty(name = "smallrye.jwt.sign.key", defaultValue = "")
    private String jwtSecret;
    //private SecretKey secret;
    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "")
    private String publicPemFilePath;
    private PublicKey publicPem;

    @POST
    //@Transactional
    @Consumes(MediaType.APPLICATION_JSON)//接受JSON数据
    @Produces(MediaType.APPLICATION_JSON)//返回JSON数据
    @Path("login")
    @PermitAll
    public ApiResponse login(@Valid Map<String, String> data){
//        JSONObject obj = JSON.parseObject(data);
////        FileItem item = JSON.parseObject(data, FileItem.class);
//        String username = obj.getString("username");
//        String password = obj.getString("password");

        String username = data.get("username");
        String password = data.get("password");

        if(username==null || username.isEmpty()){
            return ApiResponse.returnFail("用户名异常");
        }
        if(password == null || password.length() < 8){
            return ApiResponse.returnFail("密码异常");
        }

        //TODO 登录
        User result = User.find("name", username).firstResult();
        if(result==null){
            return ApiResponse.returnFail("用户名不存在");
        }
        if(!result.verifyPassword(password)){
            return ApiResponse.returnFail("密码错误");
        }


        String token = null;
        try {
            String rules = result.getRules();
            if(rules==null){
                rules="";
            }
            token = Jwt
//                    .issuer("https://quarkus.io/issuer")
                    .issuer("http://MyJmcomicManage.jackxwb.top")
                    .upn(username)
                    //.groups(new HashSet<>(Arrays.asList("User", "Admin")))
                    .groups(new HashSet<>(Arrays.asList(rules.split(","))))
                    .claim("birthdate", Instant.now().getEpochSecond())
                    .claim("username", result.getName())
                    .claim("rules", rules)
                    // 过期时间
                    .expiresAt(Instant.now().plusSeconds(ExpirationDate))
            .sign();

            TokenManage.addToken(token);
        } catch (JwtSignatureException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
            return ApiResponse.returnFail("Token 生成失败!");
        }

//        JSONObject t = new JSONObject();
//        t.put("token", token);
        Map<String, String> t = new HashMap<>();
        t.put("token", token);
        return ApiResponse.returnOK().setDataNow(t);
    }

    @POST
    //@Transactional//不修改数据库不要开启事务
    @Produces(MediaType.APPLICATION_JSON)
    @Path("check")
    @PermitAll
    public ApiResponse check(@Valid Map<String, String> data, @HeaderParam("Authorization") String authHeader){
        String token = data.get("token");
        if(token==null || token.isEmpty()){
            return ApiResponse.returnFail("未登录");
        }

        if(publicPem==null){
            synchronized(this) { // 或者更细粒度的锁
                if(publicPem==null){
                    if(publicPemFilePath==null){
                        return ApiResponse.returnFail("配置异常，无法读取公钥!");
                    }
                    try {
                        initKey();
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        //throw new RuntimeException(e);
                        return ApiResponse.returnFail("密钥初始化失败: "+ e.getMessage());
                    }
                }
            }
        }
        if( TokenManage.check(jwt, token) ){
            String updateToken = tokenManage.updateToken(jwt);
            if(updateToken!=null){
                return ApiResponse.returnOK().setDataNow(updateToken);
            }
            return ApiResponse.returnOK();
        }
        return ApiResponse.returnFail("Token 无效");
    }

    private void initKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key_sourse = FileUtil.readFile(new File(publicPemFilePath), 0L, "UTF-8");
        key_sourse = key_sourse
                .replaceAll("-----BEGIN PUBLIC KEY-----","")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "")
        ;
        byte[] bytes = Base64.getDecoder().decode(key_sourse);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        this.publicPem = KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Path("reset")
//    @PermitAll
    @RolesAllowed({ "User", "Admin" })
    public ApiResponse reSetpassword(@Valid Map<String, String> json){
//        JSONObject obj = JSON.parseObject(data);
//        //String username = obj.getString("username");
//        String password = obj.getString("password");
//        //String password2 = obj.getString("password2");

        String password = json.get("password");

        if(password==null || password.isEmpty() || password.length() < 8){
            return ApiResponse.returnFail("密码异常！");
        }

        //User user = TokenManage.contextGetUser(context);
        User user = TokenManage.contextGetUser(jwt);
        if(user == null){
            return ApiResponse.returnFail("权限异常", 403);
        }
//        User user = (User) loginResult.getData();
        user.updatePass(password);
        user.persist();

        return ApiResponse.returnOK();
    }
}
