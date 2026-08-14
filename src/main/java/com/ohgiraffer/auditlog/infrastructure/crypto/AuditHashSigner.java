package com.ohgiraffer.auditlog.infrastructure.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
public class AuditHashSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public AuditHashSigner(@Value("${audit.hash.secret-key}") String secretKey) {
        this.secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String sign(String rawPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signed = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signed);
        } catch (Exception e) {
            throw new IllegalStateException("감사로그 해시 서명에 실패했습니다.", e);
        }
    }
}