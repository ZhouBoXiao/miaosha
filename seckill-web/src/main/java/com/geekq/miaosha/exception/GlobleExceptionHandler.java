package com.geekq.miaosha.exception;

import com.geekq.miaosha.common.resultbean.ResultGeekQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.geekq.miaosha.common.enums.ResultStatus.SESSION_ERROR;
import static com.geekq.miaosha.common.enums.ResultStatus.SYSTEM_ERROR;

/**
 * 拦截异常
 * @author  qiurunze
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobleExceptionHandler {

    @ExceptionHandler(value=Exception.class)
    public ResultGeekQ<String> exceptionHandler(HttpServletRequest request , Exception e){
        e.printStackTrace();
        if(e instanceof GlobleException){
            GlobleException ex= (GlobleException)e;
            return ResultGeekQ.error(ex.getStatus());
        }else if( e instanceof BindException){
            BindException ex = (BindException) e  ;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            /**
             * 打印堆栈信息
             */
            log.error(String.format(msg, msg));
            return ResultGeekQ.error(SESSION_ERROR);
        }else {
            return ResultGeekQ.error(SYSTEM_ERROR);
        }
    }
}
