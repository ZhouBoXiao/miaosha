package com.geekq.miaosha.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.MQ.MQSender;
import com.geekq.miaosha.access.AccessLimit;
import com.geekq.miaosha.annotation.TimeCounter;
import com.geekq.miaosha.common.enums.ResultStatus;
import com.geekq.miaosha.common.resultbean.ResultGeekQ;
import com.geekq.miaosha.domain.MiaoshaMessage;
import com.geekq.miaosha.domain.MiaoshaOrder;
import com.geekq.miaosha.domain.MiaoshaUser;
import com.geekq.miaosha.redis.GoodsKey;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.GoodsService;
import com.geekq.miaosha.service.MiaoShaUserService;
import com.geekq.miaosha.service.MiaoshaService;
import com.geekq.miaosha.service.OrderService;
import com.geekq.miaosha.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    MiaoShaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender mqSender;

    private ConcurrentHashMap<Long, Boolean> localOverMap = new ConcurrentHashMap<Long, Boolean>();

    @RequestMapping(value="/hello" ,method = RequestMethod.POST)
    @ResponseBody
    public String hello(@RequestBody String id){

        return id + "";
    }


    @RequestMapping(value="/test" ,method = RequestMethod.POST , produces="application/json;charset=UTF-8")
    @ResponseBody
    public ResultGeekQ<Integer> test(@RequestBody String params) {

        ResultGeekQ<Integer> result = ResultGeekQ.build();
        JSONObject jsonObject = JSON.parseObject(params);
        long userId = jsonObject.getInteger("userId");
        long goodsId = jsonObject.getInteger("goodsId");
        //是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            result.withError(ResultStatus.REPEATE_MIAOSHA.getCode(), ResultStatus.REPEATE_MIAOSHA.getMessage());
            return result;
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMessage());
            return result;
        }
        //预见库存
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMessage());
            return result;
        }
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setGoodsId(goodsId);
        MiaoshaUser user = new MiaoshaUser();
        user.setId(userId);
        mm.setUser(user);
        mqSender.sendMiaoshaMessage(mm);
        return result;
    }


    /**
     * QPS:1306
     * 5000 * 10
     * get　post get 幂等　从服务端获取数据　不会产生影响　　post 对服务端产生变化
     */
    //@AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @SentinelResource(value = "miaosha", defaultFallback = "defaultFallback")
    @TimeCounter
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public ResultGeekQ<Integer> miaosha(Model model, long userId, @PathVariable("path") String path,
                                        @RequestParam("goodsId") long goodsId) {
        ResultGeekQ<Integer> result = ResultGeekQ.build();

//        if (user == null) {
//            result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
//            return result;
//        }
        //验证path
        /*boolean check = miaoshaService.checkPath(user, goodsId, path);
        if (!check) {
            result.withError(REQUEST_ILLEGAL.getCode(), REQUEST_ILLEGAL.getMessage());
            return result;
        }*/
//		//使用RateLimiter 限流
//		RateLimiter rateLimiter = RateLimiter.create(10);
//		//判断能否在1秒内得到令牌，如果不能则立即返回false，不会阻塞程序
//		if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
//			System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
//			return ResultGeekQ.error(CodeMsg.MIAOSHA_FAIL);
//
//		}
        //是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            result.withError(ResultStatus.REPEATE_MIAOSHA.getCode(), ResultStatus.REPEATE_MIAOSHA.getMessage());
            return result;
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMessage());
            return result;
        }
        //预见库存
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMessage());
            return result;
        }
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setGoodsId(goodsId);
        MiaoshaUser user = new MiaoshaUser();
        user.setId(userId);
        mm.setUser(user);
        mqSender.sendMiaoshaMessage(mm);
        return result;
    }


    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<Long> miaoshaResult(Model model, MiaoshaUser user,
                                           @RequestParam("goodsId") long goodsId) {
        ResultGeekQ<Long> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMessage());
            return result;
        }
        model.addAttribute("user", user);
        Long miaoshaResult = miaoshaService.getMiaoshaResult(Long.valueOf(user.getNickname()), goodsId);
        result.setData(miaoshaResult);
        return result;
    }

    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                              @RequestParam("goodsId") long goodsId,
                                              @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode
    ) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMessage());
            return result;
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            result.withError(ResultStatus.REQUEST_ILLEGAL.getCode(), ResultStatus.REQUEST_ILLEGAL.getMessage());
            return result;
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        result.setData(path);
        return result;
    }

    @RequestMapping(value = "/verifyCodeRegister", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaVerifyCod(HttpServletResponse response
    ) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        try {
            BufferedImage image = miaoshaService.createVerifyCodeRegister();
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return result;
        } catch (Exception e) {
            log.error("生成验证码错误-----注册:{}", e);
            result.withError(ResultStatus.MIAOSHA_FAIL.getCode(), ResultStatus.MIAOSHA_FAIL.getMessage());
            return result;
        }
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                                   @RequestParam("goodsId") long goodsId) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMessage());
            return result;
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return result;
        } catch (Exception e) {
            log.error("生成验证码错误-----goodsId:{}", goodsId, e);
            result.withError(ResultStatus.MIAOSHA_FAIL.getCode(), ResultStatus.MIAOSHA_FAIL.getMessage());
            return result;
        }
    }

    /**
     * 系统初始化
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    public String defaultFallback() {
        log.info("Go to default fallback");
        return "default_fallback";
    }
}
