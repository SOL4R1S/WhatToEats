/* eslint-disable @typescript-eslint/no-require-imports */
const puppeteer = require('puppeteer');
const path = require('path');

const CHROME_PATH = '/Users/solaris/.cache/puppeteer/chrome/mac_arm-149.0.7827.22/chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing';
const TARGET_DIR = '/Volumes/nv6000t/project/WhatToEat/기능정의서img';

(async () => {
  console.log('브라우저 실행 중...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });

  // 1. 카카오 로그인 전 비회원 상태의 홈 화면 캡처
  console.log('비회원 홈 화면 캡처 중...');
  await page.goto('http://localhost:3000/', { waitUntil: 'networkidle2', timeout: 10000 });
  // 명시적으로 로그아웃/초기화 상태 보장
  await page.evaluate(() => localStorage.removeItem('isLoggedIn'));
  await page.reload({ waitUntil: 'networkidle2' });
  await page.screenshot({ path: path.join(TARGET_DIR, '스크린샷_2026-06-18_04.18.47_original.png') });

  // 2. 가상 카카오 로그인 페이지 캡처 및 로그인 액션 수행 (F-101)
  console.log('로그인 페이지 캡처 및 로그인 수행 중...');
  await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle2', timeout: 10000 });
  // 입력 필드에 임시 값 타이핑하여 리얼리티 확보
  await page.type('input[type="email"]', 'foodie@kakao.com');
  await page.type('input[type="password"]', 'password123');
  // 입력 완료된 가상 로그인 화면 스크린샷 저장
  await page.screenshot({ path: path.join(TARGET_DIR, '스크린샷_로그인페이지.png') });
  
  // 로그인 버튼 클릭 시뮬레이션
  await page.click('button[type="submit"]');
  // 로그인 완료 후 홈 화면으로 리다이렉트 및 헤더 하이드레이션 완료 대기
  await page.waitForFunction(() => document.body.innerText.includes('오늘의푸디 님'), { timeout: 5000 });
  
  // 3. 카카오 로그인 완료된 회원 상태의 홈 화면 캡처
  console.log('회원 홈 화면 캡처 중...');
  await page.screenshot({ path: path.join(TARGET_DIR, '스크린샷_2026-06-18_04.18.47.png') });

  // Helper 함수: 페이지 이동 후 로그인 세션 하이드레이션(오늘의푸디 님 문구 노출) 완료 시까지 대기하고 캡처
  const gotoAndCapture = async (url, filename, customAction = null) => {
    console.log(`페이지 접속 및 하이드레이션 대기: ${url} -> ${filename}`);
    await page.goto(url, { waitUntil: 'networkidle2', timeout: 10000 });
    
    // 세션이 풀리는 걸 방지하기 위해 각 페이지 이동 시 로컬 스토리지 강제 설정 후 렌더링 대기
    await page.evaluate(() => localStorage.setItem('isLoggedIn', 'true'));
    
    // 헤더에 '오늘의푸디 님' 닉네임이 나타날 때까지 대기 (하이드레이션 대기)
    await page.waitForFunction(() => document.body.innerText.includes('오늘의푸디 님'), { timeout: 5000 }).catch(() => {
      console.log('주의: 하이드레이션 대기 시간 제한 초과');
    });

    if (customAction) {
      await customAction(page);
    }

    await page.screenshot({ path: path.join(TARGET_DIR, filename) });
    console.log(`저장 성공: ${filename}`);
  };

  // -------------------------------------------------------------
  // 로그인 세션이 유지된 상태로 나머지 기능 페이지들 캡처 시작 (헤더 프로필 유지 보장)
  // -------------------------------------------------------------

  // 5. 식당 검색 실제 구현 (지도 반할 분할 뷰)
  await gotoAndCapture('http://localhost:3000/search', '스크린샷_2026-06-18_04.19.03.png');

  // 6. 식당 상세 & 리뷰 관련 개별 캡처 (F-301, F-302, F-303)
  // F-301 리뷰 등록 (작성 폼 모달 오픈)
  await gotoAndCapture('http://localhost:3000/restaurant/1?action=create', '스크린샷_리뷰등록_F301.png');
  
  // F-302 리뷰 목록 조회
  await gotoAndCapture('http://localhost:3000/restaurant/1', '스크린샷_리뷰목록_F302.png');
  
  // F-303 리뷰 수정/삭제 (수정 모달 오픈)
  await gotoAndCapture('http://localhost:3000/restaurant/1?action=edit', '스크린샷_리뷰수정삭제_F303.png');

  // 7. 추천 페이지 (F-701)
  await gotoAndCapture('http://localhost:3000/recommend', '스크린샷_2026-06-18_04.19.59.png');

  // 8. 맛집 리스트 관련 개별 캡처 (F-401, F-402)
  // F-401 맛집 리스트 생성 모달
  await gotoAndCapture('http://localhost:3000/lists?action=create', '스크린샷_리스트생성_F401.png');

  // F-402 리스트 내 식당 구성 및 매핑 편집 화면
  await gotoAndCapture('http://localhost:3000/lists?action=edit', '스크린샷_리스트편집_F402.png');

  // 9. 피드 페이지 (F-601)
  await gotoAndCapture('http://localhost:3000/feed', '스크린샷_2026-06-18_04.20.21.png');

  // 10. 프로필 & 팔로우 관련 개별 캡처 (F-501)
  // F-501 팔로우 하지 않은 상태 (주황색 팔로우 버튼)
  await gotoAndCapture('http://localhost:3000/profile?follow=false', '스크린샷_팔로우안함_F501.png');
  // F-501 팔로우 완료한 상태 (회색 팔로잉 버튼)
  await gotoAndCapture('http://localhost:3000/profile?follow=true', '스크린샷_팔로우완료_F501.png');

  // 11. 프로필 페이지 내 팔로잉 모달 팝업 상태 캡처 (F-502)
  await gotoAndCapture('http://localhost:3000/profile?follow=true', '스크린샷_팔로잉모달_F502.png', async (page) => {
    const buttons = await page.$$('button');
    for (const btn of buttons) {
      const text = await page.evaluate(el => el.textContent, btn);
      if (text.includes('팔로잉')) {
        await btn.click();
        await page.waitForSelector('h2', { timeout: 2000 });
        break;
      }
    }
  });

  await browser.close();
  console.log('모든 로그인 연동 기반 스크린샷 작업 완료!');
})();
