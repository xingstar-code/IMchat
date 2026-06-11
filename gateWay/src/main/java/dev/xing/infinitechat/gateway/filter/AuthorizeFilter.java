package dev.xing.infinitechat.gateway.filter;

import dev.xing.infinitechat.gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Slf4j
@Component
@SuppressWarnings({"all"})
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求参数
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        try {
            if (request.getHeaders().get("Authorization") == null) {
                // 没有token 拦截
                if (path.matches("/api/v1/user/noToken/.*")) {
                    return chain.filter(exchange);
                }
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();

            } else {
                String token = request.getHeaders().get("Authorization").get(0);
                // 判断是否合法
                Claims claims = JwtUtil.parse(token);
                String userId = claims.getSubject();
                String redisToken = redisTemplate.opsForValue().get(userId);
                if (token.equals(redisToken)) {
                    log.info("token合法");
                    return chain.filter(exchange);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        // 否 拦截
        log.info("token不合法");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
