package com.winster.glmall.glmallauthserver.web;

import com.winster.common.constant.AuthServerConstant;
import com.winster.common.exception.BizCodeEnum;
import com.winster.common.to.UserLoginTo;
import com.winster.common.to.UserRegisterTo;
import com.winster.common.utils.R;
import com.winster.glmall.glmallauthserver.feign.MemberFeign;
import com.winster.glmall.glmallauthserver.feign.ThirdPartyServerFeign;
import com.winster.glmall.glmallauthserver.vo.UserLoginVo;
import com.winster.glmall.glmallauthserver.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {

    /**
     * 如果我们的接口不做任何后台操作，仅仅是为了跳转
     * 到一个新的页面，我们可以使用springMVC viewController，
     * 将页面和请求映射起来。
     * 见代码：GlmallWebConfig.addViewControllers
     */

    @GetMapping({"/", "/login.html"})
    public String toLogin(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER_SESSION);
        if (attribute==null) {
            return "login";
        }
        return "redirect:http://glmall.com";
    }

    /*@GetMapping("/register.html")
    public String toRegister() {
        return "register";
    }*/

    @Resource
    private ThirdPartyServerFeign thirdPartyServerFeign;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MemberFeign memberFeign;

    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // 1.接口防刷：防止同一个手机号60s内重复发送
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(redisCode)) {
            String[] s = redisCode.split("_");
            Long time = Long.valueOf(s[1]);
            Long t = System.currentTimeMillis() - time;
            if (t < 60 * 1000) {
                return R.error(BizCodeEnum.AUTH_REPEAT_SEND_CODE_EXCEPTION.getCode(),
                        BizCodeEnum.AUTH_REPEAT_SEND_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        // 2.验证码的再次校验，存到redis
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        log.info("给手机：{}，发送的验证码：{}", phone, code);

        R r = thirdPartyServerFeign.sendCode(phone, code);

        return R.ok("发送短信成功");
    }

    /**
     * 转发：forward:/register.html 将post给了 /register.html 这个path
     * 转发会将源请求原封不动发给目标path，目标path不支持post所以由如下错误：
     * <p>
     * There was an unexpected error (type=Method Not Allowed, status=405).
     * Request method 'POST' not supported
     * org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'POST' not supported
     * <p>
     * todo 重定向携带数据，利用session原理。
     * todo 将元数据防止session中。
     * todo 只要跳转到下一个页面，就会将session中数据删除
     * todo 用了session就会存在分布式下session的问题
     * RedirectAttributes model:
     * 重定向携带数据
     *
     * @param vo
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes model) {
        Map<String, String> errors = new HashMap<>();
        // 1.如果前置校验出现问题
        if (result.hasErrors()) {
            errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            model.addFlashAttribute("errors", errors);
            return "redirect:http://auth.glmall.com/register.html";
        }

        // 2.校验验证码
        String code = vo.getCode();
        String phone = vo.getPhone();
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CACHE_PREFIX + phone);
        if (StringUtils.isBlank(redisCode)
                || !code.equals(redisCode.split("_")[0])) {
            errors.put("code", "验证码校验错误");
            model.addFlashAttribute("errors", errors);
            return "redirect:http://auth.glmall.com/register.html";
        }

        // 校验验证码通过，删除验证码
        stringRedisTemplate.delete(AuthServerConstant.SMS_CACHE_PREFIX + phone);

        // 3.真正注册，调用远程服务进行用户注册
        UserRegisterTo to = new UserRegisterTo();
        BeanUtils.copyProperties(vo, to);
        R registerR = memberFeign.register(to);
        if (!registerR.get("code").equals(0)) {
            errors.put("msg", (String) registerR.get("msg"));
            model.addFlashAttribute("errors", errors);
            return "redirect:http://auth.glmall.com/register.html";
        }

        return "redirect:http://auth.glmall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        // 远程登录
        UserLoginTo to = new UserLoginTo();
        BeanUtils.copyProperties(vo, to);
        R r = memberFeign.login(to);
        if (0 == (Integer) r.get("code")) {
            session.setAttribute(AuthServerConstant.LOGIN_USER_SESSION,
                    r.get("user"));
            return "redirect:http://glmall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) r.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.glmall.com/login.html";
        }

    }

}
