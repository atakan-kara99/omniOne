#!/usr/bin/env python3

import io
import json
import shutil
import tarfile
import urllib.request
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "backend" / "src" / "main" / "resources" / "reference"
CITIES_DIR = OUTPUT_DIR / "cities"
NPM_TARBALL_URL = "https://registry.npmjs.org/country-state-city/-/country-state-city-3.2.1.tgz"
COUNTRY_ASSET_PATH = "package/lib/assets/country.json"
CITY_ASSET_PATH = "package/lib/assets/city.json"


def download_package() -> bytes:
    with urllib.request.urlopen(NPM_TARBALL_URL) as response:
        return response.read()


def extract_json(package_bytes: bytes, member_name: str):
    with tarfile.open(fileobj=io.BytesIO(package_bytes), mode="r:gz") as archive:
        member = archive.getmember(member_name)
        extracted = archive.extractfile(member)
        if extracted is None:
            raise RuntimeError(f"Failed to extract {member_name}")
        return json.load(extracted)


def write_json(path: Path, payload) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build_reference_data() -> None:
    package_bytes = download_package()
    countries = extract_json(package_bytes, COUNTRY_ASSET_PATH)
    cities = extract_json(package_bytes, CITY_ASSET_PATH)

    if OUTPUT_DIR.exists():
        shutil.rmtree(OUTPUT_DIR)
    CITIES_DIR.mkdir(parents=True, exist_ok=True)

    reduced_countries = sorted(
        (
            {"code": country["isoCode"], "name": country["name"]}
            for country in countries
            if country.get("isoCode") and country.get("name")
        ),
        key=lambda item: item["name"],
    )
    write_json(OUTPUT_DIR / "countries.json", reduced_countries)

    cities_by_country: dict[str, set[str]] = {}
    for city_name, country_code, *_ in cities:
        if not city_name or not country_code:
            continue
        cities_by_country.setdefault(country_code, set()).add(city_name)

    for country_code, names in cities_by_country.items():
        write_json(CITIES_DIR / f"{country_code}.json", sorted(names))


if __name__ == "__main__":
    build_reference_data()
