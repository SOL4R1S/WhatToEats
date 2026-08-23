import { expect, Page } from "@playwright/test";

/**
 * E2E 공통 헬퍼
 * - 테스트 전용 계정으로 로그인
 * - 운영 환경(whattoeats.app) 대상
 */

export const TEST_USER = {
  loginId: "e2e_tester_01@test.com",
  password: "e2eTest1234!",
  nickname: "E2E테스터",
};

/** 로그인 페이지에서 테스트 계정으로 로그인 */
export async function login(page: Page): Promise<void> {
  await page.goto("/login");
  await page.waitForLoadState("networkidle");

  // 이메일 로그인 탭 클릭 (기본 탭은 카카오)
  await page.getByRole("button", { name: "이메일 로그인" }).click();

  // 입력 필드는 placeholder로 식별
  await page.getByPlaceholder("email@example.com").fill(TEST_USER.loginId);
  await page.getByPlaceholder("••••••••").first().fill(TEST_USER.password);

  // 제출 (로그인 모드)
  await page.getByRole("button", { name: "로그인", exact: true }).click();

  // 로그인 성공 = /feed로 이동 (handleLoginSuccess가 router.push("/feed"))
  await expect(page).toHaveURL(/\/feed/, { timeout: 20_000 });
}