package miaosha.kafka;

//import com.itstyle.seckill.common.entity.Result;
//import com.itstyle.seckill.common.redis.RedisUtil;
//import com.itstyle.seckill.common.webSocket.WebSocketServer;
//import com.itstyle.seckill.service.ISeckillService;

import miaosha.MQ.MQConfig;
import miaosha.MQ.MQReceiver;
import miaosha.common.redis.RedisUtil;
import miaosha.domain.MiaoshaMessage;
import miaosha.domain.MiaoshaOrder;
import miaosha.domain.MiaoshaUser;
import miaosha.redis.RedisService;
import miaosha.service.GoodsService;
import miaosha.service.MiaoShaMessageService;
import miaosha.service.MiaoshaService;
import miaosha.service.OrderService;
import miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消费者 spring-kafka 2.0 + 依赖JDK8
 * @author 科帮网 By https://blog.52itstyle.com
 */
@Component
public class KafkaConsumer {
	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

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

	private static RedisUtil redisUtil = new RedisUtil();


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
        miaoshaService.miaosha(user, goods);
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