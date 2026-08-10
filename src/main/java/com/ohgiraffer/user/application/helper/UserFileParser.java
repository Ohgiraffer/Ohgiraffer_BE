package com.ohgiraffer.user.application.helper;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserFileParser {
    boolean supports(String filename);
    List<List<Object>> parse(MultipartFile file);
}
