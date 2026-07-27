#!/usr/bin/env python3
"""
행정동코드 CSV를 4단계 regions.json 구조로 변환합니다.

다운로드: 행정안전부 행정표준코드관리시스템 (www.code.go.kr)
예상 컬럼: 행정동코드, 시도명, 시군구명, 읍멸동명, ...

사용법:
    python convert_regions.py 행정동코드.csv regions.json
"""

import csv
import json
import sys
from collections import defaultdict


def parse_sigungu(sigungu: str):
    """
    시군구명을 '시/군'과 '구'로 분리합니다.
    예) '수원시 장안구' -> ('수원시', '장안구')
        '종로구'        -> ('종로구', None)
        '세종특별자치시' -> ('세종특별자치시', None)
    """
    if not sigungu:
        return None, None

    parts = sigungu.split()
    if len(parts) >= 2 and parts[-1].endswith("구"):
        city = " ".join(parts[:-1])
        district = parts[-1]
        return city, district

    return sigungu, None


def read_csv(csv_path: str):
    """
    CSV에서 4단계 트리를 구성합니다.
    구조: region1 -> region2 -> region3 -> [region4, ...]
    - 3단계 지역(서울 등): region3 = '전체', region4 리스트에 동 저장
    - 4단계 지역(경기 수원시 등): region3 = 구, region4 리스트에 동 저장
    """
    tree = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))

    with open(csv_path, encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            sido = (row.get("시도명") or "").strip()
            sigungu = (row.get("시군구명") or "").strip()
            emd = (row.get("읍멸동명") or "").strip()

            # 폐지된 행정동 제외 (삭제일자 컬럼이 있다면)
            if row.get("삭제일자") and row["삭제일자"].strip():
                continue

            if not sido or not sigungu or not emd:
                continue

            city, district = parse_sigungu(sigungu)
            if not city:
                continue

            if district:
                # 4단계: sido -> city -> district -> dong
                if emd not in tree[sido][city][district]:
                    tree[sido][city][district].append(emd)
            else:
                # 3단계: sido -> city -> '전체' -> dong
                if emd not in tree[sido][city]["전체"]:
                    tree[sido][city]["전체"].append(emd)

    return tree


def build_json(tree):
    """
    트리를 최종 regions.json 형태로 정렬/변환합니다.
    """
    result = {}

    for sido in sorted(tree.keys()):
        result[sido] = {}

        for city in sorted(tree[sido].keys()):
            districts = tree[sido][city]

            if "전체" in districts:
                # 3단계 구조: sido -> city -> [동, ...]
                dongs = sorted(districts["전체"])
                result[sido][city] = ["전체"] + dongs
            else:
                # 4단계 구조: sido -> city -> district -> [동, ...]
                result[sido][city] = {}
                for district in sorted(districts.keys()):
                    dongs = sorted(districts[district])
                    result[sido][city][district] = ["전체"] + dongs

    return result


def main():
    if len(sys.argv) != 3:
        print("Usage: python convert_regions.py <input.csv> <output.json>")
        print("  input.csv : 행정동코드 CSV (컬럼: 시도명, 시군구명, 읍멸동명)")
        print("  output.json: 변환된 regions.json")
        sys.exit(1)

    csv_path = sys.argv[1]
    output_path = sys.argv[2]

    tree = read_csv(csv_path)
    result = build_json(tree)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"변환 완료: {output_path}")
    print(f"시도 개수: {len(result)}")


if __name__ == "__main__":
    main()
