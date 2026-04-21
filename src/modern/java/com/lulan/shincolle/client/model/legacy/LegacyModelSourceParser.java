package com.lulan.shincolle.client.model.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyModelSourceParser {

    private static final String NUMBER = "[-+]?\\d+(?:\\.\\d+)?(?:[FfDd])?";

    private static final Pattern TEXTURE_WIDTH = Pattern.compile("(?:this\\.)?textureWidth\\s*=\\s*(\\d+)\\s*;");
    private static final Pattern TEXTURE_HEIGHT = Pattern.compile("(?:this\\.)?textureHeight\\s*=\\s*(\\d+)\\s*;");
    private static final Pattern FIELD_ASSIGNMENT = Pattern.compile("(?:this\\.)?(scale|offsetY)\\s*=\\s*(" + NUMBER + ")\\s*;");
    private static final Pattern PART_CONSTRUCTION = Pattern.compile(
            "(?:this\\.)?(\\w+)\\s*=\\s*new\\s+(?:ShipModelRenderer|ModelRenderer)\\s*\\(\\s*this\\s*,\\s*(?:\"([^\"]+)\"|(\\d+)\\s*,\\s*(\\d+))\\s*\\)\\s*;",
            Pattern.DOTALL);
    private static final Pattern TEXTURE_OFFSET = Pattern.compile(
            "(?:this\\.)?setTextureOffset\\(\\s*\"([^\"]+)\"\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*\\)\\s*;");
    private static final Pattern PART_MIRROR = Pattern.compile("(?:this\\.)?(\\w+)\\.mirror\\s*=\\s*true\\s*;");
    private static final Pattern ROTATION_POINT = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.setRotationPoint\\(\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*\\)\\s*;");
    private static final Pattern ROTATION = Pattern.compile(
            "(?:this\\.)?(?:setRotateAngle|setRotation)\\(\\s*(?:this\\.)?(\\w+)\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*\\)\\s*;");
    private static final Pattern NAMED_BOX = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.addBox\\(\\s*\"([^\"]+)\"\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")(?:\\s*,\\s*(" + NUMBER + "))?\\s*\\)\\s*;");
    private static final Pattern UNNAMED_BOX = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.addBox\\(\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")(?:\\s*,\\s*(" + NUMBER + "))?\\s*\\)\\s*;");
    private static final Pattern CHILD_LINK = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.addChild\\(\\s*(?:this\\.)?(\\w+)\\s*\\)\\s*;");
    private static final Pattern RENDER_METHOD = Pattern.compile("public\\s+void\\s+render\\s*\\([^)]*\\)\\s*\\{");
    private static final Pattern SCALE_LEVEL_DEFAULT = Pattern.compile("default\\s*:\\s*(.*?)(?:break\\s*;|$)", Pattern.DOTALL);
    private static final Pattern GL_TRANSFORM = Pattern.compile("GlStateManager\\.(scale|translate|rotate)\\(([^)]*)\\)\\s*;");
    private static final Pattern METHOD_COMMENT_BLOCK = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern METHOD_COMMENT_LINE = Pattern.compile("//.*?$", Pattern.MULTILINE);
    private static final Pattern FIRST_RENDER_CALL = Pattern.compile("\\.render\\s*\\(");

    public LegacyModelDefinition parse(String modelName, String source) {
        String sanitized = stripComments(source);
        String constructorBody = extractBody(Pattern.compile("public\\s+" + Pattern.quote(modelName) + "\\s*\\(\\)\\s*\\{"),
                sanitized);
        String renderBody = extractBody(RENDER_METHOD, sanitized);

        ParsedModelBuilder builder = new ParsedModelBuilder(modelName);
        builder.textureWidth = parseInt(TEXTURE_WIDTH, constructorBody, 64);
        builder.textureHeight = parseInt(TEXTURE_HEIGHT, constructorBody, 64);

        if (constructorBody.contains("setDefaultFaceModel()")) {
            injectDefaultFaceParts(builder);
        }

        parseTextureOffsets(constructorBody, builder);
        parsePartConstruction(constructorBody, builder);
        parseMirrors(constructorBody, builder);
        parseRotationPoints(constructorBody, builder);
        parseRotations(constructorBody, builder);
        parseBoxes(constructorBody, builder);
        parseChildLinks(constructorBody, builder);
        parseTransforms(constructorBody, renderBody, builder);
        builder.finish();

        return builder.build();
    }

    private static void parseTextureOffsets(String body, ParsedModelBuilder builder) {
        Matcher matcher = TEXTURE_OFFSET.matcher(body);
        while (matcher.find()) {
            builder.namedTextureOffsets.put(matcher.group(1),
                    new int[] {Math.round(parseNumber(matcher.group(2))), Math.round(parseNumber(matcher.group(3)))});
        }
    }

    private static void parsePartConstruction(String body, ParsedModelBuilder builder) {
        Matcher matcher = PART_CONSTRUCTION.matcher(body);
        while (matcher.find()) {
            PartBuilder part = builder.part(matcher.group(1));

            if (matcher.group(3) != null && matcher.group(4) != null) {
                part.defaultTexU = Integer.parseInt(matcher.group(3));
                part.defaultTexV = Integer.parseInt(matcher.group(4));
            }
        }
    }

    private static void parseMirrors(String body, ParsedModelBuilder builder) {
        Matcher matcher = PART_MIRROR.matcher(body);
        while (matcher.find()) {
            builder.part(matcher.group(1)).mirror = true;
        }
    }

    private static void parseRotationPoints(String body, ParsedModelBuilder builder) {
        Matcher matcher = ROTATION_POINT.matcher(body);
        while (matcher.find()) {
            PartBuilder part = builder.part(matcher.group(1));
            part.pivotX = parseNumber(matcher.group(2));
            part.pivotY = parseNumber(matcher.group(3));
            part.pivotZ = parseNumber(matcher.group(4));
        }
    }

    private static void parseRotations(String body, ParsedModelBuilder builder) {
        Matcher matcher = ROTATION.matcher(body);
        while (matcher.find()) {
            PartBuilder part = builder.part(matcher.group(1));
            part.rotateX = parseNumber(matcher.group(2));
            part.rotateY = parseNumber(matcher.group(3));
            part.rotateZ = parseNumber(matcher.group(4));
        }
    }

    private static void parseBoxes(String body, ParsedModelBuilder builder) {
        Matcher namedMatcher = NAMED_BOX.matcher(body);
        while (namedMatcher.find()) {
            String partName = namedMatcher.group(1);
            String boxName = namedMatcher.group(2);
            PartBuilder part = builder.part(partName);
            int[] uv = builder.namedTextureOffsets.get(partName + "." + boxName);

            if (uv == null) {
                uv = new int[] {part.defaultTexU, part.defaultTexV};
            }

            part.cubes.add(new LegacyModelDefinition.LegacyCube(uv[0], uv[1],
                    parseNumber(namedMatcher.group(3)), parseNumber(namedMatcher.group(4)),
                    parseNumber(namedMatcher.group(5)), parseNumber(namedMatcher.group(6)),
                    parseNumber(namedMatcher.group(7)), parseNumber(namedMatcher.group(8)),
                    parseOptionalNumber(namedMatcher.group(9)), part.mirror));
        }

        Matcher unnamedMatcher = UNNAMED_BOX.matcher(body);
        while (unnamedMatcher.find()) {
            PartBuilder part = builder.part(unnamedMatcher.group(1));
            part.cubes.add(new LegacyModelDefinition.LegacyCube(part.defaultTexU, part.defaultTexV,
                    parseNumber(unnamedMatcher.group(2)), parseNumber(unnamedMatcher.group(3)),
                    parseNumber(unnamedMatcher.group(4)), parseNumber(unnamedMatcher.group(5)),
                    parseNumber(unnamedMatcher.group(6)), parseNumber(unnamedMatcher.group(7)),
                    parseOptionalNumber(unnamedMatcher.group(8)), part.mirror));
        }
    }

    private static void parseChildLinks(String body, ParsedModelBuilder builder) {
        Matcher matcher = CHILD_LINK.matcher(body);
        while (matcher.find()) {
            String parent = matcher.group(1);
            String child = matcher.group(2);
            builder.part(parent).children.add(child);
            builder.part(child);
            builder.childParts.add(child);
        }
    }

    private static void parseTransforms(String constructorBody, String renderBody, ParsedModelBuilder builder) {
        float constructorScale = resolveFieldValue(constructorBody, "scale", Float.NaN);
        float constructorOffsetY = resolveFieldValue(constructorBody, "offsetY", Float.NaN);

        float defaultScale = constructorScale;
        float defaultOffsetY = constructorOffsetY;
        Matcher defaultMatcher = SCALE_LEVEL_DEFAULT.matcher(renderBody);

        if (defaultMatcher.find()) {
            String defaultBlock = defaultMatcher.group(1);
            defaultScale = resolveFieldValue(defaultBlock, "scale", defaultScale);
            defaultOffsetY = resolveFieldValue(defaultBlock, "offsetY", defaultOffsetY);
        }

        Matcher renderCallMatcher = FIRST_RENDER_CALL.matcher(renderBody);
        String prefix = renderCallMatcher.find() ? renderBody.substring(0, renderCallMatcher.start()) : renderBody;
        Matcher transformMatcher = GL_TRANSFORM.matcher(prefix);

        while (transformMatcher.find()) {
            String operation = transformMatcher.group(1).toLowerCase(Locale.ROOT);
            String[] args = splitArguments(transformMatcher.group(2));

            switch (operation) {
                case "scale" -> {
                    if (args.length == 3) {
                        Float x = resolveTransformArg(args[0], defaultScale, defaultOffsetY);
                        Float y = resolveTransformArg(args[1], defaultScale, defaultOffsetY);
                        Float z = resolveTransformArg(args[2], defaultScale, defaultOffsetY);

                        if (x != null && y != null && z != null) {
                            builder.transformOps.add(new LegacyModelDefinition.TransformOp(
                                    LegacyModelDefinition.TransformOp.Operation.SCALE, x, y, z, 0.0F));
                        }
                    }
                }
                case "translate" -> {
                    if (args.length == 3) {
                        Float x = resolveTransformArg(args[0], defaultScale, defaultOffsetY);
                        Float y = resolveTransformArg(args[1], defaultScale, defaultOffsetY);
                        Float z = resolveTransformArg(args[2], defaultScale, defaultOffsetY);

                        if (x != null && y != null && z != null) {
                            builder.transformOps.add(new LegacyModelDefinition.TransformOp(
                                    LegacyModelDefinition.TransformOp.Operation.TRANSLATE, x, y, z, 0.0F));
                        }
                    }
                }
                case "rotate" -> {
                    if (args.length == 4) {
                        Float angle = resolveTransformArg(args[0], defaultScale, defaultOffsetY);
                        Float axisX = resolveTransformArg(args[1], defaultScale, defaultOffsetY);
                        Float axisY = resolveTransformArg(args[2], defaultScale, defaultOffsetY);
                        Float axisZ = resolveTransformArg(args[3], defaultScale, defaultOffsetY);

                        if (angle != null && axisX != null && axisY != null && axisZ != null) {
                            builder.transformOps.add(new LegacyModelDefinition.TransformOp(
                                    LegacyModelDefinition.TransformOp.Operation.ROTATE, angle, axisX, axisY, axisZ));
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private static String[] splitArguments(String args) {
        String[] raw = args.split(",");
        String[] trimmed = new String[raw.length];

        for (int i = 0; i < raw.length; i++) {
            trimmed[i] = raw[i].trim();
        }

        return trimmed;
    }

    private static Float resolveTransformArg(String token, float scale, float offsetY) {
        String normalized = token.trim();

        if (normalized.equals("scale") || normalized.equals("this.scale")) {
            return Float.isNaN(scale) ? null : scale;
        }

        if (normalized.equals("offsetY") || normalized.equals("this.offsetY")) {
            return Float.isNaN(offsetY) ? null : offsetY;
        }

        if (normalized.matches(NUMBER)) {
            return parseNumber(normalized);
        }

        return null;
    }

    private static float resolveFieldValue(String body, String fieldName, float fallback) {
        Matcher matcher = FIELD_ASSIGNMENT.matcher(body);
        float value = fallback;

        while (matcher.find()) {
            if (fieldName.equals(matcher.group(1))) {
                value = parseNumber(matcher.group(2));
            }
        }

        return value;
    }

    private static int parseInt(Pattern pattern, String body, int fallback) {
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static float parseOptionalNumber(String token) {
        return token == null ? 0.0F : parseNumber(token);
    }

    private static float parseNumber(String token) {
        return Float.parseFloat(token.replace("F", "").replace("f", "").replace("D", "").replace("d", ""));
    }

    private static String stripComments(String source) {
        return METHOD_COMMENT_LINE.matcher(METHOD_COMMENT_BLOCK.matcher(source).replaceAll("")).replaceAll("");
    }

    private static String extractBody(Pattern pattern, String source) {
        Matcher matcher = pattern.matcher(source);

        if (!matcher.find()) {
            throw new IllegalStateException("Unable to locate method body for pattern " + pattern.pattern());
        }

        int braceStart = source.indexOf('{', matcher.start());
        int depth = 1;
        int cursor = braceStart + 1;

        while (cursor < source.length() && depth > 0) {
            char current = source.charAt(cursor);

            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
            }

            cursor++;
        }

        if (depth != 0) {
            throw new IllegalStateException("Unbalanced braces while parsing legacy model source");
        }

        return source.substring(braceStart + 1, cursor - 1);
    }

    private static void injectDefaultFaceParts(ParsedModelBuilder builder) {
        addDefaultFacePart(builder, "Face0", 98, 63, 0.0F, -12.2F, -6.1F, -7.0F, 0.0F, -0.5F, 14.0F, 12.0F, 1.0F);
        addDefaultFacePart(builder, "Face1", 98, 76, 0.0F, -12.2F, -6.1F, -7.0F, 0.0F, -0.5F, 14.0F, 12.0F, 1.0F);
        addDefaultFacePart(builder, "Face2", 98, 89, 0.0F, -12.2F, -6.1F, -7.0F, 0.0F, -0.5F, 14.0F, 12.0F, 1.0F);
        addDefaultFacePart(builder, "Face3", 98, 102, 0.0F, -12.2F, -6.1F, -7.0F, 0.0F, -0.5F, 14.0F, 12.0F, 1.0F);
        addDefaultFacePart(builder, "Face4", 98, 115, 0.0F, -12.2F, -6.1F, -7.0F, 0.0F, -0.5F, 14.0F, 12.0F, 1.0F);
        addDefaultFacePart(builder, "Mouth0", 100, 53, 0.0F, -4.2F, -6.2F, -3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F);
        addDefaultFacePart(builder, "Mouth1", 100, 58, 0.0F, -4.2F, -6.2F, -3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F);
        addDefaultFacePart(builder, "Mouth2", 114, 53, 0.0F, -4.2F, -6.2F, -3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F);
        addDefaultFacePart(builder, "Flush0", 114, 58, -6.0F, -3.0F, -6.9F, -1.0F, 0.0F, -0.5F, 2.0F, 1.0F, 0.0F);
        addDefaultFacePart(builder, "Flush1", 114, 58, 6.0F, -3.0F, -6.9F, -1.0F, 0.0F, -0.5F, 2.0F, 1.0F, 0.0F);
    }

    private static void addDefaultFacePart(ParsedModelBuilder builder, String name, int texU, int texV, float pivotX,
                                           float pivotY, float pivotZ, float boxX, float boxY, float boxZ,
                                           float width, float height, float depth) {
        PartBuilder part = builder.part(name);
        part.defaultTexU = texU;
        part.defaultTexV = texV;
        part.pivotX = pivotX;
        part.pivotY = pivotY;
        part.pivotZ = pivotZ;
        part.cubes.add(new LegacyModelDefinition.LegacyCube(texU, texV, boxX, boxY, boxZ, width, height, depth,
                0.0F, false));
    }

    private static final class ParsedModelBuilder {
        private final Map<String, PartBuilder> parts = new LinkedHashMap<>();
        private final Map<String, int[]> namedTextureOffsets = new LinkedHashMap<>();
        private final Set<String> childParts = new LinkedHashSet<>();
        private final List<LegacyModelDefinition.TransformOp> transformOps = new ArrayList<>();
        private int textureWidth = 64;
        private int textureHeight = 64;
        private List<String> rootParts = List.of();

        private ParsedModelBuilder(String modelName) {
        }

        private PartBuilder part(String name) {
            return this.parts.computeIfAbsent(name, PartBuilder::new);
        }

        private void finish() {
            List<String> resolvedRoots = new ArrayList<>();

            for (String partName : this.parts.keySet()) {
                if (!this.childParts.contains(partName)) {
                    resolvedRoots.add(partName);
                }
            }

            this.rootParts = resolvedRoots;
        }

        private LegacyModelDefinition build() {
            Map<String, LegacyModelDefinition.LegacyPart> builtParts = new LinkedHashMap<>();

            for (PartBuilder builder : this.parts.values()) {
                builtParts.put(builder.name, builder.build());
            }

            return new LegacyModelDefinition(this.textureWidth, this.textureHeight, List.copyOf(this.transformOps),
                    List.copyOf(this.rootParts), Map.copyOf(builtParts));
        }
    }

    private static final class PartBuilder {
        private final String name;
        private final List<LegacyModelDefinition.LegacyCube> cubes = new ArrayList<>();
        private final List<String> children = new ArrayList<>();
        private int defaultTexU;
        private int defaultTexV;
        private boolean mirror;
        private float pivotX;
        private float pivotY;
        private float pivotZ;
        private float rotateX;
        private float rotateY;
        private float rotateZ;

        private PartBuilder(String name) {
            this.name = name;
        }

        private LegacyModelDefinition.LegacyPart build() {
            return new LegacyModelDefinition.LegacyPart(this.name, this.pivotX, this.pivotY, this.pivotZ,
                    this.rotateX, this.rotateY, this.rotateZ, List.copyOf(this.cubes), List.copyOf(this.children));
        }
    }
}
