package com.winster.glmall.glmallauthserver.feign;

import com.winster.common.to.GithubUserTo;
import com.winster.common.to.UserLoginTo;
import com.winster.common.to.UserRegisterTo;
import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("glmall-member")
public interface MemberFeign {
    @PostMapping("/glmallmember/member/register")
    R register(@RequestBody UserRegisterTo to);

    @PostMapping("/glmallmember/member/login")
    R login(@RequestBody UserLoginTo to);

    @PostMapping("/glmallmember/member/github/login")
     R githubLogin(@RequestBody GithubUserTo to);
}
