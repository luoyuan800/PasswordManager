package com.zqx.pwd.model.manager;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class MySecureRandom extends SecureRandom {
    public MySecureRandom(String seed){
        super(seed.getBytes(StandardCharsets.UTF_16LE));
    }
}
