package com.ohgiraffer.global.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import org.springframework.http.ContentDisposition;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class S3UrlResolver {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final Duration URL_EXPIRATION = Duration.ofHours(24);
    private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration PREVIEW_URL_EXPIRATION = Duration.ofMinutes(5);

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

    public String resolveDownload(String key, String originalFileName) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "S3 key가 필요합니다."
            );
        }

        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "원본 파일명이 필요합니다."
            );
        }

        String contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                originalFileName,
                                StandardCharsets.UTF_8
                        )
                        .build()
                        .toString();

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .responseContentDisposition(
                                contentDisposition
                        )
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                DOWNLOAD_URL_EXPIRATION
                        )
                        .getObjectRequest(
                                getObjectRequest
                        )
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }

    public String resolvePreview(
            String key,
            String originalFileName,
            String contentType
    ) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "S3 key가 필요합니다."
            );
        }

        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "원본 파일명이 필요합니다."
            );
        }

        if (contentType == null
                || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "콘텐츠 유형이 필요합니다."
            );
        }

        String contentDisposition =
                ContentDisposition
                        .inline()
                        .filename(
                                originalFileName,
                                StandardCharsets.UTF_8
                        )
                        .build()
                        .toString();

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .responseContentType(
                                contentType
                        )
                        .responseContentDisposition(
                                contentDisposition
                        )
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                PREVIEW_URL_EXPIRATION
                        )
                        .getObjectRequest(
                                getObjectRequest
                        )
                        .build();

        return s3Presigner
                .presignGetObject(
                        presignRequest
                )
                .url()
                .toString();
    }

    // 만료 없는 고정 public URL 생성 - 버킷 정책으로 이미 공개된 prefix(chatAttachments 등) 전용
    // 해당 prefix가 버킷 정책에서 public read로 열려있지 않으면 이 URL로는 접근 불가함
    public String resolvePublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        // '/'는 경로 구분자로 유지하고, 각 세그먼트만 인코딩 (슬래시까지 인코딩되면 경로 구조가 깨짐)
        String encodedKey = java.util.Arrays.stream(key.split("/", -1))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "/" + b)
                .orElse("");

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

}