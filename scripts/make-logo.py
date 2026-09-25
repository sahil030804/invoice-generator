#!/usr/bin/env python3
"""
Single source of truth for the Parchi logo. Writes every logo asset from the same shapes:

  branding/parchi-icon.svg                    master SVG (icon on indigo tile)
  branding/parchi-mark.svg                    mark only, transparent (for print/web)
  res/drawable/ic_launcher_foreground.xml     adaptive-icon foreground (108dp)
  res/drawable/parchi_logo.xml                in-app logo (tile + mark)
  res/drawable/splash_logo.xml                static splash vector for Android 8-11 (288dp)
  res/drawable/splash_logo_anim_base.xml      same, animated parts hidden (AVD start frame)
  res/drawable/splash_logo_animated.xml       Android 12+ animated splash (≤1000ms)

Concept: a receipt slip ("parchi") with a torn zig-zag edge, sealed by a saffron tick badge.
Run from repo root:  python3 scripts/make-logo.py
"""

RES = "app/src/main/res"

INDIGO = "#3730A3"
INDIGO_DEEP = "#1E1B4B"
SLIP = "#FFFFFF"
LINE = "#C7C4FF"
SAFFRON = "#F59E0B"

# ---- geometry in the 108-unit adaptive-icon grid (safe zone 21..87) ----
SLIP_LEFT, SLIP_RIGHT, SLIP_TOP, SLIP_BOTTOM = 31, 67, 26, 80
CORNER = 4
TOOTH = 3  # zig-zag tooth half-width and depth


def slip_path():
    d = [f"M{SLIP_LEFT + CORNER},{SLIP_TOP}", f"H{SLIP_RIGHT - CORNER}",
         f"A{CORNER},{CORNER} 0 0 1 {SLIP_RIGHT},{SLIP_TOP + CORNER}", f"V{SLIP_BOTTOM}"]
    x, up = SLIP_RIGHT, True
    while x > SLIP_LEFT:
        x -= TOOTH
        d.append(f"L{x},{SLIP_BOTTOM - TOOTH if up else SLIP_BOTTOM}")
        up = not up
    d += [f"V{SLIP_TOP + CORNER}", f"A{CORNER},{CORNER} 0 0 1 {SLIP_LEFT + CORNER},{SLIP_TOP}", "Z"]
    return " ".join(d)


SLIP_D = slip_path()
LINE1_D = "M37,36 H59"
LINE2_D = "M37,44 H52"
LINE_WIDTH = 3.2
BADGE_CX, BADGE_CY, BADGE_R = 67, 68, 10
BADGE_D = (f"M{BADGE_CX - BADGE_R},{BADGE_CY} a{BADGE_R},{BADGE_R} 0 1 0 {2 * BADGE_R},0 "
           f"a{BADGE_R},{BADGE_R} 0 1 0 {-2 * BADGE_R},0 Z")
BADGE_RING = 2.5
TICK_D = f"M{BADGE_CX - 4.6},{BADGE_CY + 0.2} L{BADGE_CX - 1.4},{BADGE_CY + 3.4} L{BADGE_CX + 4.8},{BADGE_CY - 3.1}"
TICK_WIDTH = 3.0


# Launchers show roughly the centre 72 of the 108 grid; the standalone tile uses the same crop.
TILE_MIN, TILE, TILE_RADIUS = 18, 72, 16

# ---------------------------------------------------------------- SVG
# Mark scaled about the grid centre so its farthest corner stays inside the 66-unit safe circle
# (radius 33) that circular launcher masks keep.
MARK_SCALE = 0.86
GRID_CENTER = 54


def svg_mark_elements():
    return f"""  <g transform="translate({GRID_CENTER} {GRID_CENTER}) scale({MARK_SCALE}) translate({-GRID_CENTER} {-GRID_CENTER})">
  <path d="{SLIP_D}" fill="{SLIP}"/>
  <path d="{LINE1_D}" stroke="{LINE}" stroke-width="{LINE_WIDTH}" stroke-linecap="round"/>
  <path d="{LINE2_D}" stroke="{LINE}" stroke-width="{LINE_WIDTH}" stroke-linecap="round"/>
  <path d="{BADGE_D}" fill="{SAFFRON}" stroke="{INDIGO}" stroke-width="{BADGE_RING}"/>
  <path d="{TICK_D}" fill="none" stroke="{INDIGO_DEEP}" stroke-width="{TICK_WIDTH}" stroke-linecap="round" stroke-linejoin="round"/>
  </g>"""


def write_svgs():
    with open("branding/parchi-icon.svg", "w") as f:
        f.write(f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="{TILE_MIN} {TILE_MIN} {TILE} {TILE}" width="512" height="512">
  <title>Parchi</title>
  <rect x="{TILE_MIN}" y="{TILE_MIN}" width="{TILE}" height="{TILE}" rx="{TILE_RADIUS}" fill="{INDIGO}"/>
{svg_mark_elements()}
</svg>
""")
    with open("branding/parchi-mark.svg", "w") as f:
        f.write(f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="24 20 60 64" width="480" height="512">
  <title>Parchi mark</title>
{svg_mark_elements()}
</svg>
""")


# ---------------------------------------------------------------- Android vectors
def scaled_mark(indent="    "):
    return f"""{indent}<group android:pivotX="{GRID_CENTER}" android:pivotY="{GRID_CENTER}"
{indent}    android:scaleX="{MARK_SCALE}" android:scaleY="{MARK_SCALE}">
{vector_mark(indent=indent + "    ")}
{indent}</group>"""


def vector_mark(indent="    ", names=False, start_state=False):
    """
    Mark as <path>s in the 108 grid. `names` adds android:name for animation targets.
    `start_state` hides the animated parts (lines, badge, tick) - the AVD reveals them.
    """
    n = (lambda s: f' android:name="{s}"') if names else (lambda s: "")
    trim = ' android:trimPathEnd="0"' if start_state else ""
    badge_scale = ' android:scaleX="0" android:scaleY="0"' if start_state else ""
    i = indent
    return f"""{i}<group{n("slip")}>
{i}    <path{n("slipShape")} android:pathData="{SLIP_D}" android:fillColor="{SLIP}" />
{i}    <path{n("line1")}{trim} android:pathData="{LINE1_D}" android:strokeColor="{LINE}"
{i}        android:strokeWidth="{LINE_WIDTH}" android:strokeLineCap="round" />
{i}    <path{n("line2")}{trim} android:pathData="{LINE2_D}" android:strokeColor="{LINE}"
{i}        android:strokeWidth="{LINE_WIDTH}" android:strokeLineCap="round" />
{i}</group>
{i}<group{n("badge")} android:pivotX="{BADGE_CX}" android:pivotY="{BADGE_CY}"{badge_scale}>
{i}    <path android:pathData="{BADGE_D}" android:fillColor="{SAFFRON}"
{i}        android:strokeColor="{INDIGO}" android:strokeWidth="{BADGE_RING}" />
{i}    <path{n("tick")}{trim} android:pathData="{TICK_D}" android:strokeColor="{INDIGO_DEEP}"
{i}        android:strokeWidth="{TICK_WIDTH}" android:strokeLineCap="round" android:strokeLineJoin="round" />
{i}</group>"""


HEADER = "<!-- Generated by scripts/make-logo.py - edit the script, not this file. -->\n"


def write_vectors():
    with open(f"{RES}/drawable/ic_launcher_foreground.xml", "w") as f:
        f.write(f"""<?xml version="1.0" encoding="utf-8"?>
{HEADER}<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
{scaled_mark()}
</vector>
""")

    # In-app logo: the tile is the launcher-visible 72-unit centre, so the mark fills it like the home-screen icon.
    r, s = TILE_RADIUS, TILE
    with open(f"{RES}/drawable/parchi_logo.xml", "w") as f:
        f.write(f"""<?xml version="1.0" encoding="utf-8"?>
{HEADER}<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="72dp" android:height="72dp"
    android:viewportWidth="{s}" android:viewportHeight="{s}">
    <path android:pathData="M{r},0 H{s - r} A{r},{r} 0 0 1 {s},{r} V{s - r} A{r},{r} 0 0 1 {s - r},{s} H{r} A{r},{r} 0 0 1 0,{s - r} V{r} A{r},{r} 0 0 1 {r},0 Z"
        android:fillColor="{INDIGO}" />
    <group android:translateX="{-TILE_MIN}" android:translateY="{-TILE_MIN}">
{scaled_mark(indent="        ")}
    </group>
</vector>
""")

    # Splash: 288dp canvas, visible circle 192dp. Mark bbox ~46x54 in the 108 grid → scale 2.2.
    scale = 2.2
    mark_cx, mark_cy = (SLIP_LEFT + BADGE_CX + BADGE_R) / 2, (SLIP_TOP + SLIP_BOTTOM) / 2
    tx, ty = 144 - mark_cx * scale, 144 - mark_cy * scale
    def splash_vector(path, start_state):
        with open(path, "w") as f:
            f.write(f"""<?xml version="1.0" encoding="utf-8"?>
{HEADER}<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="288dp" android:height="288dp"
    android:viewportWidth="288" android:viewportHeight="288">
    <group android:translateX="{tx:.2f}" android:translateY="{ty:.2f}"
        android:scaleX="{scale}" android:scaleY="{scale}">
{vector_mark(indent="        ", names=True, start_state=start_state)}
    </group>
</vector>
""")

    splash_vector(f"{RES}/drawable/splash_logo.xml", start_state=False)            # Android 8-11 (static)
    splash_vector(f"{RES}/drawable/splash_logo_anim_base.xml", start_state=True)   # AVD start frame


# ---------------------------------------------------------------- animation
# Plain valueFrom/valueTo animators (keyframe animators played in reverse on a Samsung splash).
# With system animations off, Android jumps to the end values, so the full logo still shows.
# Timeline (ms): line1 100-400, line2 250-550, badge pops 450-750, tick draws 700-1000.
STEPS = [
    ("line1", [("trimPathEnd", 0, 1)], 100, 300, "@android:interpolator/fast_out_slow_in"),
    ("line2", [("trimPathEnd", 0, 1)], 250, 300, "@android:interpolator/fast_out_slow_in"),
    ("badge", [("scaleX", 0, 1), ("scaleY", 0, 1)], 450, 300, "@android:anim/overshoot_interpolator"),
    ("tick", [("trimPathEnd", 0, 1)], 700, 300, "@android:interpolator/fast_out_slow_in"),
]


def target(name, props, offset, duration, interpolator):
    animators = "\n".join(
        f"""                <objectAnimator android:propertyName="{prop}" android:valueType="floatType"
                    android:valueFrom="{v0}" android:valueTo="{v1}"
                    android:startOffset="{offset}" android:duration="{duration}"
                    android:interpolator="{interpolator}" />"""
        for prop, v0, v1 in props
    )
    return f"""    <target android:name="{name}">
        <aapt:attr name="android:animation">
            <set android:ordering="together">
{animators}
            </set>
        </aapt:attr>
    </target>"""


def write_animated():
    targets = [target(*step) for step in STEPS]
    with open(f"{RES}/drawable/splash_logo_animated.xml", "w") as f:
        f.write(f"""<?xml version="1.0" encoding="utf-8"?>
{HEADER}<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:drawable="@drawable/splash_logo_anim_base">
{chr(10).join(targets)}
</animated-vector>
""")


if __name__ == "__main__":
    import os
    os.makedirs("branding", exist_ok=True)
    write_svgs()
    write_vectors()
    write_animated()
    print("logo assets written")
