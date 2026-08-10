package com.ohgiraffer.user.application.helper;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserFileParserResolver {

    private final List<UserFileParser> parsers;

    public List<List<Object>> resolveAndParse(MultipartFile file) {
        String filename = file.getOriginalFilename();

        return parsers.stream()
                .filter(parser -> parser.supports(filename))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE))
                .parse(file);
    }
}