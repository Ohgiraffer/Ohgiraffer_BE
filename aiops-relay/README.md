# AIOps 중계서버 (aiops-relay)

Grafana 알럿 → Loki 로그 컨텍스트 수집 → Claude API로 발생/why/how 추론 → Slack 전송까지 이어주는 중계서버.

```
Grafana 알럿 발동
  → POST /webhook/grafana
    → (firing이면) Loki에서 최근 로그 조회
    → Claude API 호출 (발생/why/how)
    → Slack으로 결과 전송
```

## 1. 설치

```bash
cd aiops-relay
npm install
cp .env.example .env
```

`.env` 파일을 열어서 값 채우기:

| 변수 | 설명 |
|---|---|
| `ANTHROPIC_API_KEY` | https://console.anthropic.com 에서 발급 |
| `SLACK_WEBHOOK_URL` | 아까 만든 CampFlow Alert Bot의 Incoming Webhook URL |
| `LOKI_URL` | 기본값 `http://localhost:3100` 그대로 두면 됨 |
| `LOKI_QUERY_LABEL` / `LOKI_QUERY_VALUE` | 아래 "중요 제약" 참고 |

## 2. 실행

```bash
npm start
```

`http://localhost:4000` 에서 뜨는지 확인:
```bash
curl http://localhost:4000/health
# {"status":"ok"}
```

## 3. Grafana에 웹훅 Contact point 추가

지금까지는 Grafana가 **Slack으로 직접** 알럿을 보냈는데, 이제는 **이 중계서버로 먼저 보내고**, 중계서버가 AI 분석을 거쳐서 Slack으로 보내는 구조로 바꿔야 해요.

1. Grafana → **Alerting → Notification configuration → Contact points → + Add contact point**
2. Name: `AI Relay Webhook`
3. Integration: **Webhook**
4. URL:
   - 맥에서 Grafana가 Docker 컨테이너로 떠 있고, 중계서버는 로컬(호스트)에서 `npm start`로 띄웠다면:
     ```
     http://host.docker.internal:4000/webhook/grafana
     ```
   - (중계서버도 나중에 Docker로 옮기면 컨테이너명으로 바꿔야 함)
5. **Save contact point**

## 4. 알럿 규칙이 이 새 Contact point를 쓰도록 변경

기존 3개 알럿 규칙(`DB Connection Pool Saturation`, `HTTP 5xx Error Rate Spike`, `JVM Heap Memory High Usage`)이 지금 **CampFlow Alert Bot**(Slack 직접)을 쓰고 있을 거예요. 이걸 **AI Relay Webhook**으로 바꿔야 해요.

- 가장 쉬운 방법: **Notification policies** 탭에서 `campflow-group`(또는 default policy)의 Contact point를 `AI Relay Webhook`으로 변경
- 또는 각 알럿 규칙 Edit → Notifications 섹션에서 개별적으로 Contact point 변경

**주의**: 이렇게 바꾸면 Grafana가 더 이상 Slack에 직접 안 보내요. 대신 중계서버가 (분석 결과를 담아서) Slack에 보내니, 최종적으로 Slack에 오는 메시지 자체는 끊기지 않고 오히려 더 풍부해져요.

## 5. 테스트

이전에 썼던 강제 500 에러 트리거 방법 등으로 알럿을 다시 발동시켜보고:
- 중계서버 터미널 로그에 `[webhook] 처리 시작 → 로그 N줄 수집됨 → AI 분석 완료 → 처리 완료`가 순서대로 찍히는지 확인
- Slack에 "🔍 발생 / ❓ Why / 🛠 How" 3단 구성으로 메시지가 오는지 확인

## ⚠️ 중요 제약: 지금 로그 컨텍스트가 비어있을 수 있음

`promtail-config.yml`은 **Docker 컨테이너의 로그만** 수집하도록 되어 있어요 (`docker_sd_configs`). 그런데 지금 `campflow-app`은 Docker가 아니라 **로컬에서 `./gradlew bootRun`으로 직접 실행 중**이라, Loki에 이 앱의 로그가 전혀 안 쌓여 있어요.

즉 지금 상태로는 `fetchRecentLogs()`가 항상 빈 배열을 반환하고, Claude에게 "로그를 가져오지 못했다"는 문구가 그대로 전달돼요. **파이프라인 자체는 안 죽고 정상 작동**하지만, "로그 기반 원인 추론"이라는 핵심 가치는 로그가 있어야 제대로 살아나요.

### 해결 옵션 (택 1)

**옵션 A — 가장 간단: 로그를 파일로 남기고 promtail이 그 파일을 보게 하기**
1. `application.yaml`에 로그를 파일로도 남기게 설정:
   ```yaml
   logging:
     file:
       name: logs/campflow-app.log
   ```
2. `promtail-config.yml`에 파일 기반 scrape_config 추가:
   ```yaml
   scrape_configs:
     - job_name: campflow-app-file
       static_configs:
         - targets: [localhost]
           labels:
             container: campflow-app
             __path__: /var/log/campflow-app.log   # 마운트 경로에 맞게 조정
   ```
3. `docker-compose-monitoring.yml`의 promtail 볼륨에 프로젝트의 `logs/` 폴더를 추가 마운트

**옵션 B — 나중에: campflow-app 자체를 Docker 컨테이너로 옮기기**
- 그러면 지금 있는 `docker_sd_configs` 방식 그대로 자동으로 로그가 잡힘
- 지금 로컬 개발 단계에서는 굳이 서두를 필요 없음

**옵션 C — 지금 데모 목적에는 이걸로 충분: 로그 없이 진행**
- Claude가 "로그 근거가 부족하니 확실하지 않음"이라고 답하는 것도 사실 정직한 AI 응답이라, 데모에서는 "로그 연동이 안 된 상태에서도 AI가 불확실성을 인정하며 답한다"는 것 자체를 보여줄 수도 있음
- 시간이 없으면 이대로 두고, 발표에서는 "옵션 A로 확장 가능"이라고 언급하는 것도 방법

## 파일 구조

```
aiops-relay/
├── server.js         # 웹훅 수신 + 파이프라인 오케스트레이션
├── lib/
│   ├── loki.js       # Loki 로그 조회
│   ├── claude.js     # Claude API 호출 (발생/why/how 추론)
│   └── slack.js      # Slack 메시지 포맷 및 전송
├── package.json
└── .env.example
```
