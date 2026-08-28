import assert from "node:assert/strict";
import test from "node:test";

import { recommendRadiusMeter } from "./recommendGeo.ts";

test("동/가 단위 선택이면 1500m 반경", () => {
  assert.equal(recommendRadiusMeter("중구", "을지로동"), 1500);
  assert.equal(recommendRadiusMeter("강남구", "개포1동"), 1500);
});

test("읍 단위 선택이면 5000m 반경", () => {
  assert.equal(recommendRadiusMeter("고성군", "간성읍"), 5000);
  assert.equal(recommendRadiusMeter("양양군", "양양읍"), 5000);
});

test("면 단위 선택이면 8000m 반경", () => {
  assert.equal(recommendRadiusMeter("청송군", "파천면"), 8000);
  assert.equal(recommendRadiusMeter("양구군", "국토정중앙면"), 8000);
});

test("구/군/시 전체 선택이면 반경 제한 없음 (텍스트 검색)", () => {
  assert.equal(recommendRadiusMeter("중구", "전체"), null);
  assert.equal(recommendRadiusMeter("중구", ""), null);
  assert.equal(recommendRadiusMeter("가평군", "전체"), null);
});

test("시/도 단위 선택이면 반경 제한 없음", () => {
  assert.equal(recommendRadiusMeter("전체", ""), null);
  assert.equal(recommendRadiusMeter("", ""), null);
  assert.equal(recommendRadiusMeter("전체", "전체"), null);
});