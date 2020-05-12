package com.xianglei.message_service.service;

/**
 * @Auther: Xianglei
 * @Company: xxx
 * @Date: 2020/4/27 21:59
 * com.example.message_service.service
 * @Description:校验验证码是否正确
 */
public interface MessageService {

    String insertMessage(String message);

    boolean checkMessage(String message);

    /**
     * 删除验证码
     * @param code
     * @return
     */
    int deleteMessageCode(String code);
}
