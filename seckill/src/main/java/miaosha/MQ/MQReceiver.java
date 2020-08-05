package miaosha.MQ;

import miaosha.redis.RedisService;
import miaosha.service.GoodsService;
import miaosha.service.MiaoShaMessageService;
import miaosha.service.MiaoshaService;
import miaosha.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Service
public class MQReceiver {

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
		
/*		@RabbitListener(queues= MQConfig.MIAOSHA_QUEUE)
		public void receive(String message) {
			log.info("receive message:"+message);
			MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
			MiaoshaUser user = mm.getUser();
			long goodsId = mm.getGoodsId();

			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	    	int stock = goods.getStockCount();
	    	if(stock <= 0) {
	    		return;
	    	}
	    	//判断是否已经秒杀到了
	    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(Long.valueOf(user.getNickname()), goodsId);
	    	if(order != null) {
	    		return;
	    	}
	    	//减库存 下订单 写入秒杀订单
	    	miaoshaService.miaosha(user, goods);
		}



	@RabbitListener(queues= MQConfig.MIAOSHATEST)
	public void receiveMiaoShaMessage(Message message, Channel channel) throws IOException {
		log.info("接受到的消息为:{}",message);
		String messRegister = new String(message.getBody(), "UTF-8");
		channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
		MiaoShaMessageVo msm  = RedisService.stringToBean(messRegister, MiaoShaMessageVo.class);
		messageService.insertMs(msm);
		}*/
}
