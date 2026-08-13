package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.chatbot.application.port.ChatbotSessionPort;
import com.ohgiraffer.chatbot.domain.model.ChatbotGeminiTurnResult;
import com.ohgiraffer.chatbot.domain.model.ChatbotSessionTurn;
import com.ohgiraffer.chatbot.domain.model.ChatbotGeminiFunctionCall;
import com.ohgiraffer.chatbot.infrastructure.config.ChatbotBotProperties;
import com.ohgiraffer.chatbot.infrastructure.gemini.ChatbotFunctionCatalog;
import com.ohgiraffer.chatbot.infrastructure.gemini.ChatbotGeminiCallAdapter;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotOrchestrator {

    private static final int MAX_HOPS = 5; // Gemini functionCall 반복 최대 허용 횟수 - 무한루프 방지

    private final ChatbotWebhookPayloadParser payloadParser;
    private final UserRepository userRepository;
    private final ChatbotSessionPort chatbotSessionPort;
    private final ChatbotSessionLockedUpdater chatbotSessionLockedUpdater;
    private final ChatbotFunctionCatalog chatbotFunctionCatalog;
    private final ChatbotGeminiCallAdapter chatbotGeminiCallAdapter;
    private final ChatbotFunctionDispatcher chatbotFunctionDispatcher;
    private final SendbirdApiPort sendbirdApiPort;
    private final ChatbotBotProperties chatbotBotProperties;

    public void handleIncomingMessage(String rawPayload) {
        Optional<ChatbotWebhookPayloadParser.ParsedBotMessage> maybeParsed = payloadParser.parse(rawPayload);
        if (maybeParsed.isEmpty()) {
            // 봇 자신의 발화 에코 등 처리 대상이 아닌 콜백 - 조용히 무시
            return;
        }
        ChatbotWebhookPayloadParser.ParsedBotMessage parsed = maybeParsed.get();

        User user = userRepository.findById(parsed.senderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Role role = user.getRole();

        // updateHistory가 갱신된 히스토리(List)를 그대로 반환하므로, 이걸로 바로 최종 답변 추출
        // (기존 코드처럼 chatbotSessionPort.findHistory()를 다시 호출할 필요 없음 - 불필요한 Redis 재조회 제거)
        List<ChatbotSessionTurn> updatedHistory = chatbotSessionLockedUpdater.updateHistory(parsed.senderId(),
                history -> runConversationTurn(parsed, user, role, history));

        String finalAnswer = extractLastModelText(updatedHistory);
        sendbirdApiPort.sendBotMessage(chatbotBotProperties.getUserId(), parsed.channelId(), finalAnswer);
    }

    // 락 내부에서 실행되는 실제 대화 처리 로직 - 히스토리를 받아 갱신된 히스토리를 반환
    private List<ChatbotSessionTurn> runConversationTurn(ChatbotWebhookPayloadParser.ParsedBotMessage parsed,
                                                         User user, Role role, List<ChatbotSessionTurn> history) {
        List<ChatbotSessionTurn> workingHistory = new ArrayList<>(history);
        workingHistory.add(new ChatbotSessionTurn("user", List.of(Map.of("text", parsed.message())), LocalDateTime.now()));

        List<Map<String, Object>> tools = chatbotFunctionCatalog.buildToolsForRole(role);

        for (int hop = 0; hop < MAX_HOPS; hop++) {
            List<Map<String, Object>> contents = toGeminiContents(workingHistory);
            ChatbotGeminiTurnResult result = chatbotGeminiCallAdapter.call(contents, tools);

            if (!result.hasFunctionCalls()) {
                // 최종 텍스트 응답 - 히스토리에 반영하고 종료
                String answer = result.finalText() != null ? result.finalText() : "죄송합니다, 답변을 생성하지 못했습니다.";
                workingHistory.add(new ChatbotSessionTurn("model", List.of(Map.of("text", answer)), LocalDateTime.now()));
                return workingHistory;
            }

            // functionCall 응답 - model 턴으로 기록 후, 각 함수 실행 결과를 function 턴으로 추가
            workingHistory.add(new ChatbotSessionTurn("model",
                    result.functionCalls().stream()
                            .map(this::toFunctionCallPart)
                            .toList(),
                    LocalDateTime.now()));

            for (ChatbotGeminiFunctionCall call : result.functionCalls()) {
                Object executionResult = safeDispatch(call, parsed.senderId(), role);
                // role "function"이 아니라 "user" - Gemini 3.x는 "function" role 자체를 거부함
                // functionResponse에 id 포함 필수 - 대응하는 functionCall의 id와 반드시 일치해야 모델이 매칭함
                workingHistory.add(new ChatbotSessionTurn("user",
                        List.of(Map.of("functionResponse", Map.of(
                                "id", call.id(),
                                "name", call.name(),
                                "response", Map.of("result", executionResult)
                        ))),
                        LocalDateTime.now()));
            }
        }

        // MAX_HOPS 초과 - 안전장치 발동, 강제 종료 메시지
        log.warn("[ChatbotOrchestration] 최대 함수 호출 횟수({}) 초과 - 강제 종료 | userId={}", MAX_HOPS, parsed.senderId());
        workingHistory.add(new ChatbotSessionTurn("model",
                List.of(Map.of("text", "요청이 너무 복잡해서 한 번에 처리하지 못했습니다. 질문을 나눠서 다시 시도해주세요.")),
                LocalDateTime.now()));
        return workingHistory;
    }

    // Gemini가 요청한 functionCall을 model 턴의 part로 변환 - id는 필수 echo 대상, thoughtSignature는 있을 때만 포함(병렬 호출 시 첫 번째 part에만 존재)
    private Object toFunctionCallPart(ChatbotGeminiFunctionCall fc) {
        Map<String, Object> functionCallBody = new HashMap<>();
        functionCallBody.put("id", fc.id());
        functionCallBody.put("name", fc.name());
        functionCallBody.put("args", fc.args());

        Map<String, Object> part = new HashMap<>();
        part.put("functionCall", functionCallBody);
        if (fc.thoughtSignature() != null) {
            part.put("thoughtSignature", fc.thoughtSignature()); // 다음 요청에 재전송해야 모델이 이전 추론 문맥 유지함
        }
        return part;
    }

    // 함수 실행 중 에러 처리(에러 처리 A안: 서버가 하드코딩 메시지) - functionResponse에 에러 표시를 담아 Gemini에게 전달
    private Object safeDispatch(ChatbotGeminiFunctionCall call, Long userId, Role role) {
        try {
            return chatbotFunctionDispatcher.dispatch(call, userId, role);
        } catch (BusinessException e) {
            log.warn("[ChatbotOrchestration] 함수 실행 실패 | function={}, userId={}, code={}", call.name(), userId, e.getErrorCode().getCode());
            return Map.of("error", "조회/처리에 실패했습니다.");
        } catch (RuntimeException e) {
            // 잘못된 Gemini 인자(형식 오류 등) 같이 BusinessException이 아닌 실행 실패도
            // 대화 전체를 중단시키지 않고 functionResponse에 에러로만 담아 넘김
            log.warn("[ChatbotOrchestration] 함수 실행 중 예상치 못한 오류 | function={}, userId={}", call.name(), userId, e);
            return Map.of("error", "조회/처리에 실패했습니다.");
        }
    }

    private List<Map<String, Object>> toGeminiContents(List<ChatbotSessionTurn> history) {
        return history.stream()
                .map(turn -> Map.<String, Object>of("role", turn.role(), "parts", turn.parts()))
                .toList();
    }

    private String extractLastModelText(List<ChatbotSessionTurn> history) {
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatbotSessionTurn turn = history.get(i);
            if ("model".equals(turn.role())) {
                for (Object part : turn.parts()) {
                    if (part instanceof Map<?, ?> map && map.containsKey("text")) {
                        return (String) map.get("text");
                    }
                }
            }
        }
        return "답변을 생성하지 못했습니다.";
    }

    // 웹훅 payload 파싱 과정 없이, 이미 확보된 userId/channelId/message로 바로 대화 처리 시작
    // ChatMessageCommandService에서 직접 호출하는 우회 경로용 진입점
    public void handleDirectMessage(Long userId, String channelId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Role role = user.getRole();

        // ParsedBotMessage 재사용 - 기존 runConversationTurn() 로직을 그대로 씀
        List<ChatbotSessionTurn> updatedHistory = chatbotSessionLockedUpdater.updateHistory(userId,
                history -> runConversationTurn(
                        new ChatbotWebhookPayloadParser.ParsedBotMessage(channelId, userId, message),
                        user, role, history));

        String finalAnswer = extractLastModelText(updatedHistory);
        sendbirdApiPort.sendBotMessage(chatbotBotProperties.getUserId(), channelId, finalAnswer); // 봇 답변 즉시 발신
    }

}
