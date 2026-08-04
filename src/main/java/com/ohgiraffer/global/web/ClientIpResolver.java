package com.ohgiraffer.global.web;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Pattern;

public final class ClientIpResolver {
    // nginx를 우회해서 앱에 직접 접근하는 경우까지 대비한 방어적 검증.
    // 엄격한 RFC 검증이 아니라, Redis 키로 쓰기 전에 터무니없는 문자열을 걸러내는 용도.
    private static final Pattern IPV4 = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$");
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:]{2,45}$");

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            String ip = realIp.trim();
            if (isValidIp(ip)) {
                return ip;
            }
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String ip = forwardedFor.split(",")[0].trim();
            if (isValidIp(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.length() > 45) {
            return false;
        }
        return IPV4.matcher(ip).matches() || IPV6.matcher(ip).matches();
    }
}
