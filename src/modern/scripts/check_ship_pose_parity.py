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
import shutil


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
    parser.add_argument("--classpath-file", type=Path)
    args = parser.parse_args()
    kinds = re.findall(r"\bModel\w+\b", (JAVA / "entity/ship/LegacyShipModelKind.java")
                       .read_text().split(";")[1].split(";")[0])
    kinds = sorted(set(kinds))
    assert kinds, "No model kinds found"
    for kind in kinds:
        modern = JAVA / f"client/model/legacy/ship/{kind}.java"
        legacy = ROOT / f"src/main/java/com/lulan/shincolle/client/model/{kind}.java"
        body = model_body(modern.read_text())
        if kind == "ModelDestroyerHime":
            override = "DestroyerHimePosePolicy.showLegs(state,ent.getShipDepth(0))"
            assert body.count(override) == 2, "Both Hime visibility and pose must use the approved policy"
            body = body.replace(override, "EmotionHelper.checkModelState(4,state)")
        assert body == model_body(legacy.read_text()), kind
    for name, old_path, new_path in (
        ("setRotationAngles", "client/model/ShipModelBaseAdv.java", "client/model/legacy/ship/ShipModelBaseAdv.java"),
        ("checkModelState", "utility/EmotionHelper.java", "client/model/legacy/ship/EmotionHelper.java"),
    ):
        old = ROOT / "src/main/java/com/lulan/shincolle" / old_path
        assert method(old.read_text(), name) == method((JAVA / new_path).read_text(), name), name
    bridge = method((JAVA / "client/model/entity/LegacyShipPoseModel.java").read_text(), "renderToBuffer")
    assert bridge.index("model.setRotationAngles(") < bridge.index("poses.pushPose()") < bridge.index("model.render(")
    print(f"All {len(kinds)} model bodies match legacy after three API aliases and the approved Hime land-leg override", flush=True)
    print("Shared pose, visibility flags and outer pre-render call order verified", flush=True)

    classes = ROOT / "build/ship-pose-audit"
    classes.mkdir(exist_ok=True)
    classpath_file = args.classpath_file
    if classpath_file is None:
        classpath_file = ROOT / "build/moddev/clientLegacyClasspath.txt"
        if not classpath_file.exists():
            classpath_file = ROOT / "build/classpath/runClient_minecraftClasspath.txt"
            if not classpath_file.exists():
                classpath_file = ROOT / "build/classpath/runGameTestServer_minecraftClasspath.txt"
    paths = classpath_file.read_text().strip().replace(os.pathsep, "\n").splitlines()
    paths += [str(ROOT / "build/classes/java/main")]
    paths += [str(p) for p in (ROOT / "build/moddev/artifacts").glob("*.jar") if "sources" not in p.name]
    classpath = os.pathsep.join(paths)
    java_home = Path(os.environ.get("JAVA_HOME", str(Path(shutil.which("java")).resolve().parents[1])))
    subprocess.run([str(java_home / "bin/javac"), "-proc:none", "-sourcepath", "", "-cp", classpath,
                    "-d", str(classes), str(Path(__file__).with_name("ShipModelPoseAudit.java"))], check=True)
    with args.csv.open("w") as output:
        subprocess.run([str(java_home / "bin/java"), "-Dterminal.jline=false", "-cp", str(classes) + os.pathsep + classpath,
                        "ShipModelPoseAudit"], stdout=output, cwd=classes, check=True)
    print(f"Vertex audit passed; CSV: {args.csv}")


if __name__ == "__main__":
    main()
