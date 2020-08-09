package com.geekq.miaosha.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.geekq.miaosha.common.resultbean.ResultGeekQ;
import com.geekq.miaosha.service.MiaoShaUserService;
import com.geekq.miaosha.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private MiaoShaUserService userService;

    @Reference
    @RequestMapping("/to_login")
    public String tologin(LoginVo loginVo, Model model) {
        log.info(loginVo.toString());
        //未完成
//          RedisLua.vistorCount(COUNTLOGIN);
//        String count = RedisLua.getVistorCount(COUNTLOGIN).toString();
//        log.info("访问网站的次数为:{}",count);
//        model.addAttribute("count",count);
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public ResultGeekQ<Boolean> dologin(HttpServletResponse response, @Valid LoginVo loginVo) {
        ResultGeekQ<Boolean> result = ResultGeekQ.build();
        log.info(loginVo.toString());
        userService.login(response, loginVo);
        return result;
    }


    @RequestMapping("/create_token")
    @ResponseBody
    public String createToken(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        String token = userService.createToken(response, loginVo);
        return token;
    }
}
