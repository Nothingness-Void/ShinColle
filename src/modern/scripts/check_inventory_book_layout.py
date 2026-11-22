"""Source guard for the reported legacy GUI overlap, not a visual test."""
import json
from pathlib import Path
import re

root = Path(__file__).resolve().parents[3]
oldbase = root / "src/main/java/com/lulan/shincolle"
newbase = root / "src/modern/java/com/lulan/shincolle"
oldship = (oldbase / "client/gui/GuiShipInventory.java").read_text()
newship = (newbase / "client/screen/ShipInventoryScreen.java").read_text()
compact = lambda s: re.sub(r"\s+", "", re.sub(r"/\*.*?\*/|//[^\n]*", "", s, flags=re.S))
assert "drawEntityModel(guiLeft+218,guiTop+100,entity.getModelPos(),guiLeft+215-xMouse,guiTop+60-yMouse,this.shipRiding)" in compact(oldship)
assert "drawEntityModel(leftPos+218,topPos+100,entity.getModelPos(),leftPos+215-xMouse,topPos+60-yMouse,this.shipRiding)" in compact(newship)
for source in (oldship, newship):
    assert "modelPos[0]" in source and "modelPos[1]" in source and "modelPos[3]" in source
    assert "drawString(canMelee,187,133,0)" in compact(source)
oldbook = (oldbase / "client/gui/GuiBook.java").read_text()
newbook = (newbase / "client/screen/DeskScreen.java").read_text()
for key in ("PageWidth", "Page0LX", "Page0RX", "Page0Y", "PageTLX", "PageTRX", "PageTY"):
    pattern = rf"\b{key}\s*=\s*(\d+)"
    assert re.search(pattern, oldbook)[1] == re.search(pattern, newbook)[1], key
for expression in ("PageTY+offY-4", "picX+=offX", "scale(0.8F,0.8F,0.8F)", "newY+=font."):
    assert expression in compact(oldbook) and expression in compact(newbook), expression

def layouts(source):
    result = {}
    for page, data in re.findall(r"put\((\d+),\s*Arrays\.asList\((.*?)\)\);", source, re.S):
        entries = []
        for entry in re.findall(r"new int\[\]\s*\{([^}]+)\}", data):
            fields = [field.strip() for field in entry.split(",")]
            # Item registry/metadata aliases do not change icon placement.
            entries.append(fields[:4] if fields[0] == "2" else fields)
        result[page] = entries
    return result

oldpages = layouts((oldbase / "reference/Values.java").read_text())
newpages = layouts((newbase / "menu/LegacyDeskBookData.java").read_text())
for page, layout in newpages.items():
    assert layout == oldpages[page], f"book layout changed: {page}"
oldlang = dict(line.split("=", 1) for line in (root / "src/main/resources/assets/shincolle/lang/en_US.lang").read_text().splitlines() if "=" in line and not line.startswith("#"))
newlang = json.loads((root / "src/modern/resources/assets/shincolle/lang/en_us.json").read_text())
for key, value in oldlang.items():
    if "book.chap" in key:
        assert newlang.get(key) == value, f"book English text changed: {key}"
print("Ship preview anchor/model transform inputs and Can Melee label position match legacy; no new clipping")
print(f"Book layout: {len(newpages)} page entries retain legacy text/image/icon positions; English book text unchanged")
