package com.ohgiraffer.global.s3;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class S3FileHandler {

    private static final int MAX_S3_KEY_BYTE_LENGTH = 1024;

    private final S3Client s3Client;
    private final String bucket;

    public S3FileHandler(
            S3Client s3Client,
            @Value("${cloud.aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    /**
     * 파일을 S3에 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param key  S3에 저장할 객체 key
     * @return 저장된 S3 객체 key
     */
    public String upload(
            MultipartFile file,
            String key
    ) {
        validateUploadRequest(file, key);

        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(resolveContentType(file))
                            .contentLength(file.getSize())
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return key;
        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.FILE_STORAGE_UPLOAD_FAILED,
                    "업로드할 파일을 읽는 중 오류가 발생했습니다.",
                    exception
            );
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.FILE_STORAGE_UPLOAD_FAILED,
                    "파일 저장소에 파일을 업로드하지 못했습니다.",
                    exception
            );
        }
    }

    /**
     * S3에 저장된 파일을 삭제합니다.
     *
     * key가 null이거나 공백이면 삭제 요청을 수행하지 않습니다.
     *
     * @param key 삭제할 S3 객체 key
     */
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();

            s3Client.deleteObject(request);
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.FILE_STORAGE_DELETE_FAILED,
                    "파일 저장소에서 파일을 삭제하지 못했습니다.",
                    exception
            );
        }
    }

    private void validateUploadRequest(
            MultipartFile file,
            String key
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "업로드할 파일이 필요합니다."
            );
        }

        if (file.getSize() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "빈 파일은 업로드할 수 없습니다."
            );
        }

        if (key == null || key.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키가 필요합니다."
            );
        }

        int keyByteLength =
                key.getBytes(StandardCharsets.UTF_8).length;

        if (keyByteLength > MAX_S3_KEY_BYTE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키는 UTF-8 기준 1,024바이트를 초과할 수 없습니다."
            );
        }
    }

    private String resolveContentType(
            MultipartFile file
    ) {
        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
}