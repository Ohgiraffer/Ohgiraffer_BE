const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";

function isPlaceholder(v) {
  return !v || /[^\x00-\x7F]/.test(v) || v.trim() === "";
}

async function analyzeAlert({ alertName, status, value, labels, logs }) {
  if (isPlaceholder(GEMINI_API_KEY)) {
    return {
      발생: `${alertName} 알럿이 ${status} 상태입니다. (GEMINI_API_KEY 미설정 - AI 분석 생략됨)`,
      why: "분석 불가 (API 키 없음 또는 .env.example 플레이스홀더가 그대로 남아 있음)",
      how: ".env 파일에서 GEMINI_API_KEY=실제키 형태로 채운 뒤 서버를 재시작하세요.",
    };
  }

  const logSnippet =
      logs.length > 0
          ? logs.join("\n")
          : "(최근 로그를 가져오지 못했습니다. Loki 연결 또는 라벨 설정을 확인하세요.)";

  const labelText = Object.entries(labels || {})
      .map(([k, v]) => `${k}=${v}`)
      .join(", ");

  const prompt = `당신은 백엔드 서비스의 SRE/AIOps 분석가입니다.
아래 Grafana 알럿 정보와 최근 로그를 보고, 실제 운영자가 참고할 수 있도록
"발생", "why(원인 추정)", "how(권장 조치)" 세 가지를 한국어로 간결하게 작성하세요.

각 항목은 2~3문장 이내로 작성하고, 로그에서 근거를 찾을 수 있으면 구체적으로 언급하세요.
근거가 부족하면 "확실하지 않음"을 명시하고 추가로 확인해야 할 것을 제안하세요.
반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.

{"발생": "...", "why": "...", "how": "..."}

--- 알럿 정보 ---
알럿 이름: ${alertName}
상태: ${status}
현재 값: ${value}
라벨: ${labelText}

--- 최근 로그 (최대 50줄) ---
${logSnippet}
`;

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`;

    const res = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": GEMINI_API_KEY,
      },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.3,
          maxOutputTokens: 2000, // thinkingConfig 미지원 모델이라, thinking 소비분까지 감안해 대폭 상향
          responseMimeType: "application/json",
        },
      }),
    });

    if (!res.ok) {
      const text = await res.text();
      console.error(`[gemini] API 호출 실패: HTTP ${res.status} - ${text}`);
      return {
        발생: `${alertName} 알럿이 ${status} 상태입니다.`,
        why: "AI 분석 실패 (API 오류)",
        how: "중계서버 로그를 확인하세요.",
      };
    }

    const data = await res.json();
    const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text || "";

    try {
      return JSON.parse(rawText);
    } catch {
      const jsonMatch = rawText.match(/\{[\s\S]*\}/);
      if (jsonMatch) return JSON.parse(jsonMatch[0]);

      console.error("[gemini] JSON 파싱 실패, 원본:", rawText);
      return {
        발생: `${alertName} 알럿이 ${status} 상태입니다.`,
        why: "AI 응답 파싱 실패",
        how: "중계서버 로그에서 원본 응답을 확인하세요.",
      };
    }
  } catch (err) {
    console.error("[gemini] 호출 중 예외 발생:", err.message);
    return {
      발생: `${alertName} 알럿이 ${status} 상태입니다.`,
      why: "AI 분석 중 오류 발생",
      how: "중계서버 로그를 확인하세요.",
    };
  }
}

async function summarizeForManager(prompt) {
  if (isPlaceholder(GEMINI_API_KEY)) {
    return "(GEMINI_API_KEY 미설정 - 요약 생략됨. .env 확인 필요)";
  }

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`;

    const res = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": GEMINI_API_KEY, // URL 쿼리 파라미터 대신 헤더로 (analyzeAlert와 동일하게 통일)
      },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.3,
          maxOutputTokens: 2000, // 1200 -> 2000: thinking 소비분 감안, 응답 중간에 끊기는 문제 대응
        },
      }),
    });

    if (!res.ok) {
      const text = await res.text();
      console.error(`[gemini] 매니저 요약 호출 실패: HTTP ${res.status} - ${text}`);
      return "(AI 요약 실패 - 중계서버 로그 확인 필요)";
    }

    const data = await res.json();
    const candidate = data?.candidates?.[0];
    const text = candidate?.content?.parts?.[0]?.text?.trim();

    if (candidate?.finishReason === "MAX_TOKENS") {
      console.error(
          `[gemini] 매니저 요약이 MAX_TOKENS로 잘림 (maxOutputTokens=2000). 원본 일부: ${
              text?.slice(0, 100) || "(텍스트 없음, thinking에 토큰 전부 소비 추정)"
          }`
      );
      return text
          ? `${text} (…이하 생략, 토큰 제한으로 잘림)`
          : "(AI 요약 실패 - 토큰 제한으로 응답이 비어 있음)";
    }

    return text || "(빈 응답)";
  } catch (err) {
    console.error("[gemini] 매니저 요약 중 예외 발생:", err.message);
    return "(AI 요약 중 오류 발생)";
  }
}

module.exports = { analyzeAlert, summarizeForManager };