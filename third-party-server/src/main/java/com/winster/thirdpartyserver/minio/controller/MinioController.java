package com.winster.thirdpartyserver.minio.controller;

import com.winster.common.utils.R;
import com.winster.thirdpartyserver.minio.properties.MinioProp;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/thirdParty/minio")
public class MinioController {

    @Resource
    private MinioProp prop;

    @Resource
    private MinioClient minioClient;

    @GetMapping("/certificaton")
    public R certificaton(String accessKey, String secretKey) {

        if (prop.getAccessKey().equals(accessKey) && prop.getSecretKey().equals(secretKey)) {
            String token = UUID.randomUUID().toString().replace("-", "");
            long now = System.currentTimeMillis();
            return R.ok().put("token", token + "_" + now);
        }
        return R.error(9999, "认证失败，请使用正确的认证信息！");
    }

    @PostMapping("/upload")
    public R upload(@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
        String fileUrl = null;
        try {
            String bucketName = StringUtils.isBlank(request.getParameter("bucketName")) ? "glmall" : request.getParameter("bucketName");

            if (null == file || 0 == file.getSize()) {
                return R.error(1999, "上传文件不能为空");
            }

            //检查文件大小
            if (file.getSize() > 10 * 1024*1024) {
                return R.error(1993, "请上传10M以内的图片");
            }

            // 判断存储桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("glmall").build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("glmall").build());
            } else {
                log.info("Bucket 'glmall' already exists.");
            }

            // 源文件名
            String originalFilename = file.getOriginalFilename();

            // 新的文件名 = 存储桶名称_时间戳.后缀名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = dateFormat.format(new Date()) + "/" + bucketName + "_" + System.currentTimeMillis() + originalFilename.substring(originalFilename.lastIndexOf("."));

            InputStream inputStream = file.getInputStream();
            // 开始上传
            PutObjectArgs argss = PutObjectArgs.builder().bucket(bucketName).
                    object(fileName).stream(
                    inputStream, file.getSize(), -1)
                    .contentType("image/jpeg")
                    .build();
            minioClient.putObject(argss);
            inputStream.close();
            String url =
                    minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(fileName)
                                    .expiry(1, TimeUnit.DAYS)
                                    .build());
            log.info(url);
            // 服务器endpoint地址，控制台/下载文件端口，bucket，文件名
            // "http://192.168.0.196" + ":9001" + "/" + "glmall" + "/" + "2022-02-06/设计模式.doc"
            fileUrl = prop.getEndpoint() + ":" + prop.getUploadPort() + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return R.error(1996, "上传失败");
        }

        return R.ok().put("fileUrl", fileUrl);
    }
}
