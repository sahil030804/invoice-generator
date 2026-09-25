#!/usr/bin/env bash
# Downloads the bundled Noto Sans fonts (OFL) from a pinned commit and verifies checksums.
# Run from the repo root: ./scripts/fetch-fonts.sh
set -euo pipefail

COMMIT="28b15b4b43b7bed62b5cf6e6b0b5ff5846270535"
BASE="https://raw.githubusercontent.com/notofonts/notofonts.github.io/${COMMIT}/fonts/NotoSans/unhinted/ttf"
LICENSE_URL="https://raw.githubusercontent.com/google/fonts/main/ofl/notosans/OFL.txt"

FONT_DIR="app/src/main/res/font"
LICENSE_DIR="app/src/main/assets/licenses"
mkdir -p "$FONT_DIR" "$LICENSE_DIR"

for weight in Regular Medium SemiBold Bold; do
    lower=$(echo "$weight" | tr '[:upper:]' '[:lower:]')
    curl -fsSL --max-time 60 --retry 2 "${BASE}/NotoSans-${weight}.ttf" -o "${FONT_DIR}/notosans_${lower}.ttf"
done
curl -fsSL --max-time 60 --retry 2 "$LICENSE_URL" -o "${LICENSE_DIR}/NotoSans-OFL.txt"

sha256sum -c scripts/fonts.sha256
