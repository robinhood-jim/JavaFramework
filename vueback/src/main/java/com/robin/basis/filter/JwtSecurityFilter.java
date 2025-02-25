package com.robin.basis.filter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.robin.basis.sercurity.SysLoginUser;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class JwtSecurityFilter extends OncePerRequestFilter {
    @Resource
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenStr =request.getHeader("Authorization");
        if(ObjectUtils.isEmpty(tokenStr)) {
            tokenStr=CookieUtils.getCookie(request, Const.TOKEN);
        }else if(tokenStr.startsWith("Bearer ")){
            tokenStr=tokenStr.substring(7,tokenStr.length());
        }
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String salt = environment.getProperty("jwt.salt");
        String loginUrl="login";
        if(environment.containsProperty("login.loginUrl")){
            loginUrl=environment.getProperty("login.loginUrl");
        }
        if(!ObjectUtils.isEmpty(tokenStr)) {
            JWT jwt = JWTUtil.parseToken(tokenStr).setKey(salt.getBytes());
            boolean validate = jwt.validate(0);
            JSONObject payloads = jwt.getPayloads();
            LocalDateTime expTs = payloads.getLocalDateTime(JWTPayload.EXPIRES_AT, LocalDateTimeUtil.of(1));
            if (!expTs.isAfter(LocalDateTime.now())) {
                request.getSession().removeAttribute(Const.SESSION);
                log.error("token expire");
                response.sendRedirect(loginUrl + "?redirect_url=" + request.getRequestURL());
            }
            if (validate) {
                SysLoginUser loginUser=payloads.toBean(SysLoginUser.class);
                UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        }
        filterChain.doFilter(request,response);
    }
}
