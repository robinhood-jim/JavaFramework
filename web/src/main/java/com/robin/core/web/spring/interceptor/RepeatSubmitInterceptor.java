package com.robin.core.web.spring.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.robin.comm.util.ip.IpUtils;
import com.robin.core.web.util.HttpContextUtils;
import com.robin.core.web.spring.anotation.RepeatSubmitCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class RepeatSubmitInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    private static final String LASTACCESSTS = "__lastTs";
    private static final String FIRSTACCESSTS = "__firstTs";
    private static final String NUMBER = "__number";
    private Gson gson = new Gson();


    //禁止的ip缓存，指定时间段内，请求超限的ip添加
    private Cache<String, Integer> bannedIpCache;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private Environment environment;
    private int bannedHours = 1;

    public RepeatSubmitInterceptor() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (!ObjectUtils.isEmpty(environment) && environment.containsProperty("webrequest.bannIpfrequency")) {
            bannedHours = Integer.valueOf(environment.getProperty("webrequest.bannIpfrequency"));
        }
        bannedIpCache = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(1000)
                .expireAfterWrite(bannedHours, TimeUnit.HOURS).build();
        Assert.notNull(redisTemplate, "redistemplate required!");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean isRepeated = false;
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("success", false);
        String ipAddress = IpUtils.getClientIp(request);
        if (HandlerMethod.class.isAssignableFrom(handler.getClass())) {
            if (isIpBanned(ipAddress)) {
                log.error("ip {} was banned!deny access", ipAddress);
                retMap.put("message", "you ip was temporary banned for access this url too frequently!");
                HttpContextUtils.renderResponse(response, gson.toJson(retMap));
                return false;
            }
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmitCheck anotation = method.getAnnotation(RepeatSubmitCheck.class);
            if (anotation != null) {
                String url = request.getRequestURI();
                String httpMethod = request.getMethod();
                Long currentTs = System.currentTimeMillis();
                Map<String, Object> reqParam = HttpContextUtils.parseRequest(request);
                String key = url + "|" + ipAddress + "|" + httpMethod.toUpperCase();

                Map<String, Object> map = (Map) redisTemplate.opsForValue().get(key);
                isRepeated = isRepeated(map, anotation, reqParam, currentTs);
                if (!isRepeated) {
                    if (CollectionUtils.isEmpty(map)) {
                        reqParam.put(FIRSTACCESSTS, currentTs);
                        reqParam.put(LASTACCESSTS, currentTs);
                        if (anotation.allowAccessNumbers() > 0) {
                            reqParam.put(NUMBER, 1);
                        }
                    } else {
                        Long firstAccessTs = (Long) map.get(FIRSTACCESSTS);
                        if (anotation.allowAccessNumbers() > 0) {
                            Integer accessNumbers = 1;
                            if (currentTs - firstAccessTs > anotation.allowInterval()) {
                                firstAccessTs = currentTs;
                                if (anotation.allowAccessNumbers() > 0) {
                                    reqParam.put(NUMBER, 1);
                                }
                            } else {
                                accessNumbers = (Integer) map.get(NUMBER);
                                reqParam.put(NUMBER, accessNumbers + 1);
                            }
                        }
                        reqParam.put(FIRSTACCESSTS, firstAccessTs);
                        reqParam.put(LASTACCESSTS, currentTs);
                    }
                    redisTemplate.opsForValue().set(key, reqParam, anotation.allowInterval(), TimeUnit.MILLISECONDS);
                } else {
                    //请求次数超限且禁ip,添加进入禁止列表
                    if (anotation.allowAccessNumbers() > 0 && anotation.banIp()) {
                        log.error("ban ip {} for {} hour!", ipAddress, bannedHours);
                        bannedIpCache.put(ipAddress, 1);
                    }
                }
            }
        }
        if (isRepeated) {
            log.error("ip {} access too frequency, forbidden access!", ipAddress);
            retMap.put("message", "you access this url too frequently,please try again later!");
            HttpContextUtils.renderResponse(response, gson.toJson(retMap));
            return false;
        }
        return !isRepeated;
    }

    private boolean isRepeated(Map<String, Object> map, RepeatSubmitCheck check, Map<String, Object> paramMap, Long checkTs) {
        boolean repeatTag = false;
        if (!CollectionUtils.isEmpty(map)) {
            Stream<Map.Entry<String, Object>> stream = map.entrySet().stream().filter(f -> !LASTACCESSTS.equals(f.getKey()) && !FIRSTACCESSTS.equals(f.getKey()) && !NUMBER.equals(f.getKey()));
            if (check.checkParamValue()) {
                repeatTag = stream.allMatch(e -> e.getValue().equals(paramMap.get(e.getKey())));
            } else {
                repeatTag = stream.allMatch(e -> paramMap.containsKey(e.getKey()));
            }
            if (repeatTag) {
                if (check.allowAccessNumbers() > 0) {
                    repeatTag = compareNumber(map, checkTs, check.allowInterval(), check.allowAccessNumbers());
                } else {
                    repeatTag = compareTime(map, checkTs, check.allowInterval());
                }
            } else {
                repeatTag = false;
            }
        } else {
            repeatTag = map != null && CollectionUtils.isEmpty(map) && CollectionUtils.isEmpty(paramMap);
        }
        return repeatTag;
    }

    private boolean compareTime(Map<String, Object> originMap, Long currentTs, Long allowInterval) {
        Long originTs = (Long) originMap.get(LASTACCESSTS);
        return currentTs - originTs < allowInterval;
    }

    private boolean compareNumber(Map<String, Object> originMap, Long currentTs, Long allowInterval, int allowAccessNumbers) {
        Long firstAccessTs = (Long) originMap.get(FIRSTACCESSTS);
        Integer accessNumbers = (Integer) originMap.get(NUMBER);
        if (currentTs - firstAccessTs <= allowInterval) {
            return accessNumbers + 1 >= allowAccessNumbers;
        }
        return false;
    }

    private boolean isIpBanned(String ip) {
        return !ObjectUtils.isEmpty(bannedIpCache.getIfPresent(ip));
    }

    public Cache<String, Integer> getBannedIpCache() {
        return bannedIpCache;
    }
}
