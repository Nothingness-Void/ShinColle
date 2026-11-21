package com.lulan.shincolle.client.model.legacy.compat;

import net.minecraft.world.phys.Vec3;

public class TexturedQuad {
    public PositionTextureVertex[] vertexPositions;
    public TexturedQuad(PositionTextureVertex[] vertices, int u1, int v1, int u2, int v2, float width, float height) {
        vertexPositions = vertices;
        vertices[0] = vertices[0].setTexturePosition(u2 / width, v1 / height);
        vertices[1] = vertices[1].setTexturePosition(u1 / width, v1 / height);
        vertices[2] = vertices[2].setTexturePosition(u1 / width, v2 / height);
        vertices[3] = vertices[3].setTexturePosition(u2 / width, v2 / height);
    }
    public void flipFace() {
        PositionTextureVertex[] reversed = new PositionTextureVertex[vertexPositions.length];
        for (int i = 0; i < reversed.length; i++) reversed[i] = vertexPositions[reversed.length - i - 1];
        vertexPositions = reversed;
    }
    public void draw(float scale) {
        Vec3 a = vertexPositions[0].vector3D.subtract(vertexPositions[1].vector3D);
        Vec3 b = vertexPositions[2].vector3D.subtract(vertexPositions[1].vector3D);
        Vec3 normal = b.cross(a).normalize();
        var pose = LegacyRenderContext.poseStack.last();
        for (PositionTextureVertex vertex : vertexPositions) {
            LegacyRenderContext.vertices.addVertex(pose.pose(), (float)(vertex.vector3D.x * scale),
                            (float)(vertex.vector3D.y * scale), (float)(vertex.vector3D.z * scale))
                    .setColor(LegacyRenderContext.red, LegacyRenderContext.green, LegacyRenderContext.blue, LegacyRenderContext.alpha)
                    .setUv(vertex.texturePositionX, vertex.texturePositionY)
                    .setOverlay(LegacyRenderContext.overlay).setLight(LegacyRenderContext.light)
                    .setNormal(pose.normal(), (float)normal.x, (float)normal.y, (float)normal.z);
        }
    }
}
