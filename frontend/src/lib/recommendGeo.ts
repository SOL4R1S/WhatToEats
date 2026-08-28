/**
 * 추천 검색에서 선택 지역의 세부 수준에 따라 적용할
 * 검색 반경/검증 임계값(미터)을 반환한다.
 *
 * - 시/도 전체: null — 반경 제한 없음. 좌표 없이 지역명 텍스트 검색.
 * - 구/군/시 전체: null — 위와 동일. 텍스트 검색이 지역을 정확히 잡는다.
 * - 동/가(도심): 1,500m — 도보 생활권. 행정동은 보통 수백 m~1km 크기.
 * - 읍: 5,000m — 시골 중형 행정구역.
 * - 면: 8,000m — 시골 대형 행정구역 (면 하나가 수십 km² 단위인 곳이 많다).
 *
 * 검색 시에는 카카오 keywordSearch의 radius로, 추천 API에는
 * maxDistanceMeter로 동일 값을 보내 서버가 한 번 더 검증한다.
 * null이면 좌표 기반 반경을 아예 사용하지 않는다.
 */
export function recommendRadiusMeter(
  region2: string,
  region3: string,
): number | null {
  if (!region2 || region2 === "전체") return null;
  if (!region3 || region3 === "전체") return null;
  if (region3.endsWith("면")) return 8000;
  if (region3.endsWith("읍")) return 5000;
  return 1500;
}