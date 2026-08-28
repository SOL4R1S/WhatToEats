import assert from "node:assert/strict";
import test from "node:test";

import { getOrCreateDeviceKey } from "./deviceKey.ts";

/**
 * 브라우저 전역(window.localStorage, crypto)을 스텁한다.
 * - storage가 null이면 localStorage 접근 자체가 throw한다.
 * - throwOnSet이면 setItem이 throw한다 (프라이빗 모드/용량 초과).
 */
function withBrowserEnv(
  storage: Map<string, string> | null,
  randomUUID?: () => string,
  throwOnSet = false,
): () => void {
  const g = globalThis as unknown as Record<string, unknown>;
  const originalWindow = g.window;
  const originalCrypto = Object.getOwnPropertyDescriptor(globalThis, "crypto");

  g.window = storage
    ? {
        localStorage: {
          getItem: (k: string) => (storage.has(k) ? storage.get(k)! : null),
          setItem: (k: string, v: string) => {
            if (throwOnSet) throw new DOMException("QuotaExceededError");
            storage.set(k, v);
          },
          removeItem: (k: string) => {
            storage.delete(k);
          },
        },
      }
    : {
        get localStorage(): never {
          throw new DOMException("blocked");
        },
      };

  // Node 19+의 globalThis.crypto는 getter 전용이라 직접 대입이 안 된다
  Object.defineProperty(globalThis, "crypto", {
    value: randomUUID ? { randomUUID } : {},
    configurable: true,
    writable: true,
  });

  return () => {
    g.window = originalWindow;
    if (originalCrypto) {
      Object.defineProperty(globalThis, "crypto", originalCrypto);
    } else {
      delete g.crypto;
    }
  };
}

test("빈 저장소면 72자 키를 생성해 저장한다", () => {
  const storage = new Map<string, string>();
  const restore = withBrowserEnv(storage, () => "a".repeat(36));

  try {
    const key = getOrCreateDeviceKey();
    assert.equal(key.length, 72);
    assert.equal(storage.get("whattoeat.deviceKey"), key);
  } finally {
    restore();
  }
});

test("유효한 기존 키(16~128자)는 그대로 재사용한다", () => {
  const existing = "b".repeat(72);
  const storage = new Map<string, string>([["whattoeat.deviceKey", existing]]);
  const restore = withBrowserEnv(storage, () => "c".repeat(36));

  try {
    assert.equal(getOrCreateDeviceKey(), existing);
  } finally {
    restore();
  }
});

test("규격 밖 기존 키(16자 미만)는 새로 발급한다 (백엔드 400 방지)", () => {
  const storage = new Map<string, string>([["whattoeat.deviceKey", "guest"]]);
  const restore = withBrowserEnv(storage, () => "d".repeat(36));

  try {
    const key = getOrCreateDeviceKey();
    assert.equal(key.length, 72);
    assert.equal(storage.get("whattoeat.deviceKey"), key);
  } finally {
    restore();
  }
});

test("crypto.randomUUID가 없어도(구형 브라우저/웹뷰) 키를 만든다", () => {
  const storage = new Map<string, string>();
  const restore = withBrowserEnv(storage); // randomUUID 없음

  try {
    const key = getOrCreateDeviceKey();
    assert.equal(key.length, 72);
    assert.equal(storage.get("whattoeat.deviceKey"), key);
  } finally {
    restore();
  }
});

test("localStorage.setItem이 실패해도(프라이빗 모드) 키는 반환한다", () => {
  const storage = new Map<string, string>();
  const restore = withBrowserEnv(storage, () => "e".repeat(36), true);

  try {
    const key = getOrCreateDeviceKey();
    assert.equal(key.length, 72);
    assert.equal(storage.has("whattoeat.deviceKey"), false); // 저장은 생략
  } finally {
    restore();
  }
});

test("localStorage 접근 자체가 실패해도 키는 반환한다", () => {
  const restore = withBrowserEnv(null); // getItem은 항상 throw

  try {
    const key = getOrCreateDeviceKey();
    assert.equal(key.length, 72);
  } finally {
    restore();
  }
});