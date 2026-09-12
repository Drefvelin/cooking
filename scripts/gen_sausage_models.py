"""Clone sausage model JSONs for cooked/rotten/burnt states (texture path swap only)."""
import json
import os

MODEL_DIR = os.path.join(
    os.path.dirname(__file__),
    "..",
    "ItemsAdder",
    "tfmc_cooking",
    "resourcepack",
    "assets",
    "tfmc_cooking",
    "models",
    "item",
    "ingredients",
    "sausage",
)

STATES = ("cooked", "rotten", "burnt")
TEXTURE_PREFIX = "tfmc_cooking:item/ingredients/sausage/sausage_"


def swap_textures(data: dict, state: str) -> None:
    tex = f"{TEXTURE_PREFIX}{state}"
    textures = data.get("textures", {})
    for key in list(textures.keys()):
        val = textures[key]
        if isinstance(val, str) and "sausage_" in val:
            textures[key] = tex


def write_json(path: str, data: dict) -> None:
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent="\t")
        f.write("\n")
    print("wrote", os.path.basename(path))


def main() -> None:
    raw_single = os.path.join(MODEL_DIR, "sausage_raw.json")
    with open(raw_single, encoding="utf-8") as f:
        single_template = json.load(f)

    for state in STATES:
        data = json.loads(json.dumps(single_template))
        swap_textures(data, state)
        write_json(os.path.join(MODEL_DIR, f"sausage_{state}.json"), data)

    for n in range(1, 6):
        raw_chain = os.path.join(MODEL_DIR, f"sausage_chain_raw_{n}.json")
        with open(raw_chain, encoding="utf-8") as f:
            chain_template = json.load(f)
        for state in STATES:
            data = json.loads(json.dumps(chain_template))
            swap_textures(data, state)
            write_json(os.path.join(MODEL_DIR, f"sausage_chain_{state}_{n}.json"), data)


if __name__ == "__main__":
    main()
