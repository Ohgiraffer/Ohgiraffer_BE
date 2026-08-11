package com.ohgiraffer.consultation.domain.model;

public record SaveRecordResult(boolean aiBriefGenerated, String message) {

    public static SaveRecordResult success() {
        return new SaveRecordResult(true, "메모가 저장되고 AI 요약이 생성되었습니다.");
    }

    public static SaveRecordResult aiFailed() {
        return new SaveRecordResult(false, "메모는 저장되었지만 AI 요약 생성에 실패했습니다. 다시 시도에 주시길 바랍니다.");
    }
}