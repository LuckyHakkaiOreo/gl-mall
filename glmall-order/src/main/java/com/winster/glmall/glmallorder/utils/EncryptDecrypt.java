package com.winster.glmall.glmallorder.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptDecrypt {

    @Value("${jasypt.encryptor.password}")
    private  String password;

    public  String encryptPwd(String password, String value) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(cryptor(password));
        String encrypt = encryptor.encrypt(value);
        return encrypt;
    }

    public  String encrypt(String value) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(cryptor(password));
        String encrypt = encryptor.encrypt(value);
        return encrypt;
    }

    public  String decryptPwd(String password, String value) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(cryptor(password));
        String encrypt = encryptor.decrypt(value);
        return encrypt;
    }

    public  String decrypt(String value) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(cryptor(password));
        String encrypt = encryptor.decrypt(value);
        return encrypt;
    }

    public  PBEConfig cryptor(String password) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(StandardPBEByteEncryptor.DEFAULT_ALGORITHM);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName(null);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        return config;
    }

    public static void main(String[] args) {
        EncryptDecrypt decrypt = new EncryptDecrypt();
        System.out.println(decrypt.encryptPwd("mysalt","123456"));
        System.out.println(decrypt.decryptPwd("mysalt","MB0FZMvp91xYSF4KxpYeUw=="));
    }

}
