package com.winster.glmall.glmallcoupon.exception;

import com.winster.common.exception.BizCodeEnum;
import com.winster.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;

/*@ControllerAdvice
@ResponseBody*/
@Slf4j
@RestControllerAdvice
public class GlmallCouponExceptionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleParamsValidException(MethodArgumentNotValidException exception) {
        log.error("数据校验出现问题！", exception);
        BindingResult bindingResult = exception.getBindingResult();
        HashMap<String, String> map = new HashMap<>();
        if (bindingResult.hasErrors()) {
            List<FieldError> allErrors = bindingResult.getFieldErrors();
            allErrors.forEach(objectError -> {
                String code = objectError.getCode();
                map.put(objectError.getField(), objectError.getDefaultMessage());
            });
        }
        return R.error(BizCodeEnum.VALID_PARAMS_EXCEPTION.getCode(), BizCodeEnum.VALID_PARAMS_EXCEPTION.getMsg())
                .put("data", map);
    }

    @ExceptionHandler(Throwable.class)
    public R handleAllThrowable(Throwable t) {
        log.error("系统出现未知异常！", t);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
