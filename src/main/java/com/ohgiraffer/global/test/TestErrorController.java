package com.ohgiraffer.global.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestErrorController {

    @GetMapping("/test/force-error")
    public void forceError() {
        throw new RuntimeException("알럿 테스트용 강제 500 에러");
    }

}
