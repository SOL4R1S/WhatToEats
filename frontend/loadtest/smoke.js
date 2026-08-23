/**
 * WhatToEat 부하테스트 — 핵심 GET API
 * 운영 API(api.whattoeats.app) 대상
 *
 * 스모크: 1분, 1 VU — 정상 동작 확인
 * 부하:   3분, 50 VU — 일반 부하
 * 스트레스: 3분, 200 VU — 피크 부하
 */
import http from "k6/http";
import { check, sleep } from "k6";

const BASE = "https://api.whattoeats.app";

// 실행 모드: smoke / load / stress (--env MODE=load 로 선택)
const MODE = __ENV.MODE || "smoke";

const STAGES = {
  smoke: [{ duration: "1m", target: 1 }],
  load: [
    { duration: "1m", target: 50 },
    { duration: "2m", target: 50 },
  ],
  stress: [
    { duration: "1m", target: 100 },
    { duration: "1m", target: 200 },
    { duration: "1m", target: 200 },
  ],
};

export const options = {
  stages: STAGES[MODE],
  thresholds: {
    http_req_failed: ["rate<0.05"], // 에러율 5% 미만
    http_req_duration: ["p(95)<2000"], // p95 응답 2초 미만
  },
};

const endpoints = [
  { name: "식당 목록", url: "/api/v1/restaurants" },
  { name: "오늘의 핫플", url: "/api/v1/restaurants/today-hot" },
  { name: "DB 이름 검색", url: "/api/v1/restaurants/search?name=%ED%95%B4%EC%9A%B4%EB%8C%80" },
  { name: "DB 주소 검색", url: "/api/v1/restaurants/search?address=%EC%88%98%EC%98%81%EA%B5%AC" },
];

export default function () {
  for (const ep of endpoints) {
    const res = http.get(`${BASE}${ep.url}`, {
      tags: { name: ep.name },
    });

    check(res, {
      [`${ep.name} 200`]: (r) => r.status === 200,
    });

    sleep(0.5); // 1초당 2회 이하
  }
}
