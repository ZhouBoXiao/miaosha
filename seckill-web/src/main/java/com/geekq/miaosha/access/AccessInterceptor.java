package com.geekq.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.geekq.miaosha.common.enums.ResultStatus;
import com.geekq.miaosha.common.resultbean.ResultGeekQ;
import com.geekq.miaosha.domain.MiaoshaUser;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.MiaoShaUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Slf4j
@Service
public class AccessInterceptor  extends HandlerInterceptorAdapter{

	@Autowired
	MiaoShaUserService userService;

	@Autowired
	RedisService redisService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/**
		 * 获取调用 获取主要方法
		 */
		if(handler instanceof HandlerMethod) {
			log.info("打印拦截方法handler ：{} ", handler);
			HandlerMethod hm = (HandlerMethod) handler;
			//方便mybatis 测试
//			if(hm.getMethod().getName().startsWith("test")){
//				return true;
//			}
			MiaoshaUser user = getUser(request, response);
			UserContext.setUser(user);
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if (accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if (needLogin) {
				if (user == null) {
					render(response, ResultStatus.SESSION_ERROR);
					return false;
				}
				key += "_" + user.getNickname();
			} else {
				//do nothing
			}
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer count = redisService.get(ak, key, Integer.class);
			if(count  == null) {
				redisService.set(ak, key, 1);
			}else if(count < maxCount) {
				redisService.incr(ak, key);
			}else {
				render(response, ResultStatus.ACCESS_LIMIT_REACHED);
				return false;
			}
		}
		return true;

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
		UserContext.removeUser();
	}

	private void render(HttpServletResponse response, ResultStatus cm)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String str  = JSON.toJSONString(ResultGeekQ.error(cm));
		out.write(str.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(MiaoShaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MiaoShaUserService.COOKIE_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}

	private String getCookieValue(HttpServletRequest request, String cookiName) {
		Cookie[]  cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookiName)) {
				return cookie.getValue();
			}
		}
		return null;
	}

}
