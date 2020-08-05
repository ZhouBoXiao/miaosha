package miaosha.config;

import miaosha.access.UserContext;
import miaosha.domain.MiaoshaUser;
import miaosha.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MiaoShaUserService userService;
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
      Class<?> clazz =    methodParameter.getParameterType() ;
      return clazz == MiaoshaUser.class ;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        /**
         *  threadlocal 存储线程副本 保证线程不冲突
         */
        return UserContext.getUser();
    }

}
