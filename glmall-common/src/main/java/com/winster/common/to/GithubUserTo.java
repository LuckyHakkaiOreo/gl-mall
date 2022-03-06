package com.winster.common.to;

import lombok.Data;

import java.util.Date;

@Data
public class GithubUserTo {
    /**
     * 登录名
     */
    private String login;
    private Long id;
    private String node_id;
    /**
     * 头像url
     */
    private String avatar_url;
    private String gravatar_id;
    private String url;
    private String html_url;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    /**
     * 用户类型
     */
    private String type;
    private Boolean site_admin;
    private String name;
    private String company;
    private String blog;
    private String location;
    private String email;
    private String hireable;
    private String bio;
    private String twitter_username;
    private Integer public_repos;
    private Integer public_gists;
    private Integer followers;
    private Integer following;
    /**
     * 创建时间
     */
    private Date created_at;
    /**
     * 更新时间
     */
    private Date updated_at;
}
