package com.lulan.shincolle.client.model.legacy.compat;

import java.util.ArrayList;
import java.util.List;

public class ModelRenderer {
    public float textureWidth = 64F, textureHeight = 32F;
    public int textureOffsetX, textureOffsetY;
    public float rotationPointX, rotationPointY, rotationPointZ;
    public float rotateAngleX, rotateAngleY, rotateAngleZ;
    public float offsetX, offsetY, offsetZ;
    public boolean isHidden, mirror;
    public boolean showModel = true;
    public final List<ModelBox> cubeList = new ArrayList<>();
    public List<ModelRenderer> childModels;
    public final String boxName;
    private final ModelBase baseModel;

    public ModelRenderer(ModelBase model) { this(model, null); }
    public ModelRenderer(ModelBase model, String name) {
        this.baseModel = model;
        this.boxName = name;
        model.boxList.add(this);
        this.setTextureSize(model.textureWidth, model.textureHeight);
    }
    public ModelRenderer(ModelBase model, int u, int v) { this(model); setTextureOffset(u, v); }
    public ModelRenderer setTextureOffset(int u, int v) { textureOffsetX = u; textureOffsetY = v; return this; }
    public ModelRenderer setTextureSize(int width, int height) { textureWidth = width; textureHeight = height; return this; }
    public void setRotationPoint(float x, float y, float z) { rotationPointX = x; rotationPointY = y; rotationPointZ = z; }
    public void addChild(ModelRenderer child) {
        if (childModels == null) childModels = new ArrayList<>();
        childModels.add(child);
    }
    public ModelRenderer addBox(String name, float x, float y, float z, int width, int height, int depth) {
        String fullName = boxName + "." + name;
        int[] uv = baseModel.getTextureOffset(fullName);
        setTextureOffset(uv[0], uv[1]);
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, x, y, z, width, height, depth, 0F).setBoxName(fullName));
        return this;
    }
    public ModelRenderer addBox(float x, float y, float z, int width, int height, int depth) {
        addBox(x, y, z, width, height, depth, 0F);
        return this;
    }
    public ModelRenderer addBox(float x, float y, float z, int width, int height, int depth, boolean mirrored) {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, x, y, z, width, height, depth, 0F, mirrored));
        return this;
    }
    public void addBox(float x, float y, float z, int width, int height, int depth, float delta) {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, x, y, z, width, height, depth, delta));
    }
    public void render(float scale) {
        if (isHidden || !showModel) return;
        GlStateManager.translate(offsetX, offsetY, offsetZ);
        if (rotateAngleX == 0F && rotateAngleY == 0F && rotateAngleZ == 0F) {
            GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
            renderBoxesAndChildren(scale);
            GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
        } else {
            GlStateManager.pushMatrix();
            postRender(scale);
            renderBoxesAndChildren(scale);
            GlStateManager.popMatrix();
        }
        GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
    }
    private void renderBoxesAndChildren(float scale) {
        for (ModelBox box : cubeList) box.render(scale);
        if (childModels != null) for (ModelRenderer child : childModels) child.render(scale);
    }
    public void postRender(float scale) {
        if (isHidden || !showModel) return;
        GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
        float degrees = 180F / (float)Math.PI;
        if (rotateAngleZ != 0F) GlStateManager.rotate(rotateAngleZ * degrees, 0F, 0F, 1F);
        if (rotateAngleY != 0F) GlStateManager.rotate(rotateAngleY * degrees, 0F, 1F, 0F);
        if (rotateAngleX != 0F) GlStateManager.rotate(rotateAngleX * degrees, 1F, 0F, 0F);
    }
}
