package com.winster.glmall.glmallorder.utils;

import org.apache.commons.lang.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("desencrypt")
public class MyStringEncryptor implements StringEncryptor {

    @Resource
    private EncryptDecrypt encryptDecrypt;

    @Override
    public String encrypt(String message) {
        if (StringUtils.isNotBlank(message)) {
            try {
                message = encryptDecrypt.encrypt(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    @Override
    public String decrypt(String encryptedMessage) {
        if (StringUtils.isNotBlank(encryptedMessage)) {
            try {
                encryptedMessage = encryptDecrypt.decrypt(encryptedMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encryptedMessage;
    }
}
