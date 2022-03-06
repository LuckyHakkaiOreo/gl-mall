package com.winster.glmall.glmallmember.controller;

import com.winster.common.exception.BizCodeEnum;
import com.winster.common.to.GithubUserTo;
import com.winster.common.to.UserLoginTo;
import com.winster.common.to.UserRegisterTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallmember.entity.MemberEntity;
import com.winster.glmall.glmallmember.exception.PhoneExistException;
import com.winster.glmall.glmallmember.exception.UserNameExistException;
import com.winster.glmall.glmallmember.feign.ProductFeign;
import com.winster.glmall.glmallmember.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:34:31
 */
@RestController
@RequestMapping("glmallmember/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Resource
    ProductFeign productFeign;

    @PostMapping("/github/login")
    public R githubLogin(@RequestBody GithubUserTo to) {
        MemberEntity entity = memberService.githubLogin(to);

        if (entity != null) {
            return R.ok("登录成功！").put("user", entity);
        } else {
            return R.error(BizCodeEnum.MEMBER_LOGIN_USERNAME_PASSWPRD_EXCEPTION);
        }

    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginTo to) {
        MemberEntity entity = memberService.login(to);

        if (entity != null) {
            return R.ok("登录成功！").put("user", entity);
        } else {
            return R.error(BizCodeEnum.MEMBER_LOGIN_USERNAME_PASSWPRD_EXCEPTION);
        }

    }

    @PostMapping("/register")
    public R register(@RequestBody UserRegisterTo to) {
        try {
            memberService.register(to);
        } catch (PhoneExistException pe) {
            return R.error(BizCodeEnum.MEMBER_PHONE_UNIQUE_EXCEPTION);
        } catch (UserNameExistException ue) {
            return R.error(BizCodeEnum.MEMBER_USERNAME_UNIQUE_EXCEPTION);
        }
        return R.ok("注册成功");
    }

    @RequestMapping("feign/test")
    public R feignTest() {
        R test = productFeign.test();
        Object page = test.get("page");

        return R.ok().put("feign", page).put("lalala", "hahah");
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallmember:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallmember:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallmember:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallmember:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallmember:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
