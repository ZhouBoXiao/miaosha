package com.geekq.miaosha.MQ;


import com.geekq.miaosha.domain.MiaoshaMessage;
import com.geekq.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Service
public class MQSender {

	private static Logger log = LoggerFactory.getLogger(MQSender.class);

//	@Autowired
//	AmqpTemplate amqpTemplate ;
//
//	@Autowired
//	private RabbitTemplate rabbitTemplate;


	@Resource
	private KafkaTemplate<Object, Object> kafkaTemplate;

	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msg = RedisService.beanToString(mm);
		log.info("send message:" + msg);
		kafkaTemplate.send(MQConfig.MIAOSHA_QUEUE, String.valueOf(mm.getUser().getId()), msg);

	}

	public SendResult syncSend(MiaoshaMessage mm) throws ExecutionException, InterruptedException {
		// 同步发送消息
		return kafkaTemplate.send(MQConfig.MIAOSHA_QUEUE, String.valueOf(mm.getUser().getId()), mm).get();
	}

	public ListenableFuture<SendResult<Object, Object>> asyncSend(MiaoshaMessage mm) {
		// 异步发送消息
		return kafkaTemplate.send(MQConfig.MIAOSHA_QUEUE, String.valueOf(mm.getUser().getId()), mm);
	}


//
//	/**
//	 * 站内信
//	 * @param mm
//	 */
//	public void sendMessage(MiaoshaMessage mm) {
////		String msg = RedisService.beanToString(mm);
//		log.info("send message:"+"11111");
//		rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC,"miaosha_*", "111111111");
//	}
//
//    /**
//     * 站内信
//     * @param
//     */
//    public void sendRegisterMessage(MiaoShaMessageVo miaoShaMessageVo) {
//		String msg = RedisService.beanToString(miaoShaMessageVo);
//        log.info("send message:{}" , msg);
//		rabbitTemplate.convertAndSend(MQConfig.MIAOSHATEST,msg);
////        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC,"miaosha_*", msg);
//    }
}
