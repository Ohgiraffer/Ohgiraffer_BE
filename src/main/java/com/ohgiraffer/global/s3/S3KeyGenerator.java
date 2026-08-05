package com.ohgiraffer.global.s3;

public class S3KeyGenerator {

    private S3KeyGenerator() {}

    public static String profileImageKey(Long userId) {
        return "profileImg/" + userId;
    }
}
