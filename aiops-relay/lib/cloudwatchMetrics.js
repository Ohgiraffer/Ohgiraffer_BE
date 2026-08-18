/**
 * cloudwatchMetrics.js
 *
 * aiops-relay(Node.js)에서 발생하는 Gemini API 호출의 성공률/레이턴시를
 * CloudWatch 커스텀 메트릭으로 전송한다.
 *
 * 백엔드(Java)의 geminiApi/geminiApiChatbot circuit breaker 메트릭과는 별도 경로다 -
 * 이 프로세스에서 나가는 Gemini 호출(알럿 분석, 매니저 요약)은 Java 쪽 서킷브레이커가
 * 잡을 수 없으므로 여기서 직접 CloudWatch로 전송한다.
 *
 * EC2 인스턴스 프로파일(IAM 역할)의 자격증명을 그대로 쓴다 - Access Key를 여기 코드나
 * .env에 박아넣지 않는다 (백엔드 IAM 정책 결정과 동일한 원칙).
 *
 * 메트릭 전송 실패가 알럿 처리 자체를 막으면 안 되므로, 실패해도 throw하지 않고
 * 로그만 남긴다. 호출부에서도 await하지 않고 fire-and-forget으로 쓴다 (지연 방지).
 */

const { CloudWatchClient, PutMetricDataCommand } = require("@aws-sdk/client-cloudwatch");

const NAMESPACE = process.env.CLOUDWATCH_METRICS_NAMESPACE || "campflow-aiops-relay";
const REGION = process.env.AWS_REGION || "ap-northeast-2";

let cloudWatchClient = null;

function getClient() {
    if (!cloudWatchClient) {
        cloudWatchClient = new CloudWatchClient({ region: REGION });
    }
    return cloudWatchClient;
}

/**
 * @param {string} operation - "analyzeAlert" | "summarizeForManager" 등 호출 지점 구분용
 * @param {boolean} success
 * @param {number} latencyMs
 */
async function recordGeminiCallMetric({ operation, success, latencyMs }) {
    try {
        const command = new PutMetricDataCommand({
            Namespace: NAMESPACE,
            MetricData: [
                {
                    MetricName: "GeminiCallCount",
                    Dimensions: [
                        { Name: "Operation", Value: operation },
                        { Name: "Success", Value: success ? "true" : "false" },
                    ],
                    Unit: "Count",
                    Value: 1,
                },
                {
                    MetricName: "GeminiCallLatency",
                    Dimensions: [{ Name: "Operation", Value: operation }],
                    Unit: "Milliseconds",
                    Value: latencyMs,
                },
            ],
        });

        await getClient().send(command);
    } catch (err) {
        console.error("[cloudwatchMetrics] 전송 실패:", err.message);
    }
}

module.exports = { recordGeminiCallMetric };