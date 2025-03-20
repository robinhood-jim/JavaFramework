package com.robin.basis.controller.main;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class CaptchaController {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private Producer producer;
    @GetMapping("/captchaImage")
    public Map<String,Object> getKaptcha(HttpServletResponse response){
        Map<String,Object> retMap = new HashMap<>();
        String imagecode = producer.createText();
        // 生成图片
        BufferedImage image = producer.createImage(imagecode);

        //将图片输出给浏览器
        String uuid = IdUtil.randomUUID();
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            response.setContentType("image/png");

            ImageIO.write(image,"png",os);

            //验证码实现redis缓存，过期时间2分钟
            redisTemplate.opsForValue().set(uuid,imagecode,2, TimeUnit.MINUTES);

        } catch (IOException e) {
            retMap.put("code",500);
            retMap.put("msg",e.getMessage());
        }
        retMap.put("uuid",uuid);
        retMap.put("img", Base64.encode(os.toByteArray()));
        return retMap;
    }
}
