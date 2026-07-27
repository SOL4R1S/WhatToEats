#!/usr/bin/env python3
"""
GeoJSON 형식의 행정동 경계 데이터를 4단계 regions.json 구조로 변환합니다.

데이터 출처: https://github.com/vuski/admdongkor (CC BY 4.0)
사용법:
    python convert_geojson_regions.py HangJeongDong_ver20260701.geojson regions.json
"""

import json
import re
import sys
from collections import defaultdict


def normalize_sido(sido: str) -> str:
    """카카오맵 주소 형식과 맞춰 시도명을 축약합니다."""
    mapping = {
        "서울특별시": "서울",
        "부산광역시": "부산",
        "대구광역시": "대구",
        "인천광역시": "인천",
        "광주광역시": "광주",
        "대전광역시": "대전",
        "울산광역시": "울산",
        "세종특별자치시": "세종",
        "경기도": "경기",
        "강원특별자치도": "강원",
        "충청북도": "충북",
        "충청남도": "충남",
        "전라북도": "전북",
        "전북특별자치도": "전북",
        "전라남도": "전남",
        "경상북도": "경북",
        "경상남도": "경남",
        "제주특별자치도": "제주",
    }
    return mapping.get(sido, sido)


def parse_sigungu(sigungu: str):
    """
    시군구명을 '시/군'과 '구'로 분리합니다.
    예) '수원시 장안구'  -> ('수원시', '장안구')
        '수원시장안구'    -> ('수원시', '장안구')
        '성남시중원구'    -> ('성남시', '중원구')
        '창원시마산합포구' -> ('창원시', '마산합포구')
        '종로구'          -> ('종로구', None)
    """
    if not sigungu:
        return None, None

    # 공백으로 분리된 경우: "수원시 장안구"
    parts = sigungu.split()
    if len(parts) >= 2 and parts[-1].endswith("구"):
        city = " ".join(parts[:-1])
        district = parts[-1]
        return city, district

    # 붙어있는 경우: "수원시장안구", "성남시중원구"
    match = re.match(r"^(.+?시)(.+구)$", sigungu)
    if match:
        return match.group(1), match.group(2)

    return sigungu, None


def extract_from_geojson(geojson_path: str):
    """
    GeoJSON features에서 adm_nm 속성을 추출하여 4단계 트리를 구성합니다.
    """
    tree = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))

    with open(geojson_path, encoding="utf-8") as f:
        data = json.load(f)

    for feature in data.get("features", []):
        props = feature.get("properties", {})
        adm_nm = props.get("adm_nm", "").strip()
        if not adm_nm:
            continue

        parts = adm_nm.split()
        if len(parts) < 2:
            continue

        sido = normalize_sido(parts[0])
        dong = parts[-1]
        sigungu = " ".join(parts[1:-1])

        city, district = parse_sigungu(sigungu)
        if not city:
            continue

        if district:
            # 4단계: sido -> city -> district -> dong
            if dong not in tree[sido][city][district]:
                tree[sido][city][district].append(dong)
        else:
            # 3단계: sido -> city -> '전체' -> dong
            if dong not in tree[sido][city]["전체"]:
                tree[sido][city]["전체"].append(dong)

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
                # 3단계 구조
                dongs = sorted(districts["전체"])
                result[sido][city] = ["전체"] + dongs
            else:
                # 4단계 구조
                result[sido][city] = {}
                for district in sorted(districts.keys()):
                    dongs = sorted(districts[district])
                    result[sido][city][district] = ["전체"] + dongs

    return result


def main():
    if len(sys.argv) != 3:
        print("Usage: python convert_geojson_regions.py <input.geojson> <output.json>")
        print("  input.geojson : vuski/admdongkor GeoJSON 파일")
        print("  output.json   : 변환된 regions.json")
        sys.exit(1)

    geojson_path = sys.argv[1]
    output_path = sys.argv[2]

    tree = extract_from_geojson(geojson_path)
    result = build_json(tree)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"변환 완료: {output_path}")
    print(f"시도 개수: {len(result)}")
    for sido, cities in result.items():
        print(f"  {sido}: {len(cities)}개 시/군/구")


if __name__ == "__main__":
    main()
