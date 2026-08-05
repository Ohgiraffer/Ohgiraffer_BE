package com.ohgiraffer.global.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Component
public class S3UrlResolver {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final Duration URL_EXPIRATION = Duration.ofHours(24);

    public S3UrlResolver(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public String resolve(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(URL_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // 만료 없는 고정 public URL 생성 - 버킷 정책으로 이미 공개된 prefix(chatAttachments 등) 전용
    // 해당 prefix가 버킷 정책에서 public read로 열려있지 않으면 이 URL로는 접근 불가함
    public String resolvePublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

}