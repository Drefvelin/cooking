import os

from PIL import Image

ROOT = os.path.join(
    os.path.dirname(__file__),
    "..",
    "ItemsAdder",
    "tfmc_cooking",
    "resourcepack",
    "assets",
    "tfmc_cooking",
    "textures",
    "item",
)

PAIRS = [
    ("intermediate", "yeast.png", "yeast_rotten.png"),
    ("intermediate", "dough.png", "dough_rotten.png"),
    ("bulk", "yeast.png", "yeast_rotten.png"),
    ("bulk", "dough.png", "dough_rotten.png"),
    ("intermediate", "flour.png", "flour_rotten.png"),
    ("bulk", "flour.png", "flour_rotten.png"),
]


def make_rotten(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size)
    px = img.load()
    opx = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                opx[x, y] = (0, 0, 0, 0)
                continue
            gray = 0.299 * r + 0.587 * g + 0.114 * b
            nr = int(r * 0.35 + gray * 0.45 + 28)
            ng = int(g * 0.30 + gray * 0.40 + 32)
            nb = int(b * 0.25 + gray * 0.30 + 18)
            opx[x, y] = (
                max(0, min(255, nr)),
                max(0, min(255, ng)),
                max(0, min(255, nb)),
                a,
            )
    return out


def main() -> None:
    for folder, src_name, dest_name in PAIRS:
        src = os.path.join(ROOT, folder, src_name)
        dest = os.path.join(ROOT, folder, dest_name)
        make_rotten(Image.open(src)).save(dest)
        print("wrote", dest)


if __name__ == "__main__":
    main()
