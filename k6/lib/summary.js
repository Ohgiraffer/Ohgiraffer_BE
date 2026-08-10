// k6/lib/summary.js
// ------------------------------------------------------------
// k6 실행 결과를 Markdown 표와 JSON으로 저장합니다.
// ------------------------------------------------------------

import { RESULT_NAME } from './config.js';

function safeName(name) {
    return String(name || RESULT_NAME)
        .replace(/[^a-zA-Z0-9._-]/g, '-')
        .replace(/-+/g, '-');
}

function metricValue(data, metricName, key) {
    const metric = data.metrics[metricName];
    if (!metric || !metric.values || metric.values[key] === undefined) return '';
    const value = metric.values[key];
    if (typeof value !== 'number') return String(value);
    return Number.isInteger(value) ? String(value) : value.toFixed(2);
}

function rateValue(data, metricName) {
    const metric = data.metrics[metricName];
    if (!metric || !metric.values || metric.values.rate === undefined) return '';
    return `${(metric.values.rate * 100).toFixed(2)}%`;
}

function countValue(data, metricName) {
    return metricValue(data, metricName, 'count');
}

function durationRow(data, metricName, label) {
    return `| ${label} | ${metricValue(data, metricName, 'avg')} | ${metricValue(data, metricName, 'min')} | ${metricValue(data, metricName, 'med')} | ${metricValue(data, metricName, 'p(90)')} | ${metricValue(data, metricName, 'p(95)')} | ${metricValue(data, metricName, 'p(99)')} | ${metricValue(data, metricName, 'max')} |`;
}

function buildMarkdown(data, name) {
    const lines = [];

    lines.push(`# k6 Result - ${name}`);
    lines.push('');
    lines.push('## Summary');
    lines.push('');
    lines.push('| Metric | Value |');
    lines.push('| --- | ---: |');
    lines.push(`| http_reqs | ${countValue(data, 'http_reqs')} |`);
    lines.push(`| iterations | ${countValue(data, 'iterations')} |`);
    lines.push(`| checks success rate | ${rateValue(data, 'checks')} |`);
    lines.push(`| http_req_failed | ${rateValue(data, 'http_req_failed')} |`);
    lines.push(`| data_received bytes | ${countValue(data, 'data_received')} |`);
    lines.push(`| data_sent bytes | ${countValue(data, 'data_sent')} |`);
    lines.push('');

    lines.push('## Duration Metrics');
    lines.push('');
    lines.push('| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |');
    lines.push('| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |');
    lines.push(durationRow(data, 'http_req_duration', 'http_req_duration'));
    lines.push(durationRow(data, 'http_req_waiting',  'http_req_waiting'));
    lines.push(durationRow(data, 'http_req_blocked',  'http_req_blocked'));
    lines.push('');

    lines.push('## Metric Meaning');
    lines.push('');
    lines.push('| Value | Meaning |');
    lines.push('| --- | --- |');
    lines.push('| avg | 전체 요청 시간의 산술 평균입니다. outlier의 영향을 받을 수 있습니다. |');
    lines.push('| min | 가장 빠른 요청 시간입니다. 정상 동작의 하한선을 볼 때 사용합니다. |');
    lines.push('| med | 중앙값입니다. 요청의 절반은 이 값보다 빠르고 절반은 느립니다. |');
    lines.push('| p90 | 90% 요청이 이 값 이하로 완료됩니다. |');
    lines.push('| p95 | 95% 요청이 이 값 이하로 완료됩니다. 주요 합격 기준입니다. |');
    lines.push('| p99 | 99% 요청이 이 값 이하로 완료됩니다. tail latency 관찰에 사용합니다. |');
    lines.push('| max | 가장 느린 요청 시간입니다. 단일 outlier 여부를 확인할 때 사용합니다. |');
    lines.push('');

    lines.push('## How To Compare');
    lines.push('');
    lines.push('| Compare Point | What To Look For |');
    lines.push('| --- | --- |');
    lines.push('| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |');
    lines.push('| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |');
    lines.push('| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |');
    lines.push('| Prometheus | 서버 내부 HTTP/custom metric 추세 |');
    lines.push('| Loki | 느린 요청의 로그와 에러 이벤트 |');
    lines.push('');

    return `${lines.join('\n')}\n`;
}

function buildConsoleTable(data, name) {
    return [
        '',
        `k6 summary: ${name}`,
        '------------------------------------------------------------',
        `http_reqs        : ${countValue(data, 'http_reqs')}`,
        `iterations       : ${countValue(data, 'iterations')}`,
        `checks           : ${rateValue(data, 'checks')}`,
        `http_req_failed  : ${rateValue(data, 'http_req_failed')}`,
        '',
        'http_req_duration (ms)',
        `  avg             : ${metricValue(data, 'http_req_duration', 'avg')}`,
        `  min             : ${metricValue(data, 'http_req_duration', 'min')}`,
        `  med             : ${metricValue(data, 'http_req_duration', 'med')}`,
        `  p90             : ${metricValue(data, 'http_req_duration', 'p(90)')}`,
        `  p95             : ${metricValue(data, 'http_req_duration', 'p(95)')}`,
        `  p99             : ${metricValue(data, 'http_req_duration', 'p(99)')}`,
        `  max             : ${metricValue(data, 'http_req_duration', 'max')}`,
        '',
        'http_req_waiting (ms)',
        `  avg             : ${metricValue(data, 'http_req_waiting', 'avg')}`,
        `  p95             : ${metricValue(data, 'http_req_waiting', 'p(95)')}`,
        `  max             : ${metricValue(data, 'http_req_waiting', 'max')}`,
        '------------------------------------------------------------',
        `Markdown report  : k6/results/${name}-summary.md`,
        `JSON report      : k6/results/${name}-summary.json`,
        '',
    ].join('\n');
}

export function createSummaryHandler(defaultName) {
    return function handleSummary(data) {
        const name = safeName(__ENV.RESULT_NAME || defaultName || RESULT_NAME);
        return {
            stdout: buildConsoleTable(data, name),
            [`k6/results/${name}-summary.md`]:   buildMarkdown(data, name),
            [`k6/results/${name}-summary.json`]: JSON.stringify(data, null, 2),
        };
    };
}