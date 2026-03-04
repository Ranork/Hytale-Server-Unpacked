package com.hypixel.hytale.server.core.asset.type.buildertool.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBlockArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBrushData;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolStringArg;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BlockArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BoolArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BrushAxisArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BrushOriginArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BrushRotationArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BrushShapeArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.IntArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.MaskArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.StringArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.ToolArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.ToolArgException;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class BrushData implements NetworkSerializable<BuilderToolBrushData> {
   public static final String WIDTH_KEY = "Width";
   public static final String HEIGHT_KEY = "Height";
   public static final String SHAPE_KEY = "Shape";
   public static final String THICKNESS_KEY = "Thickness";
   public static final String CAPPED_KEY = "Capped";
   public static final String ORIGIN_KEY = "Origin";
   public static final String ORIGIN_ROTATION_KEY = "OriginRotation";
   public static final String ROTATION_AXIS_KEY = "RotationAxis";
   public static final String ROTATION_ANGLE_KEY = "RotationAngle";
   public static final String MIRROR_AXIS_KEY = "MirrorAxis";
   public static final String MATERIAL_KEY = "Material";
   public static final String FAVORITE_MATERIALS_KEY = "FavoriteMaterials";
   public static final String MASK_KEY = "Mask";
   public static final String MASK_ABOVE_KEY = "MaskAbove";
   public static final String MASK_NOT_KEY = "MaskNot";
   public static final String MASK_BELOW_KEY = "MaskBelow";
   public static final String MASK_ADJACENT_KEY = "MaskAdjacent";
   public static final String MASK_NEIGHBOR_KEY = "MaskNeighbor";
   public static final String MASK_COMMANDS_KEY = "MaskCommands";
   public static final String USE_MASK_COMMANDS_KEY = "UseMaskCommands";
   public static final String INVERT_MASK_KEY = "InvertMask";
   private static final String WIDTH_DOC = "The width of the brush shape";
   private static final String HEIGHT_DOC = "The height of the brush shape";
   private static final String THICKNESS_DOC = "The number of blocks thick the walls of the brush shape should be";
   private static final String CAPPED_DOC = "Controls whether the end(s) of hollow brush shapes are closed or open";
   private static final String SHAPE_DOC = "The brush shape";
   private static final String ORIGIN_DOC = "The origin of the brush shape";
   private static final String ORIGIN_ROTATION_DOC = "Toggles the vertical offset for shapes rotated about the x/z axis";
   private static final String ROTATION_AXIS_DOC = "The axis that the brush shape should rotate around";
   private static final String ROTATION_ANGLE_DOC = "The angle that the brush shape should be rotated by";
   private static final String MIRROR_AXIS_DOC = "The axis that the brush shape should mirror in";
   private static final String MATERIAL_DOC = "The material to apply when the brush is used";
   private static final String FAVORITE_MATERIALS_DOC = "Materials available for quick selection.\n\nWhen a material is selected from here, it is set on the Material key.";
   private static final String MASK_DOC = "Limits the selection to blocks matching materials in this mask";
   private static final String MASK_ABOVE_DOC = "Limits the selection to blocks above ones matching materials in this mask";
   private static final String MASK_NOT_DOC = "Limits the selection to any blocks except ones matching materials in this mask";
   private static final String MASK_BELOW_DOC = "Limits the selection to blocks below ones matching materials in this mask";
   private static final String MASK_ADJACENT_DOC = "Limits the selection to blocks horizontally adjacent to ones matching materials in this mask";
   private static final String MASK_NEIGHBOR_DOC = "Limits the selection to blocks neighboring (in any direction) ones matching materials in this mask";
   private static final String MASK_COMMANDS_DOC = "Custom mask commands to apply to the brush, based on /gmask syntax";
   private static final String USE_MASK_COMMANDS_DOC = "Specifies whether to use the block selector mask values or custom mask commands";
   private static final String INVERT_MASK_DOC = "When enabled, inverts the entire combined mask result";
   public static final int DEFAULT_WIDTH = 5;
   public static final int DEFAULT_HEIGHT = 5;
   public static final BrushData DEFAULT = new BrushData();
   public static final int DEFAULT_FAVORITE_MATERIALS_CAPACITY = 5;
   private static final Pattern NEWLINES_PATTERN = Pattern.compile("\\r?\\n");
   public static final BuilderCodec<BrushData> CODEC = BuilderCodec.builder(BrushData.class, BrushData::new)
      .append(new KeyedCodec<>("Width", IntArg.CODEC), (brushData, o) -> brushData.width = o, brushData -> brushData.width)
      .addValidator(Validators.nonNull())
      .documentation("The width of the brush shape")
      .add()
      .<IntArg>append(new KeyedCodec<>("Height", IntArg.CODEC), (brushData, o) -> brushData.height = o, brushData -> brushData.height)
      .addValidator(Validators.nonNull())
      .documentation("The height of the brush shape")
      .add()
      .<IntArg>append(new KeyedCodec<>("Thickness", IntArg.CODEC), (data, o) -> data.thickness = o, data -> data.thickness)
      .addValidator(Validators.nonNull())
      .documentation("The number of blocks thick the walls of the brush shape should be")
      .add()
      .<BoolArg>append(new KeyedCodec<>("Capped", BoolArg.CODEC), (data, o) -> data.capped = o, data -> data.capped)
      .addValidator(Validators.nonNull())
      .documentation("Controls whether the end(s) of hollow brush shapes are closed or open")
      .add()
      .<BrushShapeArg>append(new KeyedCodec<>("Shape", BrushShapeArg.CODEC), (brushData, o) -> brushData.shape = o, brushData -> brushData.shape)
      .addValidator(Validators.nonNull())
      .documentation("The brush shape")
      .add()
      .<BrushOriginArg>append(new KeyedCodec<>("Origin", BrushOriginArg.CODEC), (brushData, o) -> brushData.origin = o, brushData -> brushData.origin)
      .addValidator(Validators.nonNull())
      .documentation("The origin of the brush shape")
      .add()
      .<BoolArg>append(new KeyedCodec<>("OriginRotation", BoolArg.CODEC), (data, o) -> data.originRotation = o, data -> data.originRotation)
      .addValidator(Validators.nonNull())
      .documentation("Toggles the vertical offset for shapes rotated about the x/z axis")
      .add()
      .<BrushAxisArg>append(new KeyedCodec<>("RotationAxis", BrushAxisArg.CODEC), (data, o) -> data.rotationAxis = o, data -> data.rotationAxis)
      .addValidator(Validators.nonNull())
      .documentation("The axis that the brush shape should rotate around")
      .add()
      .<BrushRotationArg>append(new KeyedCodec<>("RotationAngle", BrushRotationArg.CODEC), (data, o) -> data.rotationAngle = o, data -> data.rotationAngle)
      .addValidator(Validators.nonNull())
      .documentation("The angle that the brush shape should be rotated by")
      .add()
      .<BrushAxisArg>append(new KeyedCodec<>("MirrorAxis", BrushAxisArg.CODEC), (data, o) -> data.mirrorAxis = o, data -> data.mirrorAxis)
      .addValidator(Validators.nonNull())
      .documentation("The axis that the brush shape should mirror in")
      .add()
      .<BlockArg>append(new KeyedCodec<>("Material", BlockArg.CODEC), (brushData, o) -> brushData.material = o, brushData -> brushData.material)
      .addValidator(Validators.nonNull())
      .documentation("The material to apply when the brush is used")
      .add()
      .<BlockArg[]>append(
         new KeyedCodec<>("FavoriteMaterials", new ArrayCodec<>(BlockArg.CODEC, BlockArg[]::new)),
         (brushData, o) -> brushData.favoriteMaterials = o,
         brushData -> brushData.favoriteMaterials
      )
      .documentation("Materials available for quick selection.\n\nWhen a material is selected from here, it is set on the Material key.")
      .add()
      .<MaskArg>append(new KeyedCodec<>("Mask", MaskArg.CODEC), (brushData, o) -> brushData.mask = o, brushData -> brushData.mask)
      .documentation("Limits the selection to blocks matching materials in this mask")
      .add()
      .<MaskArg>append(new KeyedCodec<>("MaskAbove", MaskArg.CODEC), (brushData, o) -> brushData.maskAbove = o, brushData -> brushData.maskAbove)
      .documentation("Limits the selection to blocks above ones matching materials in this mask")
      .add()
      .<MaskArg>append(new KeyedCodec<>("MaskNot", MaskArg.CODEC), (brushData, o) -> brushData.maskNot = o, brushData -> brushData.maskNot)
      .documentation("Limits the selection to any blocks except ones matching materials in this mask")
      .add()
      .<MaskArg>append(new KeyedCodec<>("MaskBelow", MaskArg.CODEC), (brushData, o) -> brushData.maskBelow = o, brushData -> brushData.maskBelow)
      .documentation("Limits the selection to blocks below ones matching materials in this mask")
      .add()
      .<MaskArg>append(new KeyedCodec<>("MaskAdjacent", MaskArg.CODEC), (brushData, o) -> brushData.maskAdjacent = o, brushData -> brushData.maskAdjacent)
      .documentation("Limits the selection to blocks horizontally adjacent to ones matching materials in this mask")
      .add()
      .<MaskArg>append(new KeyedCodec<>("MaskNeighbor", MaskArg.CODEC), (brushData, o) -> brushData.maskNeighbor = o, brushData -> brushData.maskNeighbor)
      .documentation("Limits the selection to blocks neighboring (in any direction) ones matching materials in this mask")
      .add()
      .<StringArg[]>append(
         new KeyedCodec<>("MaskCommands", new ArrayCodec<>(StringArg.CODEC, StringArg[]::new)),
         (brushData, o) -> brushData.maskCommands = o,
         brushData -> brushData.maskCommands
      )
      .documentation("Custom mask commands to apply to the brush, based on /gmask syntax")
      .add()
      .<BoolArg>append(
         new KeyedCodec<>("UseMaskCommands", BoolArg.CODEC), (brushData, o) -> brushData.useMaskCommands = o, brushData -> brushData.useMaskCommands
      )
      .documentation("Specifies whether to use the block selector mask values or custom mask commands")
      .add()
      .<BoolArg>append(new KeyedCodec<>("InvertMask", BoolArg.CODEC), (brushData, o) -> brushData.invertMask = o, brushData -> brushData.invertMask)
      .documentation("When enabled, inverts the entire combined mask result")
      .add()
      .build();
   protected IntArg width = new IntArg(5, 1, 100);
   protected IntArg height = new IntArg(5, 1, 100);
   protected IntArg thickness = new IntArg(0, 0, 100);
   protected BoolArg capped = new BoolArg(false);
   protected BrushShapeArg shape = new BrushShapeArg(BrushShape.Sphere);
   protected BrushOriginArg origin = new BrushOriginArg(BrushOrigin.Center);
   protected BoolArg originRotation = new BoolArg(false);
   protected BrushAxisArg rotationAxis = new BrushAxisArg(BrushAxis.None);
   protected BrushRotationArg rotationAngle = new BrushRotationArg(Rotation.None);
   protected BrushAxisArg mirrorAxis = new BrushAxisArg(BrushAxis.None);
   protected BlockArg material = new BlockArg(BlockPattern.EMPTY, true);
   protected BlockArg[] favoriteMaterials = BlockArg.EMPTY_ARRAY;
   protected MaskArg mask = MaskArg.EMPTY;
   protected MaskArg maskAbove = MaskArg.EMPTY;
   protected MaskArg maskNot = MaskArg.EMPTY;
   protected MaskArg maskBelow = MaskArg.EMPTY;
   protected MaskArg maskAdjacent = MaskArg.EMPTY;
   protected MaskArg maskNeighbor = MaskArg.EMPTY;
   protected StringArg[] maskCommands = StringArg.EMPTY_ARRAY;
   protected BoolArg useMaskCommands = new BoolArg(false);
   protected BoolArg invertMask = new BoolArg(false);

   protected BrushData() {
   }

   public BrushData(
      IntArg width,
      IntArg height,
      IntArg thickness,
      BoolArg capped,
      BrushShapeArg shape,
      BrushOriginArg origin,
      BoolArg originRotation,
      BrushAxisArg rotationAxis,
      BrushRotationArg rotationAngle,
      BrushAxisArg mirrorAxis,
      BlockArg material,
      BlockArg[] favoriteMaterials,
      MaskArg mask,
      MaskArg maskAbove,
      MaskArg maskNot,
      MaskArg maskBelow,
      MaskArg maskAdjacent,
      MaskArg maskNeighbor,
      StringArg[] maskCommands,
      BoolArg useMaskCommands
   ) {
      this.width = width;
      this.height = height;
      this.thickness = thickness;
      this.capped = capped;
      this.shape = shape;
      this.origin = origin;
      this.originRotation = originRotation;
      this.rotationAxis = rotationAxis;
      this.rotationAngle = rotationAngle;
      this.mirrorAxis = mirrorAxis;
      this.material = material;
      this.favoriteMaterials = favoriteMaterials;
      this.mask = mask;
      this.maskAbove = maskAbove;
      this.maskNot = maskNot;
      this.maskBelow = maskBelow;
      this.maskAdjacent = maskAdjacent;
      this.maskNeighbor = maskNeighbor;
      this.maskCommands = maskCommands;
      this.useMaskCommands = useMaskCommands;
   }

   public IntArg getWidth() {
      return this.width;
   }

   public IntArg getHeight() {
      return this.height;
   }

   public IntArg getThickness() {
      return this.thickness;
   }

   public BoolArg getCapped() {
      return this.capped;
   }

   public BrushShapeArg getShape() {
      return this.shape;
   }

   public BrushOriginArg getOrigin() {
      return this.origin;
   }

   public BoolArg getOriginRotation() {
      return this.originRotation;
   }

   public BrushAxisArg getRotationAxis() {
      return this.rotationAxis;
   }

   public BrushRotationArg getRotationAngle() {
      return this.rotationAngle;
   }

   public BrushAxisArg getMirrorAxis() {
      return this.mirrorAxis;
   }

   public BlockArg getMaterial() {
      return this.material;
   }

   public BlockArg[] getFavoriteMaterials() {
      return this.favoriteMaterials;
   }

   public MaskArg getMask() {
      return this.mask;
   }

   public MaskArg getMaskAbove() {
      return this.maskAbove;
   }

   public MaskArg getMaskNot() {
      return this.maskNot;
   }

   public MaskArg getMaskBelow() {
      return this.maskBelow;
   }

   public MaskArg getMaskAdjacent() {
      return this.maskAdjacent;
   }

   public MaskArg getMaskNeighbor() {
      return this.maskNeighbor;
   }

   public StringArg[] getMaskCommands() {
      return this.maskCommands;
   }

   public BoolArg getUseMaskCommands() {
      return this.useMaskCommands;
   }

   public BoolArg getInvertMask() {
      return this.invertMask;
   }

   public void updateArgValue(@Nonnull BrushData.Values brush, @Nonnull String id, @Nonnull String value) throws ToolArgException {
      switch (id) {
         case "Height":
            brush.height = this.height.fromString(value);
            break;
         case "Width":
            brush.width = this.width.fromString(value);
            break;
         case "Thickness":
            brush.thickness = this.thickness.fromString(value);
            break;
         case "Capped":
            brush.capped = this.capped.fromString(value);
            break;
         case "Shape":
            brush.shape = this.shape.fromString(value);
            break;
         case "Origin":
            brush.origin = this.origin.fromString(value);
            break;
         case "OriginRotation":
            brush.originRotation = this.originRotation.fromString(value);
            break;
         case "RotationAxis":
            brush.rotationAxis = this.rotationAxis.fromString(value);
            break;
         case "RotationAngle":
            brush.rotationAngle = this.rotationAngle.fromString(value);
            break;
         case "MirrorAxis":
            brush.mirrorAxis = this.mirrorAxis.fromString(value);
            break;
         case "Material":
            brush.material = this.material.fromString(value);
            break;
         case "FavoriteMaterials":
            brush.favoriteMaterials = value.isEmpty()
               ? BlockPattern.EMPTY_ARRAY
               : Arrays.stream(value.split(",")).limit(5L).map(BlockPattern::parse).toArray(BlockPattern[]::new);
            break;
         case "Mask":
            brush.mask = this.mask.fromString(value);
            break;
         case "MaskAbove":
            brush.maskAbove = this.maskAbove.fromString(value);
            break;
         case "MaskNot":
            brush.maskNot = this.maskNot.fromString(value);
            break;
         case "MaskBelow":
            brush.maskBelow = this.maskBelow.fromString(value);
            break;
         case "MaskAdjacent":
            brush.maskAdjacent = this.maskAdjacent.fromString(value);
            break;
         case "MaskNeighbor":
            brush.maskNeighbor = this.maskNeighbor.fromString(value);
            break;
         case "MaskCommands":
            brush.maskCommands = value.isEmpty() ? ArrayUtil.EMPTY_STRING_ARRAY : NEWLINES_PATTERN.split(value);
            break;
         case "UseMaskCommands":
            brush.useMaskCommands = this.useMaskCommands.fromString(value);
            break;
         case "InvertMask":
            brush.invertMask = this.invertMask.fromString(value);
            break;
         default:
            throw new ToolArgException(Message.translation("server.builderTools.toolUnknownArg").param("arg", id));
      }
   }

   @Nonnull
   public BuilderToolBrushData toPacket() {
      BuilderToolBrushData packet = new BuilderToolBrushData();
      packet.width = this.width.toIntArgPacket();
      packet.height = this.height.toIntArgPacket();
      packet.thickness = this.thickness.toIntArgPacket();
      packet.capped = this.capped.toBoolArgPacket();
      packet.shape = this.shape.toBrushShapeArgPacket();
      packet.origin = this.origin.toBrushOriginArgPacket();
      packet.originRotation = this.originRotation.toBoolArgPacket();
      packet.rotationAxis = this.rotationAxis.toBrushAxisArgPacket();
      packet.rotationAngle = this.rotationAngle.toRotationArgPacket();
      packet.mirrorAxis = this.mirrorAxis.toBrushAxisArgPacket();
      packet.material = this.material.toBlockArgPacket();
      packet.favoriteMaterials = Arrays.stream(this.favoriteMaterials)
         .filter(Objects::nonNull)
         .map(BlockArg::toBlockArgPacket)
         .toArray(BuilderToolBlockArg[]::new);
      packet.mask = this.mask.toMaskArgPacket();
      packet.maskAbove = this.maskAbove.toMaskArgPacket();
      packet.maskNot = this.maskNot.toMaskArgPacket();
      packet.maskBelow = this.maskBelow.toMaskArgPacket();
      packet.maskAdjacent = this.maskAdjacent.toMaskArgPacket();
      packet.maskNeighbor = this.maskNeighbor.toMaskArgPacket();
      packet.maskCommands = Arrays.stream(this.maskCommands).filter(Objects::nonNull).map(StringArg::toStringArgPacket).toArray(BuilderToolStringArg[]::new);
      packet.useMaskCommands = this.useMaskCommands.toBoolArgPacket();
      packet.invertMask = this.invertMask.toBoolArgPacket();
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BrushData{width="
         + this.width
         + ", height="
         + this.height
         + ", thickness="
         + this.thickness
         + ", capped="
         + this.capped
         + ", shape="
         + this.shape
         + ", origin="
         + this.origin
         + ", originRotation="
         + this.originRotation
         + ", rotationAxis="
         + this.rotationAxis
         + ", rotationAngle="
         + this.rotationAngle
         + ", mirrorAxis="
         + this.mirrorAxis
         + ", material="
         + this.material
         + ", favoriteMaterials="
         + Arrays.toString((Object[])this.favoriteMaterials)
         + ", mask="
         + this.mask
         + ", maskAbove="
         + this.maskAbove
         + ", maskNot="
         + this.maskNot
         + ", maskBelow="
         + this.maskBelow
         + ", maskAdjacent="
         + this.maskAdjacent
         + ", maskNeighbor="
         + this.maskNeighbor
         + ", maskCommands="
         + Arrays.toString((Object[])this.maskCommands)
         + ", useMaskCommands="
         + this.useMaskCommands
         + ", invertMask="
         + this.invertMask
         + "}";
   }

   public static class Values {
      public static final Codec<BrushData.Values> CODEC = BuilderCodec.builder(BrushData.Values.class, BrushData.Values::new)
         .append(new KeyedCodec<>("Width", Codec.INTEGER), (brushData, o) -> brushData.width = o, brushData -> brushData.width)
         .addValidator(Validators.greaterThan(0))
         .documentation("The width of the brush shape")
         .add()
         .<Integer>append(new KeyedCodec<>("Height", Codec.INTEGER), (brushData, o) -> brushData.height = o, brushData -> brushData.height)
         .addValidator(Validators.greaterThan(0))
         .documentation("The height of the brush shape")
         .add()
         .<Integer>append(new KeyedCodec<>("Thickness", Codec.INTEGER), (data, o) -> data.thickness = o, data -> data.thickness)
         .addValidator(Validators.range(0, 100))
         .documentation("The number of blocks thick the walls of the brush shape should be")
         .add()
         .<Boolean>append(new KeyedCodec<>("Capped", Codec.BOOLEAN), (data, o) -> data.capped = o, data -> data.capped)
         .addValidator(Validators.nonNull())
         .documentation("Controls whether the end(s) of hollow brush shapes are closed or open")
         .add()
         .<BrushShape>append(new KeyedCodec<>("Shape", BrushShapeArg.BRUSH_SHAPE_CODEC), (brushData, o) -> brushData.shape = o, brushData -> brushData.shape)
         .addValidator(Validators.nonNull())
         .documentation("The brush shape")
         .add()
         .<BrushOrigin>append(
            new KeyedCodec<>("Origin", BrushOriginArg.BRUSH_ORIGIN_CODEC), (brushData, o) -> brushData.origin = o, brushData -> brushData.origin
         )
         .addValidator(Validators.nonNull())
         .documentation("The origin of the brush shape")
         .add()
         .<Boolean>append(new KeyedCodec<>("OriginRotation", Codec.BOOLEAN), (data, o) -> data.originRotation = o, data -> data.originRotation)
         .addValidator(Validators.nonNull())
         .documentation("Toggles the vertical offset for shapes rotated about the x/z axis")
         .add()
         .<BrushAxis>append(new KeyedCodec<>("RotationAxis", BrushAxisArg.BRUSH_AXIS_CODEC), (data, o) -> data.rotationAxis = o, data -> data.rotationAxis)
         .addValidator(Validators.nonNull())
         .documentation("The axis that the brush shape should rotate around")
         .add()
         .<Rotation>append(new KeyedCodec<>("RotationAngle", BrushRotationArg.ROTATION_CODEC), (data, o) -> data.rotationAngle = o, data -> data.rotationAngle)
         .addValidator(Validators.nonNull())
         .documentation("The angle that the brush shape should be rotated by")
         .add()
         .<BrushAxis>append(new KeyedCodec<>("MirrorAxis", BrushAxisArg.BRUSH_AXIS_CODEC), (data, o) -> data.mirrorAxis = o, data -> data.mirrorAxis)
         .addValidator(Validators.nonNull())
         .documentation("The axis that the brush shape should mirror in")
         .add()
         .<BlockPattern>append(new KeyedCodec<>("Material", BlockPattern.CODEC), (brushData, o) -> brushData.material = o, brushData -> brushData.material)
         .addValidator(Validators.nonNull())
         .documentation("The material to apply when the brush is used")
         .add()
         .<BlockPattern[]>append(
            new KeyedCodec<>("FavoriteMaterials", new ArrayCodec<>(BlockPattern.CODEC, BlockPattern[]::new)),
            (brushData, o) -> brushData.favoriteMaterials = o,
            brushData -> brushData.favoriteMaterials
         )
         .addValidator(Validators.arraySizeRange(0, 5))
         .documentation("Materials available for quick selection.\n\nWhen a material is selected from here, it is set on the Material key.")
         .add()
         .<BlockMask>append(new KeyedCodec<>("Mask", BlockMask.CODEC), (brushData, o) -> brushData.mask = o, brushData -> brushData.mask)
         .documentation("Limits the selection to blocks matching materials in this mask")
         .add()
         .<BlockMask>append(new KeyedCodec<>("MaskAbove", BlockMask.CODEC), (brushData, o) -> brushData.maskAbove = o, brushData -> brushData.maskAbove)
         .documentation("Limits the selection to blocks above ones matching materials in this mask")
         .add()
         .<BlockMask>append(new KeyedCodec<>("MaskNot", BlockMask.CODEC), (brushData, o) -> brushData.maskNot = o, brushData -> brushData.maskNot)
         .documentation("Limits the selection to any blocks except ones matching materials in this mask")
         .add()
         .<BlockMask>append(new KeyedCodec<>("MaskBelow", BlockMask.CODEC), (brushData, o) -> brushData.maskBelow = o, brushData -> brushData.maskBelow)
         .documentation("Limits the selection to blocks below ones matching materials in this mask")
         .add()
         .<BlockMask>append(
            new KeyedCodec<>("MaskAdjacent", BlockMask.CODEC), (brushData, o) -> brushData.maskAdjacent = o, brushData -> brushData.maskAdjacent
         )
         .documentation("Limits the selection to blocks horizontally adjacent to ones matching materials in this mask")
         .add()
         .<BlockMask>append(
            new KeyedCodec<>("MaskNeighbor", BlockMask.CODEC), (brushData, o) -> brushData.maskNeighbor = o, brushData -> brushData.maskNeighbor
         )
         .documentation("Limits the selection to blocks neighboring (in any direction) ones matching materials in this mask")
         .add()
         .<String[]>append(
            new KeyedCodec<>("MaskCommands", Codec.STRING_ARRAY), (brushData, o) -> brushData.maskCommands = o, brushData -> brushData.maskCommands
         )
         .documentation("Custom mask commands to apply to the brush, based on /gmask syntax")
         .add()
         .<Boolean>append(
            new KeyedCodec<>("UseMaskCommands", Codec.BOOLEAN), (brushData, o) -> brushData.useMaskCommands = o, brushData -> brushData.useMaskCommands
         )
         .documentation("Specifies whether to use the block selector mask values or custom mask commands")
         .add()
         .<Boolean>append(new KeyedCodec<>("InvertMask", Codec.BOOLEAN), (brushData, o) -> brushData.invertMask = o, brushData -> brushData.invertMask)
         .documentation("When enabled, inverts the entire combined mask result")
         .add()
         .build();
      private int width;
      private int height;
      private int thickness;
      private boolean capped;
      private BrushShape shape;
      private BrushOrigin origin;
      private boolean originRotation;
      private BrushAxis rotationAxis;
      private Rotation rotationAngle;
      private BrushAxis mirrorAxis;
      private BlockPattern material;
      private BlockPattern[] favoriteMaterials;
      private BlockMask mask;
      private BlockMask maskAbove;
      private BlockMask maskNot;
      private BlockMask maskBelow;
      private BlockMask maskAdjacent;
      private BlockMask maskNeighbor;
      private String[] maskCommands;
      private boolean useMaskCommands;
      private boolean invertMask;

      protected Values() {
         this(BrushData.DEFAULT);
      }

      public Values(@Nonnull BrushData brushData) {
         this.width = brushData.width.getValue();
         this.height = brushData.height.getValue();
         this.thickness = brushData.thickness.getValue();
         this.capped = brushData.capped.getValue();
         this.shape = brushData.shape.getValue();
         this.origin = brushData.origin.getValue();
         this.originRotation = brushData.originRotation.getValue();
         this.rotationAxis = brushData.rotationAxis.getValue();
         this.rotationAngle = brushData.rotationAngle.getValue();
         this.mirrorAxis = brushData.mirrorAxis.getValue();
         this.material = brushData.material.getValue();
         this.favoriteMaterials = brushData.favoriteMaterials.length == 0
            ? BlockPattern.EMPTY_ARRAY
            : Arrays.stream(brushData.favoriteMaterials).limit(5L).map(ToolArg::getValue).toArray(BlockPattern[]::new);
         this.mask = brushData.mask.getValue();
         this.maskAbove = brushData.maskAbove.getValue();
         this.maskNot = brushData.maskNot.getValue();
         this.maskBelow = brushData.maskBelow.getValue();
         this.maskAdjacent = brushData.maskAdjacent.getValue();
         this.maskNeighbor = brushData.maskNeighbor.getValue();
         this.maskCommands = brushData.maskCommands.length == 0
            ? ArrayUtil.EMPTY_STRING_ARRAY
            : Arrays.stream(brushData.maskCommands).map(ToolArg::getValue).toArray(String[]::new);
         this.useMaskCommands = brushData.useMaskCommands.getValue();
         this.invertMask = brushData.invertMask.getValue();
      }

      public Values(
         int width,
         int height,
         int thickness,
         boolean capped,
         BrushShape shape,
         BrushOrigin origin,
         boolean originRotation,
         BrushAxis rotationAxis,
         Rotation rotationAngle,
         BrushAxis mirrorAxis,
         BlockPattern material,
         BlockPattern[] favoriteMaterials,
         BlockMask mask,
         BlockMask maskAbove,
         BlockMask maskNot,
         BlockMask maskBelow,
         BlockMask maskAdjacent,
         BlockMask maskNeighbor,
         String[] maskCommands,
         boolean useMaskCommands
      ) {
         this.width = width;
         this.height = height;
         this.thickness = thickness;
         this.capped = capped;
         this.shape = shape;
         this.origin = origin;
         this.originRotation = originRotation;
         this.rotationAxis = rotationAxis;
         this.rotationAngle = rotationAngle;
         this.mirrorAxis = mirrorAxis;
         this.material = material;
         this.favoriteMaterials = favoriteMaterials;
         this.mask = mask;
         this.maskAbove = maskAbove;
         this.maskNot = maskNot;
         this.maskBelow = maskBelow;
         this.maskAdjacent = maskAdjacent;
         this.maskNeighbor = maskNeighbor;
         this.maskCommands = maskCommands;
         this.useMaskCommands = useMaskCommands;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public int getThickness() {
         return this.thickness;
      }

      public boolean isCapped() {
         return this.capped;
      }

      public BrushShape getShape() {
         return this.shape;
      }

      public BrushOrigin getOrigin() {
         return this.origin;
      }

      public boolean getOriginRotation() {
         return this.originRotation;
      }

      public BrushAxis getRotationAxis() {
         return this.rotationAxis;
      }

      public Rotation getRotationAngle() {
         return this.rotationAngle;
      }

      public BrushAxis getMirrorAxis() {
         return this.mirrorAxis;
      }

      public BlockPattern getMaterial() {
         return this.material;
      }

      public BlockPattern[] getFavoriteMaterials() {
         return this.favoriteMaterials;
      }

      public BlockMask getMask() {
         return this.mask;
      }

      public BlockMask getMaskAbove() {
         return this.maskAbove;
      }

      public BlockMask getMaskNot() {
         return this.maskNot;
      }

      public BlockMask getMaskBelow() {
         return this.maskBelow;
      }

      public BlockMask getMaskAdjacent() {
         return this.maskAdjacent;
      }

      public BlockMask getMaskNeighbor() {
         return this.maskNeighbor;
      }

      public String[] getMaskCommands() {
         return this.maskCommands;
      }

      @Nonnull
      public BlockMask[] getParsedMaskCommands() {
         return Arrays.stream(this.getMaskCommands()).map(m -> m.split(" ")).map(BlockMask::parse).toArray(BlockMask[]::new);
      }

      public boolean shouldUseMaskCommands() {
         return this.useMaskCommands;
      }

      public boolean shouldInvertMask() {
         return this.invertMask;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Values{width="
            + this.width
            + ", height="
            + this.height
            + ", thickness="
            + this.thickness
            + ", capped="
            + this.capped
            + ", shape="
            + this.shape
            + ", origin="
            + this.origin
            + ", originRotation="
            + this.originRotation
            + ", rotationAxis="
            + this.rotationAxis
            + ", rotationAngle="
            + this.rotationAngle
            + ", mirrorAxis="
            + this.mirrorAxis
            + ", material="
            + this.material
            + ", favoriteMaterials="
            + Arrays.toString((Object[])this.favoriteMaterials)
            + ", mask="
            + this.mask
            + ", maskAbove="
            + this.maskAbove
            + ", maskNot="
            + this.maskNot
            + ", maskBelow="
            + this.maskBelow
            + ", maskAdjacent="
            + this.maskAdjacent
            + ", maskNeighbor="
            + this.maskNeighbor
            + ", maskCommands="
            + Arrays.toString((Object[])this.maskCommands)
            + ", useMaskCommands="
            + this.useMaskCommands
            + ", invertMask="
            + this.invertMask
            + "}";
      }
   }
}
