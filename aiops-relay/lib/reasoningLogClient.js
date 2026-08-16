/**
 * reasoningLogClient.js
 *
 * 백엔드의 /internal/agent-reasoning-logs 엔드포인트로 AI 판단 근거(reasoning_summary)를 전송한다.
 * "함수 호출이 실제로 트리거된 턴"에서만 호출할 것 - 매 턴 다 남기면 노이즈/비용 폭증.
 *
 * 감사 로그 저장 실패가 알럿 처리(Slack/매니저 통보) 자체를 막아서는 안 되므로,
 * 실패해도 throw하지 않고 로그만 남긴다. 지연도 주지 않도록 호출부에서 await하지 않고 fire-and-forget한다.
 */

function buildReasoningSummary(analysis) {
    if (!analysis) return null;

    const 발생 = analysis.발생 || "-";
    const why = analysis.why || "-";
    const how = analysis.how || "-";

    return `발생: ${발생} | why: ${why} | how: ${how}`;
}

async function logAgentReasoning({ sessionId, turnId, functionName, analysis, functionCallId, success, latencyMs }) {
    const backendAdminUrl = process.env.BACKEND_ADMIN_URL;
    const adminInternalToken = process.env.ADMIN_INTERNAL_TOKEN;

    if (!backendAdminUrl || !adminInternalToken) {
        console.log("[reasoningLog] BACKEND_ADMIN_URL/ADMIN_INTERNAL_TOKEN 미설정, 저장 스킵");
        return;
    }

    try {
        const res = await fetch(`${backendAdminUrl}/admin/agent-reasoning-logs`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Internal-Token": adminInternalToken,
            },
            body: JSON.stringify({
                sessionId,
                turnId,
                functionName,
                reasoningSummary: buildReasoningSummary(analysis),
                functionCallId: functionCallId || null,
                success,
                latencyMs,
            }),
        });

        if (!res.ok) {
            console.error(`[reasoningLog] 저장 실패: HTTP ${res.status}`);
        }
    } catch (err) {
        console.error("[reasoningLog] 저장 중 오류:", err.message);
    }
}

module.exports = { logAgentReasoning };