package com.winster.thirdpartyserver.minio.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    /**
     * 连接url
     */
    private String endpoint;
    /**
     * 用户名
     */
    private String accessKey;
    /**
     * 密码
     */
    private String secretKey;

    /**
     * 文件上传的端口
     */
    private Integer uploadPort;

    /**
     * 文件下载的端口
     */
    private Integer downloadPort;

    /**
     * token超时时间
     */
    private Integer tokenTimeout;

}
