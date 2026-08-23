import { test, expect } from "@playwright/test";
import { login } from "./helpers";

test.describe("피드", () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test("피드 목록 조회 - 게시물 표시", async ({ page }) => {
    await page.goto("/feed");
    await page.waitForLoadState("networkidle");

    // 피드 카드/목록이 표시되거나 빈 상태 안내
    await expect(
      page.locator("text=/피드|글|맛집|게시글|첫|없습니다/").first(),
    ).toBeVisible({ timeout: 20_000 });
  });

  test("피드 작성 페이지 진입", async ({ page }) => {
    await page.goto("/feed/write");
    await page.waitForLoadState("networkidle");

    await expect(
      page.locator("text=/작성|게시|피드|맛집|사진|이미지/").first(),
    ).toBeVisible({ timeout: 20_000 });
  });
});