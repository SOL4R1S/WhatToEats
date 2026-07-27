#!/bin/sh

echo "🔧 Git hooks 설정 중..."
git config core.hooksPath .githooks
echo "✅ 완료 — .githooks 디렉토리가 Git hooks 경로로 설정되었습니다."
