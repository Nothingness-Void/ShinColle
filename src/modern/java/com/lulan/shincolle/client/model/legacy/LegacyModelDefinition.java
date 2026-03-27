package com.lulan.shincolle.client.model.legacy;

import java.util.List;
import java.util.Map;

public record LegacyModelDefinition(int textureWidth, int textureHeight, List<TransformOp> transformOps,
                                    List<String> rootParts, Map<String, LegacyPart> parts) {

    public record TransformOp(Operation operation, float x, float y, float z, float w) {
        public enum Operation {
            SCALE,
            TRANSLATE,
            ROTATE
        }
    }

    public record LegacyCube(int texU, int texV, float x, float y, float z, float width, float height,
                             float depth, float deformation, boolean mirror) {
    }

    public record LegacyPart(String name, float pivotX, float pivotY, float pivotZ, float rotateX, float rotateY,
                             float rotateZ, List<LegacyCube> cubes, List<String> children) {
    }
}
