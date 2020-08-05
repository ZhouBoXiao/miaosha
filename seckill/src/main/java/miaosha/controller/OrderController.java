package miaosha.controller;

import miaosha.common.resultbean.ResultGeekQ;
import miaosha.domain.MiaoshaUser;
import miaosha.domain.OrderInfo;
import miaosha.redis.RedisService;
import miaosha.service.GoodsService;
import miaosha.service.MiaoShaUserService;
import miaosha.service.OrderService;
import miaosha.vo.GoodsVo;
import miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static miaosha.common.enums.ResultStatus.ORDER_NOT_EXIST;
import static miaosha.common.enums.ResultStatus.SESSION_ERROR;


@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	MiaoShaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	OrderService orderService;

	@Autowired
	GoodsService goodsService;

	@RequestMapping("/detail")
	@ResponseBody
	public ResultGeekQ<OrderDetailVo> info(Model model, MiaoshaUser user,
										   @RequestParam("orderId") long orderId) {
		ResultGeekQ<OrderDetailVo> result = ResultGeekQ.build();
		if (user == null) {
			result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
			return result;
		}
		OrderInfo order = orderService.getOrderById(orderId);
		if (order == null) {
			result.withError(ORDER_NOT_EXIST.getCode(), ORDER_NOT_EXIST.getMessage());
			return result;
		}
    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);
    	result.setData(vo);
    	return result;
    }
    
}
