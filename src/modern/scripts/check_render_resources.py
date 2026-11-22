#!/usr/bin/env python3
"""Check case-safe resources and the legacy custom-block item render paths."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
RESOURCES = ROOT / "src/modern/resources"
ASSETS = RESOURCES / "assets/shincolle"


def main():
    paths = {}
    for path in RESOURCES.rglob("*"):
        if path.is_file():
            key = str(path.relative_to(RESOURCES)).casefold()
            assert key not in paths, f"Case-colliding resources: {paths.get(key)} / {path}"
            paths[key] = path

    for name in ("blockcrane", "blockwaypoint"):
        path = f"models/block/{name}.json"
        model = json.loads((ASSETS / path).read_text())
        assert model["parent"] == "minecraft:block/block", path
        legacy = json.loads((ROOT / "src/main/resources/assets/shincolle" / path).read_text())
        assert {k: v for k, v in model.items() if k != "parent"} == legacy, path
        for texture in model["textures"].values():
            assert texture == texture.lower(), texture
            assert (ASSETS / "textures" / (texture.removeprefix("shincolle:") + ".png")).is_file(), texture

    for name in ("blockdesk", "blocksmallshipyard"):
        model = json.loads((ASSETS / f"models/item/{name}.json").read_text())
        assert model["parent"] == "shincolle:item/legacy_block_entity", name
    entity = json.loads((ASSETS / "models/item/legacy_block_entity.json").read_text())
    assert entity["parent"] == "minecraft:builtin/entity"
    assert set(entity["display"]) == {
        "gui", "ground", "fixed", "thirdperson_righthand", "thirdperson_lefthand",
        "firstperson_righthand", "firstperson_lefthand",
    }
    print("Render resource checks passed: 0 case-fold collisions; legacy geometry, textures and item render paths intact")


if __name__ == "__main__":
    main()
