import os
import urllib.request
from io import BytesIO

from PIL import Image

OUT = os.path.join(
    os.path.dirname(__file__),
    "..",
    "ItemsAdder",
    "tfmc_cooking",
    "resourcepack",
    "assets",
    "tfmc_cooking",
    "textures",
    "item",
    "vanilla",
    "potato_rotten.png",
)
URL = (
    "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/"
    "1.21.1/assets/minecraft/textures/item/potato.png"
)


def make_rotten(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA").resize((16, 16), Image.NEAREST)
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
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with urllib.request.urlopen(URL, timeout=30) as resp:
        data = resp.read()
    make_rotten(Image.open(BytesIO(data))).save(OUT)
    print("wrote", OUT)


if __name__ == "__main__":
    main()
