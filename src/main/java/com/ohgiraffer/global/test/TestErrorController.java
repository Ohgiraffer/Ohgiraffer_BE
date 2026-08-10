package com.ohgiraffer.global.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// ⚠️ 테스트용 임시 컨트롤러 - 알럿 테스트 끝나면 반드시 삭제할 것
@RestController
public class TestErrorController {

    @GetMapping("/test/force-error")
    public void forceError() {
        throw new RuntimeException("알럿 테스트용 강제 500 에러");
    }

}
