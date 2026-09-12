"""Print ItemsAdder YAML block for all 24 sausage items."""
STATES = ("raw", "cooked", "rotten", "burnt")
STATE_LABEL = {"raw": "Raw", "cooked": "Cooked", "rotten": "Rotten", "burnt": "Burnt"}


def item_block(item_id: str, display_name: str, model_path: str) -> str:
    return f"""  {item_id}:
    display_name: "{display_name}"
    resource:
      material: TROPICAL_FISH
      generate: false
      model_path: {model_path}
"""


def main() -> None:
    lines = []
    for state in STATES:
        label = STATE_LABEL[state]
        lines.append(item_block(
            f"sausage_{state}",
            f"Sausage {label}",
            f"item/ingredients/sausage/sausage_{state}",
        ))
    for state in STATES:
        label = STATE_LABEL[state]
        for n in range(1, 6):
            count = 6 - n
            lines.append(item_block(
                f"sausage_chain_{state}_{n}",
                f"Sausage Chain {label} ({count})",
                f"item/ingredients/sausage/sausage_chain_{state}_{n}",
            ))
    print("\n".join(lines))


if __name__ == "__main__":
    main()
