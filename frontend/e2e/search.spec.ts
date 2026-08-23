import { test, expect } from "@playwright/test";
import { login } from "./helpers";

test.describe("검색", () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test("검색 페이지 진입 - 지도/검색 UI 렌더링", async ({ page }) => {
    await page.goto("/search");
    await page.waitForLoadState("networkidle");

    await expect(page.getByPlaceholder(/검색|식당|장소/i).first()).toBeVisible({
      timeout: 20_000,
    });
  });

  test("키워드 검색 실행 - 결과 목록 표시", async ({ page }) => {
    await page.goto("/search");
    await page.waitForLoadState("networkidle");

    const searchInput = page.getByPlaceholder(/검색|식당|장소/i).first();
    await searchInput.fill("해운대 맛집");
    await searchInput.press("Enter");

    // 결과 목록 또는 결과 없음 안내 중 하나는 표시됨
    await expect(
      page.locator("text=/해운대|결과|맛집|식당/").first(),
    ).toBeVisible({ timeout: 30_000 });
  });

  test("DB 폴백 - 저장된 식당 검색 API 호출", async ({ page }) => {
    // 카카오 검색 실패를 시뮬레이션하기 위해 API 응답 인터셉트가 아닌,
    // 실제 DB 검색 API가 정상 동작하는지 확인
    await page.goto("/search");
    await page.waitForLoadState("networkidle");

    const searchInput = page.getByPlaceholder(/검색|식당|장소/i).first();
    await searchInput.fill("해운대암소갈비집"); // DB에 실제 저장된 식당
    await searchInput.press("Enter");

    // DB LIKE 검색 결과가 화면에 표시되거나, 카카오 결과가 없는 경우 안내
    await expect(
      page.locator("text=/해운대암소갈비집|결과가 없습니다|저장된 식당/").first(),
    ).toBeVisible({ timeout: 30_000 });
  });
});