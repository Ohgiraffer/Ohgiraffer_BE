package com.ohgiraffer.user.presentation.api.response;

public record SetPasswordResponse(
        boolean needResetPw,
        String message
) {
    public static SetPasswordResponse of(boolean needResetPw) {
        return new SetPasswordResponse(needResetPw, "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
    }
}
