"""Inventory all explicit int casts, retaining the reviewed legacy X/Z exception."""
import argparse
import csv
from pathlib import Path
import re

root = Path(__file__).resolve().parents[3]
parser = argparse.ArgumentParser()
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
rows = []
for path in sorted((root / "src/modern").rglob("*.java")):
    source = path.read_text()
    relative = str(path.relative_to(root))
    for match in re.finditer(r"\(\s*int\s*\)", source):
        line = source.count("\n", 0, match.start()) + 1
        expression = source[match.end():].splitlines()[0].strip()
        category = "numeric/UI/animation/size narrowing; not world-position conversion"
        if "/gametest/" in relative:
            category = "test numeric fixture/assertion"
        elif "Math.floor(ship.get" in expression or "Math.floor(entity.get" in expression:
            category = "world-coordinate conversion already explicitly floored"
        elif path.name == "ShipPath.java" and "vec." in expression:
            category = "legacy X/Z truncation retained: legacy ai/path/ShipPath.java:141"
        rows.append((relative, line, expression, category))
    if "/gametest/" not in relative:
        assert not re.search(
            r"\(\s*int\s*\)\s*\(*\s*(?:(?:[\w.]+\.)?(?:getY\(|minY\b|maxY\b|pos1\.y\b)|[\w.]+\.posY\b)",
            source), f"unadapted world-Y cast in {relative}"

with args.output.open("w", newline="") as stream:
    writer = csv.writer(stream)
    writer.writerow(("path", "line", "cast_operand_line", "review"))
    writer.writerows(rows)
print(f"Coordinate-cast inventory: {len(rows)} explicit int casts across src/modern Java; no raw entity/world-Y casts")
print("Legacy ShipPath vector X/Z truncation retained; other X/Z position conversions already floor")
print(f"Inventory: {args.output}")
