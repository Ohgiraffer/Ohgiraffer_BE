# AIOps 중계서버 (aiops-relay)

Grafana 알럿 → Loki 로그 컨텍스트 수집 → Gemini API로 발생/why/how 추론 →
Slack(개발팀, 승인 버튼)으로 통보 → 해결/실패 결과가 나온 뒤 Sendbird(매니저)로 요약 통보.

```text
Grafana 알럿 발동 (firing)
  → POST /webhook/grafana
    → Loki에서 최근 로그 조회
    → Gemini 호출 (발생/why/how)
    → 위험 등급 판정 (LOW=자동실행 / MEDIUM=1인승인 / HIGH=팀전체승인)
    → (AUTO_EXECUTE면 이 시점에 바로 실행)
    → Slack으로 개발팀 통보 (항상 먼저)
    → (해결됐으면) Sendbird로 매니저 통보 (그 다음)

Slack 승인/거부 버튼 클릭 (별도 요청, /slack/interactions)
    → 서명 검증 → 정책 재계산 → 실행/거부
    → Slack 메시지 갱신 (항상 먼저)
    → Sendbird로 매니저 통보 (그 다음)

CloudWatch Alarm 상태 변경 (별도 요청, SNS -> /webhook/cloudwatch-alarm)
    → SNS 서명 검증 → Grafana 알럿 형태로 변환 → 위 흐름과 동일한 파이프라인 재사용
    (알람명이 config/actions.js에 없으면 안전하게 HIGH/팀전체승인으로 처리됨)
```

**매니저 알림 원칙**: 항상 "개발팀 Slack 통보/갱신이 끝난 뒤"에만 나간다. 감지 시점에
매니저를 먼저 또는 동시에 알리는 경로는 없다. HIGH 등급은 결과가 나오는 즉시 매니저에게도
바로 전송되고, LOW/MEDIUM 등급은 다이제스트로 모아서 매일 09시에 한 번에 전송된다. 이 시각은
`.env`가 아니라 `lib/notifyManager.js`의 `DIGEST_CRON` 상수(`"0 9 * * *"`)에 코드로 고정되어
있으며, 바꾸려면 이 상수를 직접 수정해야 한다.

## 1. 설치

```bash
cd aiops-relay
npm install
cp .env.example .env
```

`.env`를 채운다 (아래 표 참고).

| 변수 | 설명 |
|---|---|
| `GEMINI_API_KEY` | https://aistudio.google.com 에서 발급 |
| `SLACK_WEBHOOK_URL` | 개발팀 채널의 Incoming Webhook URL |
| `SLACK_SIGNING_SECRET` | Slack 앱 설정의 Signing Secret (버튼 클릭 서명 검증용) |
| `LOKI_URL` 등 | 기본값 그대로 두면 됨 |
| `BACKEND_ADMIN_URL` / `ADMIN_INTERNAL_TOKEN` | 화이트리스트 액션 실행 시 백엔드 호출용 |
| `SENDBIRD_APP_ID` / `SENDBIRD_API_TOKEN` | Sendbird Platform API 인증 (마스터 API 토큰) |
| `SENDBIRD_BOT_USER_ID` | 매니저 채널에 메시지를 보낼 봇 유저 ID (기본 `aiops-bot`) |
| `MANAGER_CHANNEL_URL` | 아래 2번 셋업 후 채울 것. 비워두면 매니저 알림은 로그만 찍고 스킵됨 |
| `CLOUDWATCH_METRICS_NAMESPACE` | Gemini 호출 메트릭을 보낼 CloudWatch 네임스페이스. 기본값 `campflow-aiops-relay` |

## 2. 매니저 채널 최초 1회 생성

매니저들만 있는 그룹 채널이 아직 없으므로, 서버 실행 전에 딱 한 번만 만든다.

```bash
node scripts/setup-manager-channel.js <매니저1_sendbirdUserId> <매니저2_sendbirdUserId>
```

콘솔에 출력되는 `MANAGER_CHANNEL_URL` 값을 `.env`에 복사해 넣는다.
**이미 `.env`에 값이 있으면 스크립트가 중복 생성을 막기 위해 스스로 실행을 거부한다.**

## 3. 실행

```bash
npm start
```

```bash
curl http://localhost:4000/health
# {"status":"ok"}
```

## 4. Grafana 웹훅 Contact point

1. Grafana → Alerting → Notification configuration → Contact points → + Add contact point
2. Name: `AI Relay Webhook`, Integration: `Webhook`
3. URL: `http://host.docker.internal:4000/webhook/grafana` (Grafana가 Docker, 중계서버가 로컬인 경우)
4. Notification policies에서 기존 알럿 규칙의 Contact point를 이걸로 변경

## 5. Slack Interactivity

Slack 앱 설정 → Interactivity & Shortcuts → Request URL을
`http://<서버>:4000/slack/interactions`로 등록해야 승인/거부 버튼이 동작한다.

## 파일 구조

```text
aiops-relay/
├── server.js                        # 웹훅 수신 + 파이프라인 오케스트레이션
├── config/
│   └── actions.js                   # 위험 등급 판정 정책 (LOW/MEDIUM/HIGH)
├── docs/
│   └── action-risk-policy.md        # 등급 판정 기준 문서
├── lib/
│   ├── loki.js                      # Loki 로그 조회
│   ├── gemini.js                    # Gemini 호출 (알럿 분석 + 매니저 요약)
│   ├── slack.js                     # Slack 메시지 포맷/전송 (개발팀)
│   ├── slackVerify.js               # Slack 서명 검증
│   ├── executor.js                  # 화이트리스트 기반 액션 실행기
│   ├── approvalTracker.js           # 팀 승인 현황 추적
│   ├── sendbird.js                  # Sendbird Platform API 호출 (매니저)
│   ├── notifyManager.js             # 매니저 알림 라우팅 (즉시 발송 vs 다이제스트)
│   ├── reasoningLogClient.js        # AI 판단 근거(reasoning_summary) 백엔드 감사 로그 전송
│   ├── cloudwatchMetrics.js         # Gemini 호출 성공률/레이턴시 CloudWatch 커스텀 메트릭 전송
│   └── snsVerify.js                 # CloudWatch Alarm(SNS) 웹훅 서명 검증
├── scripts/
│   └── setup-manager-channel.js     # 매니저 채널 최초 1회 생성용
├── package.json
└── .env.example
```

## ⚠️ 로그 컨텍스트 관련 제약

`promtail-config.yml`이 Docker 컨테이너 로그만 수집하는 구조라, `campflow-app`을
로컬에서 `./gradlew bootRun`으로 직접 띄운 상태면 `fetchRecentLogs()`가 빈 배열을
반환한다. 파이프라인 자체는 정상 동작하지만 로그 기반 원인 추론의 정확도가 떨어진다.
해결하려면 파일 기반 promtail scrape_config를 추가하거나, 앱 자체를 Docker로 옮긴다.