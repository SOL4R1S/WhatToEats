import { defineConfig, devices } from "@playwright/test";

/**
 * WhatToEat E2E 테스트 설정
 * - 운영 환경(https://whattoeats.app) 대상
 * - 테스트 전용 계정: e2e_tester_01@test.com / e2eTest1234!
 * - 운영 DB에 영향을 주는 쓰기 테스트는 최소화
 */
export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  workers: 1,
  retries: 1,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "https://whattoeats.app",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
