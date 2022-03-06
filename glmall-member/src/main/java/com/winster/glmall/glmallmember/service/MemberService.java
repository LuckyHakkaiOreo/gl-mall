package com.winster.glmall.glmallmember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.to.GithubUserTo;
import com.winster.common.to.UserLoginTo;
import com.winster.common.to.UserRegisterTo;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallmember.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:34:31
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterTo to);

    void checkPhoneUnique(String phone);

    void checkUserNameUnique(String userName);

    MemberEntity login(UserLoginTo to);

    MemberEntity githubLogin(GithubUserTo to);
}

