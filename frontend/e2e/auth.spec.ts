import { test, expect } from "@playwright/test";
import { login, TEST_USER } from "./helpers";

test.describe("인증", () => {
  test("로그인 페이지 진입 - UI 렌더링", async ({ page }) => {
    await page.goto("/login");
    await page.waitForLoadState("networkidle");

    await expect(page.getByRole("button", { name: "카카오 로그인" })).toBeVisible({
      timeout: 15_000,
    });
    await expect(page.getByRole("button", { name: "이메일 로그인" })).toBeVisible();
  });

  test("테스트 계정으로 로그인 성공", async ({ page }) => {
    await login(page);
    // handleLoginSuccess가 /feed로 push
    await expect(page).toHaveURL(/\/feed/, { timeout: 20_000 });
  });

  test("잘못된 비밀번호 - 로그인 실패", async ({ page }) => {
    await page.goto("/login");
    await page.waitForLoadState("networkidle");

    await page.getByRole("button", { name: "이메일 로그인" }).click();
    await page.getByPlaceholder("email@example.com").fill(TEST_USER.loginId);
    await page.getByPlaceholder("••••••••").first().fill("wrong-password!");
    await page.getByRole("button", { name: "로그인", exact: true }).click();

    // alert 다이얼로그 처리 (login page는 alert(message) 사용)
    page.once("dialog", async (dialog) => {
      expect(dialog.message()).not.toBe("");
      await dialog.accept();
    });

    // /feed로 이동하지 않아야 함
    await expect(page).not.toHaveURL(/\/feed/, { timeout: 15_000 });
  });
});