#!/usr/bin/env python3
"""Compare legacy model bodies and measure CPU-rendered vertices at entity Y=0.

Requires ./gradlew compileJava and prepared client artifacts. No GPU/world is
used; the fake ship fixes age=100, dry land, no attacks or riding, scale level 0.
The CSV is a geometric diagnostic, not a claim that every visible part is a foot.
"""

import argparse
import os
from pathlib import Path
import re
import subprocess


ROOT = Path(__file__).resolve().parents[3]
JAVA = ROOT / "src/modern/java/com/lulan/shincolle"


def model_body(text):
    text = re.sub(r"(?m)^\s*(?:package|import) [^\n]*\n", "", text)
    text = re.sub(r"\s+", "", text)
    return (text.replace("IFloatingEntity", "IShipFloating")
            .replace("getEntityDepth()", "getShipDepth()")
            .replace("GL11.glTranslatef", "GlStateManager.translate"))


def method(text, name):
    match = re.search(r"public\s+(?:static\s+)?\w+\s+" + name + r"\(", text)
    assert match, name
    start = text.index("{", match.end())
    depth = 1
    end = start + 1
    while depth:
        depth += (text[end] == "{") - (text[end] == "}")
        end += 1
    return model_body(text[match.start():end])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", type=Path, required=True)
    args = parser.parse_args()
    kinds = re.findall(r"\bModel\w+\b", (JAVA / "entity/ship/LegacyShipModelKind.java")
                       .read_text().split(";")[1].split(";")[0])
    kinds = sorted(set(kinds))
    assert kinds, "No model kinds found"
    for kind in kinds:
        modern = JAVA / f"client/model/legacy/ship/{kind}.java"
        legacy = ROOT / f"src/main/java/com/lulan/shincolle/client/model/{kind}.java"
        assert model_body(modern.read_text()) == model_body(legacy.read_text()), kind
    for name, old_path, new_path in (
        ("setRotationAngles", "client/model/ShipModelBaseAdv.java", "client/model/legacy/ship/ShipModelBaseAdv.java"),
        ("checkModelState", "utility/EmotionHelper.java", "client/model/legacy/ship/EmotionHelper.java"),
    ):
        old = ROOT / "src/main/java/com/lulan/shincolle" / old_path
        assert method(old.read_text(), name) == method((JAVA / new_path).read_text(), name), name
    bridge = method((JAVA / "client/model/entity/LegacyShipPoseModel.java").read_text(), "renderToBuffer")
    assert bridge.index("model.setRotationAngles(") < bridge.index("poses.pushPose()") < bridge.index("model.render(")
    print(f"All {len(kinds)} registered model bodies match legacy after three API aliases", flush=True)
    print("Shared pose, visibility flags and outer pre-render call order verified", flush=True)

    classes = ROOT / "build/ship-pose-audit"
    classes.mkdir(exist_ok=True)
    paths = (ROOT / "build/moddev/clientLegacyClasspath.txt").read_text().splitlines()
    paths += [str(ROOT / "build/classes/java/main")]
    paths += [str(p) for p in (ROOT / "build/moddev/artifacts").glob("*.jar") if "sources" not in p.name]
    classpath = os.pathsep.join(paths)
    java_home = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-21-openjdk-amd64"))
    subprocess.run([str(java_home / "bin/javac"), "-proc:none", "-sourcepath", "", "-cp", classpath,
                    "-d", str(classes), str(Path(__file__).with_name("ShipModelPoseAudit.java"))], check=True)
    with args.csv.open("w") as output:
        subprocess.run([str(java_home / "bin/java"), "-cp", str(classes) + os.pathsep + classpath,
                        "ShipModelPoseAudit"], stdout=output, cwd=classes, check=True)
    print(f"Vertex audit passed; CSV: {args.csv}")


if __name__ == "__main__":
    main()
