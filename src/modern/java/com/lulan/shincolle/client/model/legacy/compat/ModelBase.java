package com.lulan.shincolle.client.model.legacy.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelBase {
    public int textureWidth = 64;
    public int textureHeight = 32;
    public final List<ModelRenderer> boxList = new ArrayList<>();
    public float swingProgress;
    public boolean isRiding;
    public boolean isChild = true;
    private final Map<String, int[]> textureOffsets = new HashMap<>();

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age,
                       float yaw, float pitch, float scale) {}

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float age,
                                  float yaw, float pitch, float scale, Entity entity) {}

    public void setTextureOffset(String name, int u, int v) { textureOffsets.put(name, new int[]{u, v}); }
    public int[] getTextureOffset(String name) { return textureOffsets.get(name); }
}
