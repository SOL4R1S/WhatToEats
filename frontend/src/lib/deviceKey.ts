/**
 * 기기 고유 익명 키를 생성·유지한다 (그린메일: 이메일/비번 수집 없음).
 *
 * 구형 브라우저/웹뷰(WebView)에서는 crypto.randomUUID가 없고,
 * 프라이빗 모드·스토리지 차단 환경에서는 localStorage 접근이 실패할 수 있다.
 * 어느 경우에도 예외를 던지지 않고 키를 반환해야 로그인 버튼이 멈추지 않는다.
 */
const DEVICE_KEY_STORAGE = "whattoeat.deviceKey";

/** crypto.randomUUID 미지원 대비 폴백: 36진수 난수 연결 */
function fallbackUuid(length: number): string {
  let s = "";
  while (s.length < length) {
    s += Math.random().toString(36).slice(2);
  }
  return s.slice(0, length);
}

function generateDeviceKey(): string {
  return typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
    ? crypto.randomUUID() + crypto.randomUUID()
    : fallbackUuid(72);
}

/**
 * 백엔드 검증 규칙(deviceKey: 기기 키는 16~128자여야 합니다)과 동일해야 한다.
 * 과거 버전이 저장한 규격 밖 키는 재사용하지 않고 새로 발급한다.
 */
function isValidKey(key: string | null): key is string {
  return !!key && key.length >= 16 && key.length <= 128;
}

export function getOrCreateDeviceKey(): string {
  let stored: string | null = null;
  try {
    stored = window.localStorage.getItem(DEVICE_KEY_STORAGE);
  } catch {
    // localStorage 접근 차단 — 아래에서 메모리 키 발급으로 넘어간다
  }

  if (isValidKey(stored)) return stored;

  const key = generateDeviceKey();

  try {
    window.localStorage.setItem(DEVICE_KEY_STORAGE, key);
  } catch {
    // 스토리지 차단(프라이빗 모드 등) — 저장은 생략하고 이번 세션 키로만 동작
  }

  return key;
}