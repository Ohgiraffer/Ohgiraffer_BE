package com.ohgiraffer.global.aop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "campflow.logging")
public class ServiceIoLoggingHolder {

    private static volatile boolean enabled = false;

    public void setServiceIo(boolean serviceIo) {
        ServiceIoLoggingHolder.enabled = serviceIo;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
