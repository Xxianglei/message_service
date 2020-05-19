package com.xianglei.message_service.service.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xianglei.message_service.common.JwtUtils;
import com.xianglei.message_service.common.Tools;
import com.xianglei.message_service.common.utils.RedisUtil;
import com.xianglei.message_service.domain.BsMessage;
import com.xianglei.message_service.domain.BsOrder;
import com.xianglei.message_service.mapper.MessageMapper;
import com.xianglei.message_service.mapper.OrderMapper;
import com.xianglei.message_service.service.UserMangerService;
import com.xianglei.message_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class ScheduledJob {
    private static Logger logger = LoggerFactory.getLogger(ScheduledJob.class);
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserMangerService userMangerService;
    @Autowired
    UserService userService;

    /**
     * 定时任务方法
     * 每1分钟清理message表
     * LIFO
     *
     * @Scheduled:设置定时任务 cron属性：cron表达式。定时任务触发是时间的一个字符串表达形式
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void scheduledMessageMethod() throws Exception {
        logger.info("Scheduled定时清理过期消息，时间：{}", new Date());
        List<BsMessage> bsMessages = messageMapper.selectList(new QueryWrapper<BsMessage>().orderByAsc("CREATE_DATE"));
        if (bsMessages != null && bsMessages.size() > 0) {
            BsMessage bsMessage = bsMessages.get(0);
            messageMapper.deleteById(bsMessage.getFlowId());
        }
    }

    /**
     * 定时任务方法
     * 每30分钟设置订单过期处理过期订单
     * LIFO
     *
     * @Scheduled:设置定时任务 cron属性：cron表达式。定时任务触发是时间的一个字符串表达形式
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduledOrderMethod() throws Exception {
        logger.info("Scheduled定时更新订单状态，时间：{}", new Date());
        Date now = new Date();
        List<BsOrder> charge = orderMapper.selectList(new QueryWrapper<BsOrder>().eq("CHARGE", "0"));
        for (BsOrder bsOrder : charge) {
            if(!bsOrder.getFlowId().endsWith("TEMP")){
                Date createTime = bsOrder.getCreateTime();
                long nowTime = now.getTime();
                long createTimeTime = createTime.getTime();
                //转换到分钟级别
                long nowTimeMill = nowTime / 1000 / 60;
                long createTimeTimeMill = createTimeTime / 1000 / 60;
                // 相差间隔三十分钟
                if (nowTimeMill - createTimeTimeMill >= 30) {
                    bsOrder.setCharge("2");
                    orderMapper.updateById(bsOrder);
                }
            }
        }
    }

    /**
     * 定时任务方法
     * 没三十分钟清理一次token失效或redis不存在的用户
     *
     * @Scheduled:设置定时任务 cron属性：cron表达式。定时任务触发是时间的一个字符串表达形式
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void scheduledMethod() throws Exception {
        logger.info("Scheduled定时清理非法在线用户触发，时间：{}", new Date());
        List<String> allOnlineUsers = userMangerService.findAllUserNoPrama();
        // 获取在线用户
        Set<String> keys = redisUtil.getKeys();
        if (Tools.isEmpty(keys) && !allOnlineUsers.isEmpty()) {
            for (String flowId : allOnlineUsers) {
                logger.info("Redis缓存清空同步任务\n" +
                        "强制下线---->时间：{}", new Date());
                userService.logout(flowId);
            }
        } else {
            for (String OnlineflowId : allOnlineUsers) {
                boolean tag = false;
                for (String key : keys) {
                    if (JwtUtils.verify(key)) {
                        String flowId = JwtUtils.getFlowId(key);
                        if (OnlineflowId.equals(flowId)) {
                            tag = true;
                            break;
                        }
                    }
                }
                if (!tag) {
                    // 数据库中用户在线，但是token已经没有了，失效了，则强制下线
                    logger.info("部分用户：{}token失效\n" +
                            "强制下线---->时间：{}", OnlineflowId, new Date());
                    userService.logout(OnlineflowId);
                }
            }
        }
    }
}
