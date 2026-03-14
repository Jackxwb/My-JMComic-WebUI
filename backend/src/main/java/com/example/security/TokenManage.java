package com.example.security;

import com.example.entity.ApiResponse;
import com.example.entity.User;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@ApplicationScoped
public class TokenManage {

    @ConfigProperty(name = "setting.jwt.expiration", defaultValue = "86400")
    private long ExpirationDate;

    //private static final Set<String> onlineToken = new CopyOnWriteArraySet<>();
    private static final Set<String> onlineToken = ConcurrentHashMap.newKeySet();

    public static void addToken(String token){
        onlineToken.add(token);
    }

    public static boolean check(JsonWebToken jwt, String token){
        if(jwt==null){
            return false;
        }
        if(token==null || token.isEmpty() || "null".equalsIgnoreCase(token) || "Bearer ".equalsIgnoreCase(token) ){
            return false;
        }else{
            if(token.startsWith("Bearer")){
                token = token.substring(7);
            }
        }
        if( onlineToken.contains(token) ){
            //是否过期
            if( jwt.getExpirationTime() > (System.currentTimeMillis() / 1000) ){
                return true;
            }
        }
        return false;
    }

    public String createTokenStr(String username, String rules){
        String token = null;
        try {
            //String rules = result.getRules();
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
                    .claim("username", username)
                    .claim("rules", rules)
                    // 过期时间
                    .expiresAt(Instant.now().plusSeconds(ExpirationDate))
                    .sign();

            //TokenManage.addToken(token);
            return token;
        } catch (JwtSignatureException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
            return null;
        }
    }

    public String updateToken(JsonWebToken jwt){
        long remainingTime = jwt.getExpirationTime() - (System.currentTimeMillis() / 1000);
        if(remainingTime < 0){
            return null;
        }
        if(remainingTime < (ExpirationDate/2)){
            String username = jwt.getClaim("username");
            String rules = jwt.getClaim("rules");
            String newToken = createTokenStr(username, rules);
            if(newToken!=null){
                return newToken;
            }
        }
        return null;
    }

    public static User contextGetUser(JsonWebToken jwt){
        String username = jwt.getClaim("username");
        if(username==null || username.isEmpty()){
            return null;
        }
        return User.find("name", username).firstResult();
    }
}
