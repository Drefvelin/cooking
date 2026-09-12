"""Generate sausage_cooked/rotten/burnt.png from sausage_raw using pork_steak color deltas."""
import os

from PIL import Image

BASE = os.path.join(
    os.path.dirname(__file__),
    "..",
    "ItemsAdder",
    "tfmc_cooking",
    "resourcepack",
    "assets",
    "tfmc_cooking",
    "textures",
    "item",
    "ingredients",
)

SAUSAGE_DIR = os.path.join(BASE, "sausage")
PORK_DIR = os.path.join(BASE, "pork")

STATES = ("cooked", "rotten", "burnt")


def load_rgba(path: str) -> Image.Image:
    return Image.open(path).convert("RGBA")


def channel_delta(src: Image.Image, dst: Image.Image) -> tuple[float, float, float, float]:
    """Mean per-channel offset from src to dst on opaque pixels."""
    spx = src.load()
    dpx = dst.load()
    sums = [0.0, 0.0, 0.0, 0.0]
    count = 0
    for y in range(src.height):
        for x in range(src.width):
            sr, sg, sb, sa = spx[x, y]
            if sa < 8:
                continue
            dr, dg, db, da = dpx[x, y]
            if da < 8:
                continue
            sums[0] += dr - sr
            sums[1] += dg - sg
            sums[2] += db - sb
            sums[3] += da - sa
            count += 1
    if count == 0:
        return (0.0, 0.0, 0.0, 0.0)
    return tuple(v / count for v in sums)


def apply_delta(img: Image.Image, delta: tuple[float, float, float, float]) -> Image.Image:
    out = Image.new("RGBA", img.size)
    px = img.load()
    opx = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                opx[x, y] = (0, 0, 0, 0)
                continue
            nr = int(max(0, min(255, r + delta[0])))
            ng = int(max(0, min(255, g + delta[1])))
            nb = int(max(0, min(255, b + delta[2])))
            na = int(max(0, min(255, a + delta[3])))
            opx[x, y] = (nr, ng, nb, na)
    return out


def main() -> None:
    sausage_raw_path = os.path.join(SAUSAGE_DIR, "sausage_raw.png")
    pork_raw_path = os.path.join(PORK_DIR, "pork_steak_raw.png")
    if not os.path.isfile(sausage_raw_path):
        raise SystemExit(f"Missing {sausage_raw_path}")
    if not os.path.isfile(pork_raw_path):
        raise SystemExit(f"Missing {pork_raw_path}")

    sausage_raw = load_rgba(sausage_raw_path)
    pork_raw = load_rgba(pork_raw_path)

    os.makedirs(SAUSAGE_DIR, exist_ok=True)

    for state in STATES:
        pork_state_path = os.path.join(PORK_DIR, f"pork_steak_{state}.png")
        if not os.path.isfile(pork_state_path):
            raise SystemExit(f"Missing {pork_state_path}")
        pork_state = load_rgba(pork_state_path)
        delta = channel_delta(pork_raw, pork_state)
        out = apply_delta(sausage_raw, delta)
        out_path = os.path.join(SAUSAGE_DIR, f"sausage_{state}.png")
        out.save(out_path)
        print("wrote", out_path, "delta", tuple(round(d, 2) for d in delta))


if __name__ == "__main__":
    main()
