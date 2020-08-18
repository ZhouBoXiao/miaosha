package com.geekq.miaosha.kafka;


import com.geekq.miaosha.MQ.MQConfig;
import com.geekq.miaosha.domain.MiaoshaMessage;
import com.geekq.miaosha.domain.MiaoshaOrder;
import com.geekq.miaosha.domain.MiaoshaUser;
import com.geekq.miaosha.domain.OrderInfo;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.GoodsService;
import com.geekq.miaosha.service.MiaoShaMessageService;
import com.geekq.miaosha.service.MiaoshaService;
import com.geekq.miaosha.service.OrderService;
import com.geekq.miaosha.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class KafkaConsumer {

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MiaoShaMessageService messageService;


	@KafkaListener(topics = "test")
	public void processMessage(String content) {
		log.info("收到消息, topic:test, msg:{}", content);
	}


    /**
     * 监听seckill主题,有消息就读取
     *
     * @param miaoshaMessage
     */
    @KafkaListener(topics = MQConfig.MIAOSHA_QUEUE,
            groupId = "consumer-group-" + MQConfig.MIAOSHA_QUEUE,
            concurrency = "2")
    public void receiveMessage(MiaoshaMessage miaoshaMessage) {
        log.info("receive message:" + miaoshaMessage);

        MiaoshaUser user = miaoshaMessage.getUser();
        long goodsId = miaoshaMessage.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo miaosha = miaoshaService.miaosha(user, goods);
        if (miaosha == null) {

        }
        //收到通道的消息之后执行秒杀操作
//    	String[] array = message.split(";");
//    	if(redisUtil.getValue(array[0])==null){//control层已经判断了，其实这里不需要再判断了，这个接口有限流 注意一下
//    		Result result = seckillService.startSeckil(Long.parseLong(array[0]), Long.parseLong(array[1]));
//    		//可以注释掉上面的使用这个测试
//    	    //Result result = seckillService.startSeckilDBPCC_TWO(Long.parseLong(array[0]), Long.parseLong(array[1]));
//    		if(result.equals(Result.ok())){
//    			WebSocketServer.sendInfo(array[0].toString(), "秒杀成功");//推送给前台
//    		}else{
//    			WebSocketServer.sendInfo(array[0].toString(), "秒杀失败");//推送给前台
//    			redisUtil.cacheValue(array[0], "ok");//秒杀结束
//    		}
//    	}else{
//    		WebSocketServer.sendInfo(array[0].toString(), "秒杀失败");//推送给前台
//    	}
    }


}