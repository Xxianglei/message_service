package com.xianglei.message_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xianglei.message_service.domain.BsMessage;
import com.xianglei.message_service.mapper.MessageMapper;
import com.xianglei.message_service.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Auther: Xianglei
 * @Company: xxx
 * @Date: 2020/4/27 21:59
 * com.example.message_service.service.impl
 * @Description:
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;

    @Override
    public String insertMessage(String message) {
        BsMessage bsMessage = new BsMessage();
        String flowId = UUID.randomUUID().toString();
        bsMessage.setFlowId(flowId);
        bsMessage.setTxId(message);
        int insert = messageMapper.insert(bsMessage);
        if (insert != 0) {
            return flowId;
        } else {
            return null;
        }
    }

    @Override
    public boolean checkMessage(String message) {
        BsMessage bsMessage = messageMapper.selectOne(new QueryWrapper<BsMessage>().eq("TX_ID", message));
        return bsMessage == null ? false : true;
    }

    @Override
    public int deleteMessageCode(String code) {
        return messageMapper.delete(new QueryWrapper<BsMessage>().eq("TX_ID",code));
    }


}
