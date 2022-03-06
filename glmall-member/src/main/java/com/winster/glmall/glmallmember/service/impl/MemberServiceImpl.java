package com.winster.glmall.glmallmember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.exception.BizCodeEnum;
import com.winster.common.to.GithubUserTo;
import com.winster.common.to.UserLoginTo;
import com.winster.common.to.UserRegisterTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallmember.dao.MemberDao;
import com.winster.glmall.glmallmember.entity.MemberEntity;
import com.winster.glmall.glmallmember.entity.MemberLevelEntity;
import com.winster.glmall.glmallmember.exception.PhoneExistException;
import com.winster.glmall.glmallmember.service.MemberLevelService;
import com.winster.glmall.glmallmember.service.MemberService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterTo to) {
        // 用户手机号和用户名必须是唯一的
        checkPhoneUnique(to.getPhone());
        checkUserNameUnique(to.getUserName());

        MemberEntity entity = new MemberEntity();
        entity.setUsername(to.getUserName());
        entity.setMobile(to.getPhone());
        entity.setStatus(1);
        // 设置默认等级
        QueryWrapper<MemberLevelEntity> w1 = new QueryWrapper<>();
        w1.eq("default_status", 1);
        MemberLevelEntity levelEntity = memberLevelService.getOne(w1);
        entity.setLevelId(levelEntity.getId());
        // 设置用户密码
        // 密码加密，抛弃传统加盐方法，使用BCrypt完成密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 为了密码更加安全，在这里可以对密码进行 “加盐” 或者 “迭代处理”
        String encode = passwordEncoder.encode(to.getPassword());
        // 密码验证方法
        // passwordEncoder.matches(to.getPassword(),encode);
        entity.setPassword(encode);
        entity.setNickname(to.getUserName());

        this.save(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) {
        QueryWrapper<MemberEntity> w1 = new QueryWrapper<>();
        w1.eq("mobile", phone);
        long count = this.count(w1);
        if (count > 0) {
            throw new PhoneExistException(BizCodeEnum.MEMBER_PHONE_UNIQUE_EXCEPTION.getMsg());
        }
    }

    @Override
    public void checkUserNameUnique(String userName) {
        QueryWrapper<MemberEntity> w1 = new QueryWrapper<>();
        w1.eq("username", userName);
        long count = this.count(w1);
        if (count > 0) {
            throw new PhoneExistException(BizCodeEnum.MEMBER_PHONE_UNIQUE_EXCEPTION.getMsg());
        }
    }

    @Override
    public MemberEntity login(UserLoginTo to) {
        QueryWrapper<MemberEntity> w1 = new QueryWrapper<>();
        w1.and(w -> w.eq("username", to.getUserName())
                .or().eq("mobile", to.getUserName()));
        MemberEntity one = getOne(w1);

        if (one != null) {
            // 校验用户密码
            String passwordDB = one.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(to.getPassword(), passwordDB);
            return matches ? one : null;
        }

        return null;
    }

    @Override
    public MemberEntity githubLogin(GithubUserTo to) {
        // 判断当前用户是否注册
        QueryWrapper<MemberEntity> w1 = new QueryWrapper<>();
        w1.eq("social_id", to.getId())
        .eq("social_type", "github");
        MemberEntity one = this.getOne(w1);

        if (one != null) {
            return one;
        }
        MemberEntity entity = new MemberEntity();
        entity.setUsername(to.getLogin());
        entity.setHeader(to.getAvatar_url());
        entity.setEmail(to.getEmail());
        entity.setStatus(1);
        entity.setNickname(to.getName());
        // 设置用户默认等级
        QueryWrapper<MemberLevelEntity> w2 = new QueryWrapper<>();
        w2.eq("default_status", 1);
        MemberLevelEntity levelEntity = memberLevelService.getOne(w2);
        entity.setLevelId(levelEntity.getId());
        entity.setSocialId(to.getId()+"");
        entity.setSocialType("github");
        this.save(entity);

        return entity;
    }

}