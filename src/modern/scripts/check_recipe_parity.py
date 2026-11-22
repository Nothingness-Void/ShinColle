#!/usr/bin/env python3
"""Compare every reference crafting recipe with modern ID/metadata/tag aliases."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
OLD = ROOT / "src/main/resources/assets/shincolle/recipes"
DATA = ROOT / "src/modern/resources/data/shincolle"
NEW = DATA / ("recipes" if (DATA / "recipes").exists() else "recipe")
ORES = {"blockDiamond": "storage_blocks/diamond", "cropWheat": "crops/wheat",
        "gemDiamond": "gems/diamond", "gunpowder": "gunpowder", "netherStar": "nether_stars",
        "obsidian": "obsidian", "paper": "paper", "stickWood": "rods/wooden", "cookedRice": "cooked_rice"}
ORES.update({"ingot" + metal.title(): "ingots/" + metal for metal in
             ("bronze", "copper", "gold", "iron", "lead", "silver", "steel", "tin", "uranium")})
ORES.update({"foodFlour": "flour", "dyeBlue": "dyes/blue", "gemLapis": "gems/lapis"})
VARIANTS = {"abyssmetal", "abyssnugget", "ammo", "combatration", "grudge", "shiptank"}


def item(value):
    name = value.get("item", value.get("id")).lower()
    data = value.get("data", 0)
    if name.startswith("shincolle:"):
        stem = name.split(":")[1]
        if stem == "shipspawnegg":
            return "shincolle:" + {0: "smallegg", 1: "largeegg"}[data]
        if stem in VARIANTS and data:
            return name + str(data)
    assert data == 0, (name, data)
    return {"minecraft:clownfish": "minecraft:tropical_fish"}.get(name, name)


def ingredient(value):
    if isinstance(value, list):
        return "|".join(sorted({s for v in value for s in ingredient(v).split("|")}))
    if "ore" in value:
        return "#common:" + ORES[value["ore"]]
    if "tag" in value:
        tag = value["tag"]
        if tag.startswith("shincolle:"):
            folder = "items" if (DATA / "tags/items").exists() else "item"
            values = json.loads((DATA / "tags" / folder / (tag.split(":")[1] + ".json")).read_text())["values"]
            return ingredient([{"tag": v[1:]} if v.startswith("#") else {"item": v} for v in values])
        if tag.startswith(("forge:", "c:")):
            tag = "common:" + tag.split(":", 1)[1]
        return "#" + tag
    return item(value)


def signature(value):
    result = (item(value["result"]), value["result"].get("count", 1))
    if value["type"].endswith("shapeless"):
        inputs = tuple(sorted(ingredient(v) for v in value["ingredients"]))
        return result, "shapeless", inputs
    rows = value["pattern"]
    while not rows[0].strip(): rows = rows[1:]
    while not rows[-1].strip(): rows = rows[:-1]
    left = min(len(r) - len(r.lstrip()) for r in rows)
    right = max(len(r.rstrip()) for r in rows)
    grid = tuple(tuple(" " if c == " " else ingredient(value["key"][c]) for c in r[left:right]) for r in rows)
    mirror = tuple(tuple(reversed(row)) for row in grid)
    return result, "shaped", min(grid, mirror)


def main():
    old = {p.name: signature(json.loads(p.read_text())) for p in sorted(OLD.glob("*.json"))}
    new = {p.name: signature(json.loads(p.read_text())) for p in sorted(NEW.glob("*.json"))}
    remaining = dict(new)
    missing = {}
    for name, sig in old.items():
        match = next((n for n, s in remaining.items() if s == sig), None)
        if match is None:
            missing[name] = sig
        else:
            del remaining[match]
    print(f"Recipe audit: {len(old)} legacy JSONs, {len(new)} modern JSONs, {len(old) - len(missing)} exact semantic matches")
    for name, sig in missing.items():
        candidates = {n: s for n, s in remaining.items() if s[0][0] == sig[0][0]}
        print("LEGACY", name, json.dumps(sig), "MODERN", json.dumps(candidates, sort_keys=True))
    for name, sig in remaining.items():
        if not any(s[0][0] == sig[0][0] for s in missing.values()):
            print("MODERN_ONLY", name, json.dumps(sig))
    return missing, remaining


if __name__ == "__main__":
    main()
