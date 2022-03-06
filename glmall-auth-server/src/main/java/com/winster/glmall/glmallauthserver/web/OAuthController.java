package com.winster.glmall.glmallauthserver.web;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.AuthServerConstant;
import com.winster.common.to.GithubUserTo;
import com.winster.common.utils.HttpUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallauthserver.feign.MemberFeign;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/oauth2.0")
public class OAuthController {

    @Resource
    private MemberFeign memberFeign;

    @GetMapping("/github/success")
    public String githubSuccess(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 1、根据code，去github换取accessToken
        Map<String, String> h1 = new HashMap<>();
        Map<String, String> params1 = new HashMap<>();
        params1.put("client_id", "fa30e3c2e0820d1ede2f");
        params1.put("client_secret", "8afa936e177f1f9e2310bca22dc24c6b490ca07c");
        params1.put("code", code);
        HttpResponse rsp1 = HttpUtils.doGet("https://github.com",
                "/login/oauth/access_token",
                "get",
                h1,
                params1);
        String token = "";
        if (rsp1.getStatusLine().getStatusCode() == 200) {
            // access_token=xxxoooo&scope=&token_type=bearer
            String s = EntityUtils.toString(rsp1.getEntity());
            String[] split = s.split("&");
            for (String s1 : split) {
                if (s1.contains("access_token")) {
                    token = s1.split("=")[1];
                }
            }
        } else {
            return "redirect:http://auth.glmall.com/login.html";
        }
        if (StringUtils.isBlank(token)) {
            return "redirect:http://auth.glmall.com/login.html";
        }
        // 2.获取用户数据

        Map<String, String> h2 = new HashMap<>();
        Map<String, String> params2 = new HashMap<>();
        h2.put("Authorization", "token " + token);
        params2.put("accept", "application/vnd.github.v3+");

        HttpResponse rsp2 = HttpUtils.doGet("https://api.github.com",
                "/user",
                "get",
                h2,
                params2);
        GithubUserTo githubUserTo = null;
        if (rsp2.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = rsp2.getEntity();
            String r = EntityUtils.toString(entity);
            githubUserTo = JSON.parseObject(r, GithubUserTo.class);

        } else {
            return "redirect:http://auth.glmall.com/login.html";
        }
        // 当前用户如果第一次进网站，自动注册进来（为社交账户创建一个绑定用户）
        // 注册或者登录用户
        R r = memberFeign.githubLogin(githubUserTo);
        if ((Integer) r.get("code") != 0) {
            return "redirect:http://auth.glmall.com/login.html";
        }

        session.setAttribute(AuthServerConstant.LOGIN_USER_SESSION, r.get("user"));
        return "redirect:http://glmall.com";
    }
}
