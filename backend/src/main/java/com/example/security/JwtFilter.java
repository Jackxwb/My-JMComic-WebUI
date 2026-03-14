package com.example.security;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.quarkus.vertx.web.RouteFilter;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.concurrent.Callable;

@ApplicationScoped
public class JwtFilter {

//    @Inject
//    private TokenManage tokenManage;

//    /**
//     * 自定义过滤器
//     * @param context
//     */
//    @RouteFilter(10000)
//    public void filter(RoutingContext context) {
//        String authHeader = context.request().getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring("Bearer ".length());
//            if(token.isEmpty() || "null".equals(token)){
//                // 移除无效的 Authorization 头
//                context.request().headers().remove(HttpHeaders.AUTHORIZATION);
//            }
//
////            else if(!tokenManage.check(token)){
////                context.response().setStatusCode(403);
////                context.fail(403);
////                return;
////            }
////            User user = TokenManage.contextGetUser(context);
//
//        }
//        context.next();
//    }

    public void registerMyFilter(@Observes Filters filters) {
        filters.register(rc -> {
            String authHeader = rc.request().getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // "Bearer ".length() == 7
                if (token.isBlank() || "null".equalsIgnoreCase(token)) {
                    // 只有在确定无效时才移除，确保不会干扰后续合法的 JWT 校验
                    rc.request().headers().remove(HttpHeaders.AUTHORIZATION);
                }
            }
            // 继续下一个处理器
            rc.next();
        }, 10000);
    }
}
