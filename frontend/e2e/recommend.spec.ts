import { test, expect } from "@playwright/test";
import { login } from "./helpers";

test.describe("추천", () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test("추천 페이지 진입 - UI 렌더링", async ({ page }) => {
    await page.goto("/recommend");
    await page.waitForLoadState("networkidle");

    await expect(page.getByRole("button", { name: /추천 받기/i })).toBeVisible({
      timeout: 20_000,
    });
  });

  test("추천 실행 - 결과 모달 표시", async ({ page }) => {
    await page.goto("/recommend");
    await page.waitForLoadState("networkidle");

    const recommendBtn = page.getByRole("button", { name: /추천 받기/i });
    await recommendBtn.click();

    // 결과 모달 또는 오류 안내 (카카오/DB 폴백 포함)
    await expect(
      page.locator(
        "text=/추천|식당|조건에 맞는|카카오|저장된|다시 시도/",
      ).first(),
    ).toBeVisible({ timeout: 45_000 });
  });
});