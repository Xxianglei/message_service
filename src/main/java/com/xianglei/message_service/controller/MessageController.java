package com.xianglei.message_service.controller;

import com.xianglei.message_service.common.BaseJson;
import com.xianglei.message_service.common.HttpUtils;
import com.xianglei.message_service.service.MessageService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Auther: Xianglei
 * @Company: xxx
 * @Date: 2020/4/21 14:13
 * com.example.demo
 * @Description:
 */
@RestController
public class MessageController {
    private Logger logger = LoggerFactory.getLogger(MessageController.class);
    @Autowired
    MessageService messageService;

    @GetMapping("/getMessage")
    public String getMessage(@RequestParam String phone) {
        String host = "https://zwp.market.alicloudapi.com";
        String path = "/sms/sendv2";
        String method = "GET";
        String appcode = "e5245cb4f806431185e56bb2efd5eaf9";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        int nextInt = new Random().nextInt(999999);
        String flowId = messageService.insertMessage(Integer.toString(nextInt));
        querys.put("content", "【云通知】您的验证码是" + nextInt + "。如非本人操作，请忽略本短信");
        querys.put("mobile", phone);
        try {
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flowId + "%" + nextInt;
    }

    @GetMapping("/checkMessage")
    public BaseJson checkMessage(@RequestParam String code, @RequestParam String flowId) {
        BaseJson baseJson = new BaseJson(false);
        try {
            boolean flag = messageService.checkMessage(code, flowId);
            if (flag) {
                // 删除验证码
                int result = messageService.deleteMessageCode(code);
                if(result!=0){
                    logger.error("校验通过，验证码删除失败");
                }
                baseJson.setMessage("校验通过");
                baseJson.setCode(200);
                baseJson.setStatus(true);
            } else {
                baseJson.setMessage("校验错误");
                baseJson.setCode(500);
            }
        } catch (Exception e) {
            baseJson.setMessage("校验错误");
            baseJson.setCode(500);
        }
        return baseJson;
    }
}
