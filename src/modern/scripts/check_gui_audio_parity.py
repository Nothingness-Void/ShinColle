"""Directed source guards for the reported GUI/audio parity cases."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[3]
base = "java/com/lulan/shincolle/"
legacy = root / "src/main" / base
modern = root / "src/modern" / base


def method(source, signature):
    start = source.index(signature)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


def normalized(source):
    return re.sub(r"\s+", "", re.sub(r"/\*.*?\*/|//[^\n]*", "", source, flags=re.S))


old = method((legacy / "client/gui/GuiDesk.java").read_text(), "private void drawRadarText()")
new = method((modern / "client/screen/DeskScreen.java").read_text(), "private void drawRadarText()")
for before, after in {
    "BasicEntityShip": "LegacyShipEntity", "TextFormatting": "ChatFormatting",
    "Enums.EnumColors.WHITE.getValue()": "16777215",
    "Enums.EnumColors.CYAN.getValue()": "65535",
    "Enums.EnumColors.PURPLE_LIGHT.getValue()": "16581630",
    "MathHelper": "Mth", ".getLevel()": ".getShipLevel()",
    ".posX": ".getX()", ".posY": ".getY()", ".posZ": ".getZ()",
}.items():
    old = old.replace(before, after)
old = old.replace("(int)(s.ship.getY())", "Mth.floor(s.ship.getY())")
assert normalized(old) == normalized(new), "radar text must retain the original unbounded drawing"
print("Desk radar text: legacy body after API aliases and negative-Y floor adaptation; no name truncation/scaling added")

labels = method((modern / "client/screen/LargeShipyardScreen.java").read_text(), "protected void renderLabels(")
for forbidden in ("structure_", "core_", "this.title", "isStructureComplete", "isCoreLinked"):
    assert forbidden not in labels, forbidden
failure = method((modern / "menu/LargeShipyardMenu.java").read_text(), "public Component getFailureMessage()")
assert normalized(failure) == normalized('''public Component getFailureMessage() {
    if (this.getPowerGoal() <= 0 && this.hasBuildMode()) {
        return Component.translatable("gui.shincolle.shipyard.nomaterial");
    }
    if (!this.hasRemainedPower()) {
        return Component.translatable("gui.shincolle.shipyard.nofuel");
    }
    return Component.empty();
}''')
print("Large shipyard: no invented title/status overlays; only legacy material/fuel message priority")

historical = subprocess.check_output([
    "git", "show", "0aad501:src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java"
], cwd=root, text=True)
hour = method(historical, "private int getWorldHourTime()")
assert "checkTime == 0" in hour and "time % 1000L" in hour
assert "(time / 1000L) % 24" in hour
dispatch = historical.index("//play timekeeping sound")
assert historical.rfind("//end every 8 ticks", 0, dispatch) > 0
assert "getWorldHourTime()" in historical[dispatch:dispatch + 400]
clock = method((modern / "entity/ship/LegacyShipEntity.java").read_text(), "private void tickTimeKeeper()")
for gate in ("!this.isAlive()", "!this.isTimeKeeper()", "!ShinColleConfig.timeKeeping()", "worldTime % 1000L != 0L"):
    assert gate in clock, gate
assert "this.playShipCombatSound(" in clock
print("Timekeeper: historical per-server-tick exact-hour replay exists, including a frozen hour; unchanged")
