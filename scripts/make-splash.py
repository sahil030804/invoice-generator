#!/usr/bin/env python3
"""
Generates the splash wordmark (run from repo root, needs Pillow). The logo itself is vector,
see scripts/make-logo.py.

  splash_branding.png  800x320  (200x80dp @ xxxhdpi): "Parchi" wordmark
"""
from PIL import Image, ImageDraw, ImageFont

RES = "app/src/main/res"
OUT = f"{RES}/drawable-xxxhdpi"
FONT_DIR = f"{RES}/font"

BRAND_W, BRAND_H = 800, 320
APP_NAME = "Parchi"


def make_branding():
    img = Image.new("RGBA", (BRAND_W, BRAND_H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    title = ImageFont.truetype(f"{FONT_DIR}/notosans_semibold.ttf", 112)

    def centered(text, font, y, fill):
        w = draw.textlength(text, font=font)
        draw.text(((BRAND_W - w) / 2, y), text, font=font, fill=fill)

    # Minimal: just the name, slightly translucent so the animated logo stays the hero.
    centered(APP_NAME, title, 90, (255, 255, 255, 230))
    img.save(f"{OUT}/splash_branding.png", optimize=True)


if __name__ == "__main__":
    import os
    os.makedirs(OUT, exist_ok=True)
    make_branding()
    print("wrote", OUT)
